package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;

/**
 * A creator for instance executables.
 */
public sealed interface InstanceExecutableCreator extends ExecutableCreator permits ConstructorCreator, InstanceMethodCreator {

    /**
     * Build the body.
     * The builder accepts the outermost block.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);

    /**
     * @return the {@code this} expression
     */
    Expr this_();

}
