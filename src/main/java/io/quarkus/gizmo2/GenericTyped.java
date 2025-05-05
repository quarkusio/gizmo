package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.FieldCreator;

/**
 * A thing which has a type.
 */
public sealed interface GenericTyped extends SimpleTyped permits TypeVariable, FieldCreator {
    /**
     * {@return the generic type of this entity (not {@code null})}
     */
    GenericType genericType();
}
