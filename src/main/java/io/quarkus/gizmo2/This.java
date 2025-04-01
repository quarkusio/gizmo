package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.ThisExpr;

/**
 * The {@code this} reference.
 */
public sealed interface This extends Expr permits ThisExpr {
}
