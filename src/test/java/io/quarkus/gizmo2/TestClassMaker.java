package io.quarkus.gizmo2;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.ClassFile;
import io.github.dmlloyd.classfile.ClassHierarchyResolver;
import io.github.dmlloyd.classfile.ClassModel;
import io.quarkus.gizmo2.impl.Util;

/**
 * Can accept arbitrarily many classes, but note that the following helper methods
 * only operate on the <em>last</em> accepted class:
 *
 * <ul>
 * <li>{@link #definedClass()}</li>
 * <li>{@link #staticMethod(String, Class)}</li>
 * <li>{@link #instanceMethod(String, Class)}</li>
 * <li>{@link #constructor(Class)}</li>
 * <li>{@link #noArgsConstructor(Class)}</li>
 * </ul>
 *
 * Call {@link #forClass(ClassDesc)} to obtain a helper object that contains the same
 * methods, except they operate on the given class.
 */
public class TestClassMaker implements ClassOutput {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();

    private final TestClassLoader cl = new TestClassLoader();
    private ClassDesc desc;

    public void write(final ClassDesc classDesc, final byte[] bytes) {
        if (System.getProperty("printClass") != null) {
            System.out.println(ClassFile.of().parse(bytes).toDebugString());
        }
        if (System.getProperty("dumpClass") != null) {
            try {
                Path path = Paths.get(classDesc.displayName() + ".class");
                System.out.println("Dump class to: " + path.toAbsolutePath());
                Files.write(path, bytes);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        cl.accept(classDesc, bytes);
        desc = classDesc;
    }

    public void write(final String path, final byte[] bytes) {
        if (path.endsWith(".class")) {
            throw new IllegalStateException("Class writing was not handled correctly");
        }
        cl.addResource(path, bytes);
    }

    public TestClassOps forClass(ClassDesc desc) {
        return new TestClassOps(cl, desc);
    }

    public Class<?> definedClass() {
        return forClass(desc).get();
    }

    public <T> T staticMethod(String name, Class<T> asType) {
        return forClass(desc).staticMethod(name, asType);
    }

    public <T> T instanceMethod(String name, Class<T> asType) {
        return forClass(desc).instanceMethod(name, asType);
    }

    public <T> T constructor(Class<T> asType) {
        return forClass(desc).constructor(asType);
    }

    public <T> T noArgsConstructor(Class<T> asType) {
        return forClass(desc).noArgsConstructor(asType);
    }

    public static class TestClassOps {
        private final TestClassLoader cl;
        private final ClassDesc desc;

        TestClassOps(TestClassLoader cl, ClassDesc desc) {
            this.cl = cl;
            this.desc = desc;
        }

        public ClassModel getModel() {
            try {
                return cl.loadClassModel(desc);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Class was not defined");
            }
        }

        public Class<?> get() {
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
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(get(), TestClassMaker.lookup);
                return MethodHandleProxies.asInterfaceInstance(asType, lookup.findStatic(get(), name, mt));
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
            for (Method candidate : get().getDeclaredMethods()) {
                int modifiers = candidate.getModifiers();
                if (Modifier.isStatic(modifiers)) {
                    continue;
                }
                if (candidate.getName().equals(name)) {
                    if (target != null) {
                        throw new IllegalArgumentException("Multiple methods called " + name + " exist on " + get());
                    }
                    target = candidate;
                }
            }
            if (target == null) {
                throw new IllegalAccessError("No method called " + name + " was found on " + get());
            }
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(get(), TestClassMaker.lookup);
                return MethodHandleProxies.asInterfaceInstance(asType, lookup.unreflect(target).asType(mt));
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        public <T> T constructor(Class<T> asType) {
            Method sam = findSAMSimple(asType);
            MethodType mt = MethodType.methodType(void.class, sam.getParameterTypes());
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(get(), TestClassMaker.lookup);
                return MethodHandleProxies.asInterfaceInstance(asType, lookup.findConstructor(get(), mt));
            } catch (NoSuchMethodException e) {
                throw new NoSuchMethodError(e.getMessage());
            } catch (IllegalAccessException e) {
                throw new IllegalAccessError(e.getMessage());
            }
        }

        public <T> T noArgsConstructor(Class<T> asType) {
            try {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(get(), TestClassMaker.lookup);
                return asType.cast(lookup.findConstructor(get(), MethodType.methodType(void.class)).invoke());
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
    }

    private static class TestClassLoader extends ClassLoader implements BiConsumer<ClassDesc, byte[]> {
        private final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, byte[]> resources = new ConcurrentHashMap<>();
        private final ClassFile cf;

        private TestClassLoader() {
            super("[TEST]", TestClassMaker.class.getClassLoader());
            cf = ClassFile.of(ClassFile.ClassHierarchyResolverOption.of(this::getClassInfo));
        }

        public ClassModel loadClassModel(final ClassDesc desc) throws ClassNotFoundException {
            if (desc.isClassOrInterface()) {
                return loadClassModel(Util.binaryName(desc));
            } else {
                throw new IllegalArgumentException("Descriptor must be class or interface");
            }
        }

        public ClassModel loadClassModel(final String name) throws ClassNotFoundException {
            byte[] bytes = classes.get(name);
            if (bytes == null) {
                throw new ClassNotFoundException(name);
            }
            return cf.parse(bytes);
        }

        public Class<?> loadClass(final String name) throws ClassNotFoundException {
            return name.startsWith("[") ? loadClass(ClassDesc.ofDescriptor(name)) : loadClass(ClassDesc.of(name));
        }

        public Class<?> loadClass(final ClassDesc desc) throws ClassNotFoundException {
            if (desc.isArray()) {
                return loadClass(desc.componentType()).arrayType();
            } else {
                if (desc.isPrimitive()) {
                    return switch (desc.descriptorString().charAt(0)) {
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
                    String dotName = Util.binaryName(desc);
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
                        } catch (VerifyError | ClassFormatError e) {
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
                String dotName = Util.binaryName(classDesc);
                byte[] existing = classes.putIfAbsent(dotName, bytes);
                if (existing != null) {
                    throw new IllegalArgumentException("Duplicate class " + classDesc);
                }
            }
        }

        public URL getResource(final String name) {
            byte[] bytes = resources.get(name);
            if (bytes != null) {
                try {
                    return new URL("tcm", null, -1, name, new URLStreamHandler() {
                        protected URLConnection openConnection(final URL u) {
                            return new URLConnection(u) {
                                public void connect() {
                                }

                                public InputStream getInputStream() {
                                    return new ByteArrayInputStream(bytes);
                                }

                                public int getContentLength() {
                                    return bytes.length;
                                }

                                public long getContentLengthLong() {
                                    return bytes.length;
                                }

                                public Object getContent() {
                                    return bytes.clone();
                                }

                                public Object getContent(final Class<?>... classes) {
                                    Set<Class<?>> set = Set.of(classes);
                                    if (set.contains(byte[].class)) {
                                        return bytes.clone();
                                    } else if (set.contains(String[].class)) {
                                        return new String(bytes, StandardCharsets.UTF_8).split("\n");
                                    } else if (set.contains(List.class)) {
                                        return List.of(new String(bytes, StandardCharsets.UTF_8).split("\n"));
                                    } else {
                                        return null;
                                    }
                                }
                            };
                        }
                    });
                } catch (MalformedURLException e) {
                    return null;
                }
            } else {
                return null;
            }
        }

        public Enumeration<URL> getResources(final String name) {
            URL url = getResource(name);
            return url == null ? Collections.emptyEnumeration() : Collections.enumeration(List.of(url));
        }

        public InputStream getResourceAsStream(final String name) {
            byte[] bytes = resources.get(name);
            return bytes == null ? null : new ByteArrayInputStream(bytes);
        }

        void addResource(final String path, final byte[] bytes) {
            resources.putIfAbsent(path, bytes.clone());
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
                return ClassHierarchyResolver.ClassHierarchyInfo.ofClass(
                        superClass == null ? null : Util.classDesc(superClass));
            }
        }
    }
}
