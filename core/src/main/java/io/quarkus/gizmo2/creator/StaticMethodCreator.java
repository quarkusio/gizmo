package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.StaticInterfaceMethodCreatorImpl;
import io.quarkus.gizmo2.impl.StaticMethodCreatorImpl;

/**
 * A builder for static methods.
 */
public sealed interface StaticMethodCreator extends MethodCreator, StaticExecutableCreator
        permits StaticMethodCreatorImpl, StaticInterfaceMethodCreatorImpl {
}
