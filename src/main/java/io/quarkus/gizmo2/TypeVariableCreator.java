package io.quarkus.gizmo2;

import java.util.List;

import io.quarkus.gizmo2.impl.TypeVariableCreatorImpl;

/**
 * A creator for a type variable.
 */
public sealed interface TypeVariableCreator extends AnnotatableCreator permits TypeVariableCreatorImpl {
    /**
     * {@return the name of the type variable being created (not {@code null})}
     */
    String name();

    /**
     * Establish the given bounds for the type variable.
     *
     * @param bounds the bounds (must not be {@code null})
     */
    void withBounds(List<GenericType.OfReference> bounds);
}
