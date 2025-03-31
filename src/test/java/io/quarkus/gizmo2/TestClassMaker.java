package io.quarkus.gizmo2;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.ClassFile;
import io.github.dmlloyd.classfile.ClassHierarchyResolver;
import io.github.dmlloyd.classfile.ClassModel;
import io.quarkus.gizmo2.impl.Util;

public class TestClassMaker implements BiConsumer<ClassDesc, byte[]> {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private final TestClassLoader cl = new TestClassLoader();
    private ClassDesc desc;

    public void accept(final ClassDesc classDesc, final byte[] bytes) {
        if (System.getProperty("printClass") != null) {
            System.out.println(ClassFile.of().parse(bytes).toDebugString());
        }
        if (System.getProperty("dumpClass") != null) {
            try {
                Files.write(Paths.get(classDesc.displayName() + ".class"), bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        cl.accept(classDesc, bytes);
        desc = classDesc;
    }

    public Class<?> definedClass() {
        try {
            return cl.loadClass(desc);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Class was not defined");
        }
    }

    public <T> T staticMethod(String name, Class<T> asType) {
        Method sam = findSAMSimple(asType);
        MethodType mt = MethodType.methodType(sam.getReturnType(), sam.getParameterTypes());
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(definedClass(), TestClassMaker.lookup);
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.findStatic(definedClass(), name, mt));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T instanceMethod(String name, Class<T> asType) {
        Method sam = findSAMSimple(asType);
        Class<?>[] parameterTypes = sam.getParameterTypes();
        MethodType mt = MethodType.methodType(sam.getReturnType(), parameterTypes);
        Method target = null;
        for (Method candidate : definedClass().getDeclaredMethods()) {
            int modifiers = candidate.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                continue;
            }
            if (candidate.getName().equals(name)) {
                if (target != null) {
                    throw new IllegalArgumentException("Multiple methods called " + name + " exist on " + definedClass());
                }
                target = candidate;
            }
        }
        if (target == null) {
            throw new IllegalAccessError("No method called " + name + " was found on " + definedClass());
        }
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(definedClass(), TestClassMaker.lookup);
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.unreflect(target).asType(mt));
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T constructor(Class<T> asType) {
        Method sam = findSAMSimple(asType);
        MethodType mt = MethodType.methodType(void.class, sam.getParameterTypes());
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(definedClass(), TestClassMaker.lookup);
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.findConstructor(definedClass(), mt));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T noArgsConstructor(Class<T> asType) {
        try {
            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(definedClass(), TestClassMaker.lookup);
            return asType.cast(lookup.findConstructor(definedClass(), MethodType.methodType(void.class)).invoke());
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method findSAMSimple(Class<?> type) {
        if (!type.isInterface()) {
            throw new IllegalArgumentException("Not an interface");
        }
        // don't try too hard
        Method sam = null;
        for (Method method : type.getDeclaredMethods()) {
            if (Modifier.isAbstract(method.getModifiers())) {
                if (sam == null) {
                    sam = method;
                } else {
                    throw new IllegalArgumentException("Multiple abstract methods found");
                }
            }
        }
        if (sam == null) {
            throw new IllegalArgumentException("No obvious SAM found");
        }
        return sam;
    }

    private static class TestClassLoader extends ClassLoader implements BiConsumer<ClassDesc, byte[]> {
        private final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();
        private final ClassFile cf;

        private TestClassLoader() {
            super("[TEST]", TestClassMaker.class.getClassLoader());
            cf = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(this::getClassInfo));
        }

        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            return name.startsWith("[") ? loadClass(ClassDesc.ofDescriptor(name)) : loadClass(ClassDesc.of(name));
        }

        public Class<?> loadClass(final ClassDesc desc) throws ClassNotFoundException {
            if (desc.isArray()) {
                return loadClass(desc.componentType()).arrayType();
            } else {
                String ds = desc.descriptorString();
                if (desc.isPrimitive()) {
                    return switch (ds.charAt(0)) {
                        case 'B' -> byte.class;
                        case 'C' -> char.class;
                        case 'D' -> double.class;
                        case 'F' -> float.class;
                        case 'I' -> int.class;
                        case 'J' -> long.class;
                        case 'S' -> short.class;
                        case 'V' -> void.class;
                        case 'Z' -> boolean.class;
                        default -> throw new ClassNotFoundException(desc.toString());
                    };
                } else {
                    String dotName = ds.substring(1, ds.length() - 1).replace('/', '.');
                    Class<?> loaded = findLoadedClass(dotName);
                    if (loaded == null) {
                        byte[] bytes = classes.get(dotName);
                        if (bytes == null) {
                            return super.loadClass(dotName);
                        }
                        try {
                            Class<?> defined = defineClass(dotName, bytes, 0, bytes.length);
                            // trigger all verify errors
                            defined.getDeclaredMethods();
                            return defined;
                        } catch (VerifyError e) {
                            ClassModel cm = cf.parse(bytes);
                            VerifyError ve = new VerifyError(e.getMessage() + cm.toDebugString());
                            ve.setStackTrace(e.getStackTrace());
                            throw ve;
                        } catch (LinkageError e) {
                            loaded = findLoadedClass(dotName);
                            if (loaded == null) {
                                throw e;
                            }
                        }
                    }
                    return loaded;
                }
            }
        }

        public void accept(final ClassDesc classDesc, final byte[] bytes) {
            if (classDesc.isClassOrInterface()) {
                String ds = classDesc.descriptorString();
                String dotName = ds.substring(1, ds.length() - 1).replace('/', '.');
                byte[] existing = classes.putIfAbsent(dotName, bytes);
                if (existing != null) {
                    throw new IllegalArgumentException("Duplicate class " + classDesc);
                }
            }
        }

        private ClassHierarchyResolver.ClassHierarchyInfo getClassInfo(final ClassDesc classDesc) {
            Class<?> loaded;
            try {
                loaded = loadClass(classDesc);
            } catch (ClassNotFoundException e) {
                return null;
            }
            if (loaded.isInterface()) {
                return ClassHierarchyResolver.ClassHierarchyInfo.ofInterface();
            } else {
                Class<?> superClass = loaded.getSuperclass();
                return ClassHierarchyResolver.ClassHierarchyInfo.ofClass(superClass == null ? null : Util.classDesc(superClass));
            }
        }
    }
}
