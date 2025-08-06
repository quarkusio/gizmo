package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.This;

/**
 * A creator for instance executables.
 */
public sealed interface InstanceExecutableCreator extends ExecutableCreator, InstanceBodyCreator
        permits ConstructorCreator, InstanceMethodCreator {
    /**
     * {@return the {@code this} expression}
     */
    This this_();
}
