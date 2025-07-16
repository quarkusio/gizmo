package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.AbstractMethodCreatorImpl;
import io.quarkus.gizmo2.impl.InterfaceMethodCreatorImpl;
import io.quarkus.gizmo2.impl.NativeMethodCreatorImpl;

/**
 * A builder for abstract methods.
 */
public sealed interface AbstractMethodCreator extends MethodCreator
        permits AbstractMethodCreatorImpl, InterfaceMethodCreatorImpl, NativeMethodCreatorImpl {
}
