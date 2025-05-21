package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.InstanceFieldCreatorImpl;

/**
 * A creator for an instance field.
 */
public sealed interface InstanceFieldCreator extends FieldCreator permits InstanceFieldCreatorImpl {
}
