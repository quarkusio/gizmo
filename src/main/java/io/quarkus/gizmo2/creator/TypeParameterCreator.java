package io.quarkus.gizmo2.creator;

import java.util.List;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.impl.TypeParameterCreatorImpl;

/**
 * A creator for a type parameter.
 */
public sealed interface TypeParameterCreator extends AnnotatableCreator permits TypeParameterCreatorImpl {
    /**
     * {@return the name of the type variable being created (not {@code null})}
     */
    String name();

    /**
     * Establish the (optional) first bound for the type variable.
     * The first bound is a class type.
     *
     * @param bound the first bound (must not be {@code null})
     */
    void setFirstBound(GenericType.OfClass bound);

    /**
     * Establish the (optional) first bound for the type variable.
     * The first bound is a type variable.
     * In this case, no other bounds may be present.
     *
     * @param bound the first bound (must not be {@code null})
     */
    void setFirstBound(GenericType.OfTypeVariable bound);

    /**
     * Establish the other (secondary) bounds for the type variable.
     * The other bounds must all be interface types.
     * If the first bound is a type variable, there may be no other bounds.
     * <p>
     * It is possible to set the other bounds <em>without</em> setting the first bound.
     * In this case, all bounds are interface types.
     *
     * @param bounds the secondary bounds (must not be {@code null})
     */
    void setOtherBounds(List<GenericType.OfClass> bounds);

    /**
     * Establish the other (secondary) bounds for the type variable.
     * The other bounds must all be interface types.
     * If the first bound is a type variable, there may be no other bounds.
     * <p>
     * It is possible to set the other bounds <em>without</em> setting the first bound.
     * In this case, all bounds are interface types.
     *
     * @param bounds the secondary bounds (must not be {@code null})
     */
    default void setOtherBounds(GenericType.OfClass... bounds) {
        setOtherBounds(List.of(bounds));
    }
}
