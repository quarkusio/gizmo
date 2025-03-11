package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.impl.LambdaCreatorImpl;

/**
 * A creator for a lambda instance.
 */
public sealed interface LambdaCreator extends BodyCreator permits LambdaCreatorImpl {
    /**
     * {@return the descriptor of the lambda functional interface}
     */
    ClassDesc type();

    /**
     * Access a parameter of the functional interface method declaration.
     *
     * @param name the name to assign to the parameter (must not be {@code null})
     * @param position the parameter position, starting from 0
     * @return the parameter's variable (not {@code null})
     */
    ParamVar param(String name, int position);

    /**
     * Capture an enclosing value as a variable so that it may be used in the lambda body.
     * All values that are created outside the lambda must be captured into new variables
     * (even {@code this}) in order to be used within the lambda, otherwise a generation-time
     * exception may be thrown.
     * <p>
     * If the given expression is a variable, the given name overrides the variable's name
     * within the scope of the lambda.
     *
     * @param name the name of the variable (must not be {@code null})
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

    /**
     * Build the body of the lambda.
     *
     * @param builder the builder for the lambda body (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);
}
