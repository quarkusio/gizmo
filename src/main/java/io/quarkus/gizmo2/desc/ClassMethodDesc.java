package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import io.quarkus.gizmo2.impl.ClassMethodDescImpl;

/**
 * A descriptor for a method on a class.
 */
public sealed interface ClassMethodDesc extends MethodDesc permits ClassMethodDescImpl {
    /**
     * Create a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, MethodTypeDesc type) {
        return new ClassMethodDescImpl(owner, name, type);
    }

    /**
     * Create a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, MethodType type) {
        return of(owner, name, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }

    /**
     * Create a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, Class<?> returnType, Class<?>... paramTypes) {
        return of(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * Construct a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, Class<?> returnType, List<Class<?>> paramTypes) {
        return of(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * Create a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, ClassDesc returnType, ClassDesc... paramTypes) {
        return of(owner, name, MethodTypeDesc.of(returnType, paramTypes));
    }

    /**
     * Construct a new class method descriptor.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the class method descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, ClassDesc returnType, List<ClassDesc> paramTypes) {
        return of(owner, name, MethodTypeDesc.of(returnType, paramTypes.toArray(ClassDesc[]::new)));
    }
}
