package io.quarkus.gizmo2.creator;

import java.util.List;

import io.quarkus.gizmo2.GenericType;
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
     * Establish the (optional) first bound for the type variable.
     *
     * @param bound the first bound (must not be {@code null})
     */
    void withFirstBound(GenericType.OfReference bound);

    /**
     * Establish the other (secondary) bounds for the type variable.
     *
     * @param bounds the secondary bounds (must not be {@code null})
     */
    void withOtherBounds(List<GenericType.OfReference> bounds);
}
