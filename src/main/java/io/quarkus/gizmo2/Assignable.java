package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.AssignableImpl;

/**
 * An expression that can be the target of an assignment.
 */
public sealed interface Assignable extends Expr permits Var, AssignableImpl {
}
