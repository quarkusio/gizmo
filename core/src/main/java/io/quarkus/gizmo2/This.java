package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.ThisExpr;

/**
 * The special expression for {@code this}, which is only valid from instance methods and constructors.
 */
public sealed interface This extends Expr permits ThisExpr {
}
