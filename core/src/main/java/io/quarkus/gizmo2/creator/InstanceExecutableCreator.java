package io.quarkus.gizmo2.creator;

/**
 * A creator for instance executables.
 */
public sealed interface InstanceExecutableCreator extends ExecutableCreator, InstanceBodyCreator
        permits ConstructorCreator, InstanceMethodCreator {
}
