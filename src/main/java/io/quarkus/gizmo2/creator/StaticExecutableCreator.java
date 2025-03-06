package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

/**
 * A creator for any kind of non-instance executable creator.
 */
public sealed interface StaticExecutableCreator extends ExecutableCreator permits LambdaCreator, StaticMethodCreator {

    /**
     * Build the body of this executable code.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);
}
