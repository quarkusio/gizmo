package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.impl.LambdaCreatorImpl;

/**
 * A creator for a lambda instance.
 */
public sealed interface LambdaCreator extends StaticExecutableCreator permits LambdaCreatorImpl {
    /**
     * {@return the descriptor of the lambda type}
     */
    ClassDesc lambdaType();

    /**
     * Capture an enclosing value as a variable so that it may be used in the lambda body.
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
    default Var capture(LocalVar outer) {
        return capture(outer.name(), outer);
    }

    /**
     * Build the body of the lambda.
     *
     * @param builder the builder for the lambda body (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);
}
