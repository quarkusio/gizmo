package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.This;

/**
 * A thing which has an instance body.
 */
public sealed interface InstanceBodyCreator extends BodyCreator permits InstanceExecutableCreator {
    /**
     * {@return the {@code this} expression}
     */
    This this_();
}
