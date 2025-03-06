package io.quarkus.gizmo2.desc;

import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import io.quarkus.gizmo2.impl.MethodDescImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A descriptor for a method.
 */
public sealed interface MethodDesc extends MemberDesc permits ClassMethodDesc, InterfaceMethodDesc, MethodDescImpl {
    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param type the descriptor of the type of the method (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, MethodTypeDesc type) {
        return owner.isInterface() ? InterfaceMethodDesc.of(Util.classDesc(owner), name, type) : ClassMethodDesc.of(Util.classDesc(owner), name, type);
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param type the type of the method (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, MethodType type) {
        return of(owner, name, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returning the class of the method return type (must not be {@code null})
     * @param argTypes the classes of the argument types (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, Class<?> returning, Class<?>... argTypes) {
        return of(owner, name, MethodType.methodType(returning, argTypes));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returning the class of the method return type (must not be {@code null})
     * @param argTypes the classes of the argument types (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, Class<?> returning, List<Class<?>> argTypes) {
        return of(owner, name, MethodType.methodType(returning, argTypes));
    }

    /**
     * {@return the descriptor of the method's type}
     */
    MethodTypeDesc type();
}
