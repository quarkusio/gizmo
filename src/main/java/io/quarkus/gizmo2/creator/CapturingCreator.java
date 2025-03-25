package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Var;

/**
 * A creator for an entity which can capture values from an enclosing scope.
 */
public sealed interface CapturingCreator permits AnonymousClassCreator, LambdaCreator {
    /**
     * Capture an enclosing value as a variable so that it may be used in the lambda body.
     * All values that are created outside the lambda must be captured into new variables
     * (even {@code this}) in order to be used within the lambda, otherwise a generation-time
     * exception may be thrown.
     * <p>
     * If the given expression is a variable, the given name overrides the variable's name
     * within the scope of the lambda.
     *
     * @param name  the name of the variable (must not be {@code null})
     * @param value the capture value (must not be {@code null})
     * @return the captured variable (not {@code null})
     */
    Var capture(String name, Expr value);

    /**
     * Capture an enclosing variable so that it may be used in the lambda body.
     *
     * @param outer the enclosing variable to capture (must not be {@code null})
     * @return the captured variable (not {@code null})
     */
    default Var capture(Var outer) {
        return capture(outer.name(), outer);
    }
}
