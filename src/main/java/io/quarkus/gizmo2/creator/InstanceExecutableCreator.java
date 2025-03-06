package io.quarkus.gizmo2.creator;

import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;

/**
 * A creator for instance executables.
 */
public sealed interface InstanceExecutableCreator extends ExecutableCreator permits ConstructorCreator, InstanceMethodCreator {

    /**
     * Build the body.
     * The builder accepts the outermost block, and the {@code this} expression.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(BiConsumer<BlockCreator, Expr> builder);

}
