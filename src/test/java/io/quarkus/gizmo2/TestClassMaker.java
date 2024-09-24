package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.ClassFile;
import io.github.dmlloyd.classfile.components.ClassPrinter;

public class TestClassMaker implements BiConsumer<ClassDesc, byte[]> {

    private MethodHandles.Lookup lookup;

    public void accept(final ClassDesc classDesc, final byte[] bytes) {
        // print class
        ClassPrinter.toTree(ClassFile.of().parse(bytes), ClassPrinter.Verbosity.TRACE_ALL).toYaml(System.out::print);
        try {
            lookup = MethodHandles.lookup().defineHiddenClass(bytes, true);
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T staticMethod(String name, Class<T> asType) {
        Method sam = findSAMSimple(asType);
        MethodType mt = MethodType.methodType(sam.getReturnType(), sam.getParameterTypes());
        try {
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.findStatic(lookup.lookupClass(), name, mt));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T instanceMethod(String name, Class<T> asType) {
        Method sam = findSAMSimple(asType);
        Class<?>[] parameterTypes = sam.getParameterTypes();
        MethodType mt = MethodType.methodType(sam.getReturnType(), Arrays.copyOfRange(parameterTypes, 1, parameterTypes.length));
        try {
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.findVirtual(lookup.lookupClass(), name, mt));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    public <T> T constructor(Class<T> asType) {
        Method sam = findSAMSimple(asType);
        MethodType mt = MethodType.methodType(void.class, sam.getParameterTypes());
        try {
            return MethodHandleProxies.asInterfaceInstance(asType, lookup.findConstructor(lookup.lookupClass(), mt));
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodError(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new IllegalAccessError(e.getMessage());
        }
    }

    private static Method findSAMSimple(Class<?> type) {
        if (! type.isInterface()) {
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
