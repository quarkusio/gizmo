package io.quarkus.gizmo2.creator;

public sealed interface InstanceFieldCreator extends FieldCreator permits io.quarkus.gizmo2.impl.InstanceFieldCreatorImpl {
}
