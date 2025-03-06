package io.quarkus.gizmo2.creator;

/**
 * A creator for an instance field.
 */
public sealed interface InstanceFieldCreator extends FieldCreator permits io.quarkus.gizmo2.impl.InstanceFieldCreatorImpl {
}
