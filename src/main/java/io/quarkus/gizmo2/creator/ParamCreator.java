package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.AnnotatableCreator;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.impl.ParamCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator interface for parameters.
 */
public sealed interface ParamCreator extends AnnotatableCreator, SimpleTyped permits ParamCreatorImpl {
    /**
     * Add a flag to this parameter.
     *
     * @param flag the flag to add (must not be {@code null})
     */
    void withFlag(AccessFlag flag);

    /**
     * Change the type of this parameter.
     *
     * @param type the new generic type (must not be {@code null})
     * @throws IllegalArgumentException if the new type is different from the established type
     */
    void withType(GenericType type);

    /**
     * Change the type of this parameter.
     *
     * @param type the descriptor of the new type (must not be {@code null})
     * @throws IllegalArgumentException if the new type is different from the established type
     */
    void withType(ClassDesc type);

    /**
     * {@return the type of this parameter (not {@code null})}
     */
    ClassDesc type();

    /**
     * Change the type of this parameter.
     *
     * @param type the new type (must not be {@code null})
     */
    default void withType(Class<?> type) {
        withType(Util.classDesc(type));
    }
}
