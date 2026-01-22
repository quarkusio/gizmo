package io.quarkus.gizmo2.testing;

import static java.lang.invoke.MethodHandles.*;
import static java.util.Collections.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A utility for testing generated classes which may be used in a unit test.
 * The functions of this class are not thread-safe and are not usable for concurrency testing.
 * By default, defined classes have assertions enabled.
 */
public final class TestClassMaker {
    private final Gizmo gizmo;
    private final Loader cl;

    private TestClassMaker(final Gizmo gizmo, final ClassLoader parent) {
        cl = new Loader(parent);
        cl.setDefaultAssertionStatus(true);
        TestClassMaker.class.getModule().addReads(cl.getUnnamedModule());
        this.gizmo = gizmo.withOutput(new ClassOutput() {
            public void write(final ClassDesc desc, final byte[] bytes) {
                Assert.checkNotNullParam("desc", desc);
                Assert.checkNotNullParam("bytes", bytes);
                String bn = Util.binaryName(desc);
                if (cl.classes.putIfAbsent(bn, bytes) != null) {
                    throw new IllegalArgumentException("Class " + bn + " already defined");
                }
            }

            public void write(final String path, final byte[] bytes) {
                Assert.checkNotNullParam("path", path);
                Assert.checkNotNullParam("bytes", bytes);
                cl.resources.computeIfAbsent(path, ignored -> new ArrayList<>()).add(bytes);
            }
        });
    }

    /**
     * Construct a new instance.
     * The given {@code gizmo} is used, except for its class output configuration
     * which is ignored.
     *
     * @param gizmo the configured ase Gizmo instance to use (must not be {@code null})
     * @return a new test class maker instance (not {@code null})
     */
    public static TestClassMaker create(final Gizmo gizmo) {
        return new TestClassMaker(gizmo, TestClassMaker.class.getClassLoader());
    }

    /**
     * Construct a new instance.
     *
     * @return a new test class maker instance (not {@code null})
     */
    public static TestClassMaker create() {
        return create(Gizmo.create());
    }

    /**
     * {@return the configured Gizmo instance (not {@code null})}
     * The given instance can be used to create new classes and interfaces.
     */
    public Gizmo gizmo() {
        return gizmo;
    }

    /**
     * {@return the class loader associated with this instance}
     * All test classes are defined to this loader.
     */
    public ClassLoader classLoader() {
        return cl;
    }

