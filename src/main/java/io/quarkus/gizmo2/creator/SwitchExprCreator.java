package io.quarkus.gizmo2.creator;

import java.util.function.Function;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;

/**
 * A creator for a switch expression.
 * The body of each switch expression case must yield a result.
 */
public interface SwitchExprCreator {
    /**
     * Add a case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    void case_(Constant val, Function<BlockCreator, Expr> body);

    /**
     * Add the default case.
     *
     * @param body the builder for the body of the default case (must not be {@code null})
     */
    void default_(Function<BlockCreator, Expr> body);
}
