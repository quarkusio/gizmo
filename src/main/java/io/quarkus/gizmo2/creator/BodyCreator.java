package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

/**
 * A creator that has a body.
 */
public sealed interface BodyCreator
        permits CaseCreator, InstanceBodyCreator, LambdaCreator, StaticExecutableCreator, TryCreator {
    /**
     * Build the body of this executable code.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);
}
