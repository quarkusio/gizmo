package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.MethodCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for any kind of method on a class.
 */
public sealed interface MethodCreator extends ExecutableCreator, MemberCreator
        permits AbstractMethodCreator, InstanceMethodCreator, StaticMethodCreator, MethodCreatorImpl {

    /**
     * {@return the descriptor of the method}
     */
    MethodDesc desc();

    /**
     * Change the generic return type of this method.
     * The method type is changed with the new return type.
     *
     * @param type the generic return type (must not be {@code null})
     */
    void returning(GenericType type);

    /**
     * Change the return type of this method.
     * The method type is changed with the new return type.
     *
     * @param type the descriptor of the return type (must not be {@code null})
     */
    void returning(ClassDesc type);

    /**
     * Change the return type of this method.
     * The method type is changed with the new return type.
     *
     * @param type the return type (must not be {@code null})
     */
    default void returning(Class<?> type) {
        returning(Util.classDesc(type));
    }
}
