package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.GenericTyped;
import io.quarkus.gizmo2.impl.ParamCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator interface for parameters.
 */
public sealed interface ParamCreator extends ModifiableCreator, GenericTyped permits ParamCreatorImpl {
    /**
     * Change the type of this parameter.
     *
     * @param type the new generic type (must not be {@code null})
     * @throws IllegalArgumentException if the new type is different from the established type
     */
    void type(GenericType type);

    /**
     * Change the type of this parameter.
     *
     * @param type the descriptor of the new type (must not be {@code null})
     * @throws IllegalArgumentException if the new type is different from the established type
     */
    void type(ClassDesc type);

    /**
     * {@return the type of this parameter (not {@code null})}
     */
    ClassDesc type();

    /**
     * Change the type of this parameter.
     *
     * @param type the new type (must not be {@code null})
     */
    default void type(Class<?> type) {
        type(Util.classDesc(type));
    }

    /**
     * Add the "mandated" modifier flag to this creator.
     */
    default void mandated() {
        addFlag(ModifierFlag.MANDATED);
    }

    /**
     * @deprecated Use {@link #type(GenericType)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(GenericType type) {
        type(type);
    }

    /**
     * @deprecated Use {@link #type(ClassDesc)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(ClassDesc type) {
        type(type);
    }

    /**
     * @deprecated Use {@link #type(Class)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(Class<?> type) {
        type(type);
    }
}
