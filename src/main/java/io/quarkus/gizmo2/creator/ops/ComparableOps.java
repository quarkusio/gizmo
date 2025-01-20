package io.quarkus.gizmo2.creator.ops;

import io.quarkus.gizmo2.Expr;

/**
 * Operations on {@link Comparable}.
 */
public interface ComparableOps {
    /**
     * Generate a call to {@link Comparable#compareTo(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    Expr compareTo(Expr other);
}
