package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.LValueExprImpl;

/**
 * An expression that can be the target of an assignment.
 */
public sealed interface LValueExpr extends Expr permits LValueExprImpl, Var {

}
