package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.creator.ParamCreator;
import io.quarkus.gizmo2.creator.TypeCreator;

/**
 * A thing which has a generic type.
 */
public sealed interface GenericTyped extends SimpleTyped permits TypeParameter, FieldCreator, ParamCreator, TypeCreator {
    /**
     * {@return the generic type of this entity (not {@code null})}
     */
    GenericType genericType();

    /**
     * {@return {@code true} if this value has a generic type, or {@code false} if it does not have one}
     */
    boolean hasGenericType();
}
