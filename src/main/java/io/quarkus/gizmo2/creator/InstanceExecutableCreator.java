package io.quarkus.gizmo2.creator;

import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;

public non-sealed interface InstanceExecutableCreator extends ExecutableCreator {

    /**
     * Build the body.
     * The builder accepts the outermost block, and the {@code this} expression.
     *
     * @param builder the builder (must not be {@code null})
     */
    void body(BiConsumer<BlockCreator, Expr> builder);

}
