package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.impl.ParamCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator interface for parameters.
 */
public sealed interface ParamCreator extends ModifiableCreator, SimpleTyped permits ParamCreatorImpl {
    /**
     * Change the type of this parameter.
     *
     * @param type the descriptor of the new type (must not be {@code null})
     * @throws IllegalArgumentException if the new type is different from the established type
     */
    void setType(ClassDesc type);

    /**
     * {@return the type of this parameter (not {@code null})}
     */
    ClassDesc type();

    /**
     * Change the type of this parameter.
     *
     * @param type the new type (must not be {@code null})
     */
    default void setType(Class<?> type) {
        setType(Util.classDesc(type));
    }

    /**
     * Add the "mandated" modifier flag to this creator.
     */
    default void mandated() {
        addFlag(ModifierFlag.MANDATED);
    }
}
