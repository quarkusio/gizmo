package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.AbstractMethodCreatorImpl;
import io.quarkus.gizmo2.impl.NativeMethodCreatorImpl;
import io.quarkus.gizmo2.impl.StaticNativeMethodCreatorImpl;

/**
 * A builder for abstract methods.
 */
public sealed interface AbstractMethodCreator extends MethodCreator permits AbstractMethodCreatorImpl, NativeMethodCreatorImpl, StaticNativeMethodCreatorImpl {
}
