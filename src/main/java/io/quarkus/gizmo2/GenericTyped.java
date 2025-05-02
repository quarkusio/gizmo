package io.quarkus.gizmo2;

/**
 * A thing which has a type.
 */
public sealed interface GenericTyped permits TypeVariable {
    /**
     * {@return the generic type of this entity (not {@code null})}
     */
    GenericType genericType();
}
