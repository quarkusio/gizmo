package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.LValueExprImpl;

public sealed interface LValueExpr extends Expr permits LValueExprImpl, Var {

}