    /**
     * Register a new resource with this loader.
     * The lines of text are encoded using the {@code UTF-8} character encoding.
     * Each given line of text is terminated with a newline character ({@code NL} aka {@code 0x0D (12)}).
     *
     * @param path the resource path (must not be {@code null})
     * @param lines the lines of text for the resource (must not be {@code null})
     */
    public void registerResource(String path, List<String> lines) {
        Assert.checkNotNullParam("lines", lines);
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            for (String s : lines) {
                os.write(s.getBytes(StandardCharsets.UTF_8));
                os.write('\n');
            }
            registerResource(path, os.toByteArray());
        } catch (IOException ignored) {
            // not possible
            throw new IllegalStateException();
        }
    }

    /**
     * Register a new resource with this loader.
     * The text is encoded using the {@code UTF-8} character encoding.
     *
     * @param path the resource path (must not be {@code null})
     * @param data the text data of the resource (must not be {@code null})
     */
    public void registerResource(String path, String data) {
        Assert.checkNotNullParam("data", data);
        registerResource(path, data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Register a new resource with this loader.
     *
     * @param path the resource path (must not be {@code null})
     * @param data the binary data of the resource (must not be {@code null})
     */
    public void registerResource(String path, byte[] data) {
        Assert.checkNotNullParam("path", path);
        Assert.checkNotNullParam("data", data);
        byte[] cloned = data.clone();
        cl.resources.computeIfAbsent(path, ignored -> new ArrayList<>()).add(cloned);
    }

    /**
     * Load a class from this loader with the given name.
     *
     * @param className the name of the class to load (must not be {@code null})
     * @return the loaded class (not {@code null})
     * @throws NoSuchElementException if there is no class with that name defined to this class loader
     */
    public Class<?> loadClass(String className) {
        return loadClass(className, Object.class);
    }

    /**
     * Load a class from this loader with the given descriptor.
     *
     * @param classDesc the descriptor of the class to load (must not be {@code null})
     * @return the loaded class (not {@code null})
     * @throws NoSuchElementException if there is no class with that descriptor defined to this class loader
     */
    public Class<?> loadClass(ClassDesc classDesc) {
        return loadClass(classDesc, Object.class);
    }

    /**
     * Load a class from this loader with the given name.
     *
     * @param className the name of the class to load (must not be {@code null})
     * @param expectedSupertype the expected supertype of the class (must not be {@code null})
     * @return the loaded class (not {@code null})
     * @throws NoSuchElementException if there is no class with that name defined to this class loader
     * @throws ClassCastException if the loaded class is not of the given type
     */
    public <T> Class<? extends T> loadClass(String className, Class<T> expectedSupertype) {
        Assert.checkNotNullParam("className", className);
        Assert.checkNotNullParam("expectedSupertype", expectedSupertype);
        Class<?> clazz = cl.tryLoadLocalClass(className);
        if (clazz == null) {
            throw new NoSuchElementException("No class named " + className + " found");
        }
        return clazz.asSubclass(expectedSupertype);
    }

    /**
     * Load a class from this loader with the given descriptor.
     *
     * @param classDesc the descriptor of the class to load (must not be {@code null})
     * @param expectedSupertype the expected supertype of the class (must not be {@code null})
     * @return the loaded class (not {@code null})
     * @throws NoSuchElementException if there is no class with that descriptor defined to this class loader
     * @throws ClassCastException if the loaded class is not of the given type
     */
    public <T> Class<? extends T> loadClass(ClassDesc classDesc, Class<T> expectedSupertype) {
        Assert.checkNotNullParam("classDesc", classDesc);
        Assert.checkNotNullParam("expectedSupertype", expectedSupertype);
        String ds = classDesc.descriptorString();
        Class<?> clazz = switch (ds.charAt(0)) {
            case 'L' -> loadClass(Util.binaryName(classDesc));
            case '[' -> loadClass(classDesc.arrayType()).arrayType();
            default -> null;
        };
        if (clazz == null) {
            throw new NoSuchElementException("No class for " + classDesc + " found");
        }
        return clazz.asSubclass(expectedSupertype);
    }

    /**
     * Read the bytes of a class and apply a transformation to them.
     *
     * @param className the name of the class to read (must not be {@code null})
     * @param func the function to apply (must not be {@code null})
     * @return the interpreted class
     * @param <T> the result type
     * @throws NoSuchElementException if there is no class with that name defined to this class loader
     */
    public <T> T readClass(String className, Function<byte[], T> func) {
        byte[] bytes = cl.classes.get(className);
        if (bytes != null) {
            return func.apply(bytes);
        }
        throw new NoSuchElementException("No class named " + className + " found");
    }

    /**
     * Read the bytes of a class and apply a transformation to them.
     *
     * @param classDesc the descriptor of the class to read (must not be {@code null})
     * @param func the function to apply (must not be {@code null})
     * @return the interpreted class
     * @param <T> the result type
     * @throws NoSuchElementException if there is no class with that descriptor defined to this class loader
     */
    public <T> T readClass(ClassDesc classDesc, Function<byte[], T> func) {
        if (classDesc.isClassOrInterface()) {
            String className = Util.binaryName(classDesc);
            byte[] bytes = cl.classes.get(className);
            if (bytes != null) {
                return func.apply(bytes);
            }
        }
        throw new NoSuchElementException("No class for " + classDesc + " found");
    }

    /**
     * Initialize a class defined by this test class maker.
     *
     * @param clazz the class to initialize (must not be {@code null})
     * @throws IllegalArgumentException if this test class maker does not own the given class
     */
    public void initializeClass(Class<?> clazz) {
        checkOwnership(clazz);
        if (clazz.isHidden()) {
            try {
                privateLookupIn(clazz, lookup()).ensureInitialized(clazz);
                return;
            } catch (IllegalAccessException ignored) {
            }
            try {
                // sometimes public works where private doesn't (because it's not associated with any module)
                publicLookup().ensureInitialized(clazz);
                return;
            } catch (IllegalAccessException ignored) {
                throw new IllegalArgumentException("Cannot access " + clazz + " to initialize it");
            }
        } else {
            // class has a name; just do it the easy way
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Class unexpectedly missing");
            }
        }
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(String className, String methodName, Class<T> proxyType) {
        return staticMethod(className, methodName, typeOf(findSAMSimple(proxyType)), proxyType);
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the static class (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(String className, String methodName, MethodType methodType, Class<T> proxyType) {
        return staticMethod(loadClass(className, Object.class), methodName, methodType, proxyType);
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(ClassDesc classDesc, String methodName, Class<T> proxyType) {
        return staticMethod(classDesc, methodName, typeOf(findSAMSimple(proxyType)), proxyType);
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the static class (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(ClassDesc classDesc, String methodName, MethodType methodType, Class<T> proxyType) {
        return staticMethod(loadClass(classDesc, Object.class), methodName, methodType, proxyType);
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(Class<?> clazz, String methodName, Class<T> proxyType) {
        return staticMethod(clazz, methodName, typeOf(findSAMSimple(proxyType)), proxyType);
    }

    /**
     * Look up a static method on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the static method (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T staticMethod(Class<?> clazz, String methodName, MethodType methodType, Class<T> proxyType) {
        checkOwnership(clazz);
        MethodHandle handle;
        try {
            handle = privateLookupIn(clazz, lookup()).findStatic(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw noMethod(e, clazz.getName(), methodName, methodType);
        } catch (IllegalAccessException e) {
            throw noAccess(e, clazz.getName(), methodName, methodType);
        }
        return MethodHandleProxies.asInterfaceInstance(proxyType, handle);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(String className, String methodName, Class<T> proxyType) {
        return virtualMethod(className, methodName, typeOf(findSAMSimple(proxyType)).dropParameterTypes(0, 1), proxyType);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the static method (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(String className, String methodName, MethodType methodType, Class<T> proxyType) {
        return virtualMethod(loadClass(className, Object.class), methodName, methodType, proxyType);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(ClassDesc classDesc, String methodName, Class<T> proxyType) {
        return virtualMethod(classDesc, methodName, typeOf(findSAMSimple(proxyType)).dropParameterTypes(0, 1), proxyType);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the virtual method (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(ClassDesc classDesc, String methodName, MethodType methodType, Class<T> proxyType) {
        return virtualMethod(loadClass(classDesc, Object.class), methodName, methodType, proxyType);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(Class<?> clazz, String methodName, Class<T> proxyType) {
        return virtualMethod(clazz, methodName, typeOf(findSAMSimple(proxyType)).dropParameterTypes(0, 1), proxyType);
    }

    /**
     * Look up a virtual method on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param methodName the method name (must not be {@code null})
     * @param methodType the type of the virtual method (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target method (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no method which matches the expected name and type
     */
    public <T> T virtualMethod(Class<?> clazz, String methodName, MethodType methodType, Class<T> proxyType) {
        checkOwnership(clazz);
        MethodHandle handle;
        try {
            handle = privateLookupIn(clazz, lookup()).findVirtual(clazz, methodName, methodType);
        } catch (NoSuchMethodException e) {
            throw noMethod(e, clazz.getName(), methodName, methodType);
        } catch (IllegalAccessException e) {
            throw noAccess(e, clazz.getName(), methodName, methodType);
        }
        return MethodHandleProxies.asInterfaceInstance(proxyType, handle);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(String className, Class<T> proxyType) {
        return constructor(loadClass(className, Object.class), typeOf(findSAMSimple(proxyType)).changeReturnType(void.class),
                proxyType);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param methodType the type of the constructor (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(String className, MethodType methodType, Class<T> proxyType) {
        return constructor(loadClass(className, Object.class), methodType, proxyType);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(ClassDesc classDesc, Class<T> proxyType) {
        return constructor(loadClass(classDesc, Object.class), typeOf(findSAMSimple(proxyType)).changeReturnType(void.class),
                proxyType);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param methodType the type of the constructor (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(ClassDesc classDesc, MethodType methodType, Class<T> proxyType) {
        return constructor(loadClass(classDesc, Object.class), methodType, proxyType);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(Class<?> clazz, Class<T> proxyType) {
        return constructor(clazz, typeOf(findSAMSimple(proxyType)).changeReturnType(void.class), proxyType);
    }

    /**
     * Look up a constructor on a class defined to this test class maker.
     *
     * @param clazz the owner class (must not be {@code null})
     * @param methodType the type of the constructor (must not be {@code null})
     * @param proxyType the {@code Class} of the proxy interface (must not be {@code null})
     * @return the proxy instance which invokes the target constructor (not {@code null})
     * @param <T> the type of the proxy interface
     * @throws NoSuchElementException if there is no class with that name defined to this class loader,
     *         or if there is no constructor which matches the expected type
     */
    public <T> T constructor(Class<?> clazz, MethodType methodType, Class<T> proxyType) {
        checkOwnership(clazz);
        MethodHandle handle;
        try {
            handle = privateLookupIn(clazz, lookup()).findConstructor(clazz, methodType);
        } catch (NoSuchMethodException e) {
            throw noConstructor(e, clazz.getName(), methodType);
        } catch (IllegalAccessException e) {
            throw noAccess(e, clazz.getName(), methodType);
        }
        return MethodHandleProxies.asInterfaceInstance(proxyType, handle);
    }

    /**
     * Construct a new instance using a no-argument constructor on the class.
     *
     * @param className the owner class binary name (must not be {@code null})
     * @param expectedType the expected type of the instance (must not be {@code null})
     * @return the new instance, cast to the given expected type
     * @param <T> the expected type
     */
    public <T> T newInstance(String className, Class<T> expectedType) {
        return newInstance(loadClass(className, expectedType));
    }

    /**
     * Construct a new instance using a no-argument constructor on the class.
     *
     * @param classDesc the owner class descriptor (must not be {@code null})
     * @param expectedType the expected type of the instance (must not be {@code null})
     * @return the new instance, cast to the given expected type
     * @param <T> the expected type
     */
    public <T> T newInstance(ClassDesc classDesc, Class<T> expectedType) {
        return newInstance(loadClass(classDesc, expectedType));
    }

    /**
     * Construct a new instance using a no-argument constructor on the class.
     *
     * @param clazz the owner class (must not be {@code null})
     * @return the new instance
     * @param <T> the expected type
     */
    public <T> T newInstance(Class<T> clazz) {
        checkOwnership(clazz);
        MethodType methodType = MethodType.methodType(void.class);
        MethodHandle handle;
        try {
            handle = privateLookupIn(clazz, lookup()).findConstructor(clazz, methodType)
                    .asType(MethodType.methodType(Object.class));
        } catch (NoSuchMethodException e) {
            throw noConstructor(e, clazz.getName(), methodType);
        } catch (IllegalAccessException e) {
            throw noAccess(e, clazz.getName(), methodType);
        }
        try {
            return clazz.cast((Object) handle.invokeExact());
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    private void checkOwnership(final Class<?> clazz) {
        if (clazz.getClassLoader() != cl) {
            throw new IllegalArgumentException(clazz + " does not belong to the test class loader");
        }
    }

    private static MethodType typeOf(Method m) {
        return MethodType.methodType(m.getReturnType(), m.getParameterTypes());
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

    private static NoSuchElementException noMethod(final NoSuchMethodException e, final String className,
            final String methodName, final MethodType type) {
        return new NoSuchElementException("Method " + methodName + "(type " + type + ")" + " not found in " + className, e);
    }

    private static NoSuchElementException noConstructor(final NoSuchMethodException e, final String className,
            final MethodType type) {
        return new NoSuchElementException("Constructor (type " + type + ")" + " not found in " + className, e);
    }

    private static IllegalArgumentException noAccess(final IllegalAccessException e, final String className,
            final String methodName, final MethodType type) {
        return new IllegalArgumentException("Method " + methodName + "(type " + type + ")" + " not accessible in " + className,
                e);
    }

    private static IllegalArgumentException noAccess(final IllegalAccessException e, final String className,
            final MethodType type) {
        return new IllegalArgumentException("Constructor (type " + type + ")" + " not accessible in " + className, e);
    }

    private static final class Loader extends ClassLoader {
        private final ConcurrentHashMap<String, byte[]> classes = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, List<byte[]>> resources = new ConcurrentHashMap<>();

        static {
            registerAsParallelCapable();
        }

        private Loader(final ClassLoader parent) {
            super("TestClassMaker", parent);
        }

        Class<?> tryLoadLocalClass(String name) {
            Class<?> clazz = findLoadedClass(name);
            if (clazz != null) {
                return clazz;
            }
            byte[] bytes = classes.get(name);
            if (bytes == null) {
                return null;
            }
            try {
                clazz = defineClass(name, bytes, 0, bytes.length);
                return clazz;
            } catch (LinkageError e) {
                clazz = findLoadedClass(name);
                if (clazz != null) {
                    return clazz;
                }
                throw e;
            }
        }

        protected Class<?> loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            Class<?> clazz = tryLoadLocalClass(name);
            if (clazz != null) {
                return clazz;
            }
            ClassLoader parent = getParent();
            if (parent == null) {
                parent = ClassLoader.getSystemClassLoader();
            }
            return parent.loadClass(name);
        }

        public InputStream getResourceAsStream(final String name) {
            List<byte[]> list = resources.getOrDefault(name, List.of());
            if (list.isEmpty()) {
                return null;
            }
            return new ByteArrayInputStream(list.get(0));
        }

        public Enumeration<URL> getResources(final String name) {
            List<byte[]> list = resources.getOrDefault(name, List.of());
            return list.isEmpty() ? emptyEnumeration() : new Enumeration<URL>() {
                int idx = 0;

                public boolean hasMoreElements() {
                    return idx < list.size();
                }

                public URL nextElement() {
                    if (!hasMoreElements()) {
                        throw new NoSuchElementException();
                    }
                    return newUrl(list.get(idx++), name);
                }
            };
        }

        public URL getResource(final String name) {
            Enumeration<URL> e = getResources(name);
            return e.hasMoreElements() ? e.nextElement() : null;
        }

        public Stream<URL> resources(final String name) {
            return resources.getOrDefault(name, List.of()).stream().map(b -> newUrl(b, name));
        }

        private static URL newUrl(final byte[] bytes, final String name) {
            try {
                return new URL("tcm", null, -1, name, new URLHandler(bytes));
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            }
        }

        private static class URLHandler extends URLStreamHandler {
            private final byte[] bytes;

            URLHandler(final byte[] bytes) {
                this.bytes = bytes;
            }

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
                        for (Class<?> clazz : classes) {
                            if (clazz == byte[].class) {
                                return bytes.clone();
                            } else if (clazz == String.class || clazz == CharSequence.class) {
                                return new String(bytes, StandardCharsets.UTF_8);
                            } else if (clazz == String[].class) {
                                return new String(bytes, StandardCharsets.UTF_8).split("\n");
                            } else if (clazz == List.class) {
                                return List.of(new String(bytes, StandardCharsets.UTF_8).split("\n"));
                            }
                        }
                        return null;
                    }
                };
            }
        }
    }
}
