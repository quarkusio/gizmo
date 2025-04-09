package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.DefaultMethodCreatorImpl;
import io.quarkus.gizmo2.impl.InstanceMethodCreatorImpl;
import io.quarkus.gizmo2.impl.PrivateInterfaceMethodCreatorImpl;

/**
 * A creator for an instance method.
 */
public sealed interface InstanceMethodCreator extends InstanceExecutableCreator, MethodCreator
        permits DefaultMethodCreatorImpl, InstanceMethodCreatorImpl, PrivateInterfaceMethodCreatorImpl {
}
