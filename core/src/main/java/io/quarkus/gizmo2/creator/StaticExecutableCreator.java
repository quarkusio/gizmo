package io.quarkus.gizmo2.creator;

/**
 * A creator for any kind of non-instance executable creator.
 */
public sealed interface StaticExecutableCreator extends ExecutableCreator, BodyCreator permits StaticMethodCreator {
}
