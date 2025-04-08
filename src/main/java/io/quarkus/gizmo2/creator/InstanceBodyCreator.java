package io.quarkus.gizmo2.creator;

/**
 * A thing which has an instance body.
 */
public sealed interface InstanceBodyCreator extends BodyCreator permits InstanceExecutableCreator {
}
