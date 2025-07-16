package io.quarkus.gizmo2.desc;

import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.List;

import io.quarkus.gizmo2.MethodTyped;
import io.quarkus.gizmo2.impl.MethodDescImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A descriptor for a method.
 */
public sealed interface MethodDesc extends MemberDesc, MethodTyped
        permits ClassMethodDesc, InterfaceMethodDesc, MethodDescImpl {
    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param type the descriptor of the type of the method (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, MethodTypeDesc type) {
        return owner.isInterface()
                ? InterfaceMethodDesc.of(Util.classDesc(owner), name, type)
                : ClassMethodDesc.of(Util.classDesc(owner), name, type);
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
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, Class<?> returnType, Class<?>... paramTypes) {
        return of(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the method (must not be {@code null})
     * @param name the name of the method (must not be {@code null})
     * @param returnType the class of the return type (must not be {@code null})
     * @param paramTypes the classes of the parameter types (must not be {@code null})
     * @return the method descriptor (must not be {@code null})
     */
    static MethodDesc of(Class<?> owner, String name, Class<?> returnType, List<Class<?>> paramTypes) {
        return of(owner, name, MethodType.methodType(returnType, paramTypes));
    }

    /**
     * {@return a method descriptor for the given method}
     *
     * @param method the method (must not be {@code null})
     */
    static MethodDesc of(Method method) {
        return of(method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getParameterTypes());
    }
}
