package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.creator.ParamCreator;

/**
 * A thing which has a type.
 */
public sealed interface GenericTyped extends SimpleTyped permits TypeVariable, FieldCreator, ParamCreator {
    /**
     * {@return the generic type of this entity (not {@code null})}
     */
    GenericType genericType();
}
