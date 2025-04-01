package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import io.quarkus.gizmo2.impl.ConstructorDescImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A descriptor for a constructor.
 */
public sealed interface ConstructorDesc extends MemberDesc permits ConstructorDescImpl {
    /**
     * Construct a new instance.
     *
     * @param owner the descriptor of the class which contains the member (must not be {@code null})
     * @param type the descriptor of the type of the constructor (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(ClassDesc owner, MethodTypeDesc type) {
        return new ConstructorDescImpl(owner, type);
    }

    /**
     * Construct a new instance for a zero-parameter constructor.
     *
     * @param owner the descriptor of the class which contains the member (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(ClassDesc owner) {
        return of(owner, MethodTypeDesc.of(ConstantDescs.CD_void));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the descriptor of the class which contains the member (must not be {@code null})
     * @param paramTypes a list of descriptors corresponding to the parameter types (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(ClassDesc owner, List<ClassDesc> paramTypes) {
        return of(owner, MethodTypeDesc.of(ConstantDescs.CD_void, paramTypes.toArray(ClassDesc[]::new)));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the member (must not be {@code null})
     * @param type the descriptor of the type of the constructor (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(Class<?> owner, MethodTypeDesc type) {
        return of(Util.classDesc(owner), type);
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the member (must not be {@code null})
     * @param type the type of the constructor (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(Class<?> owner, MethodType type) {
        return of(owner, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the member (must not be {@code null})
     * @param paramTypes a list of parameter types (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(Class<?> owner, Class<?>... paramTypes) {
        return of(owner, MethodType.methodType(void.class, paramTypes));
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the member (must not be {@code null})
     * @param paramTypes a list of parameter types (must not be {@code null})
     * @return the constructor descriptor (not {@code null})
     */
    static ConstructorDesc of(Class<?> owner, List<Class<?>> paramTypes) {
        return of(owner, MethodType.methodType(void.class, paramTypes));
    }

    /**
     * {@return a descriptor representing the type of this constructor}
     */
    MethodTypeDesc type();
}
