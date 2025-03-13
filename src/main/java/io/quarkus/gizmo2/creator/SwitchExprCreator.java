package io.quarkus.gizmo2.creator;

import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.function.Function;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.impl.SwitchExprCreatorImpl;

/**
 * A creator for a switch expression.
 * The body of each switch expression case must yield a result.
 */
public sealed interface SwitchExprCreator extends SimpleTyped permits SwitchExprCreatorImpl {
    /**
     * Add a case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    void case_(Constant val, Function<BlockCreator, Expr> body);

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(ConstantDesc val, Function<BlockCreator, Expr> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(Constable val, Function<BlockCreator, Expr> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(int val, Function<BlockCreator, Expr> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(long val, Function<BlockCreator, Expr> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add a switch case.
     *
     * @param val the switch case value (must not be {@code null})
     * @param body the builder for the body of the case (must not be {@code null})
     */
    default void case_(String val, Function<BlockCreator, Expr> body) {
        case_(Constant.of(val), body);
    }

    /**
     * Add the default case.
     *
     * @param body the builder for the body of the default case (must not be {@code null})
     */
    void default_(Function<BlockCreator, Expr> body);
}
