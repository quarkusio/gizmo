package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.impl.LambdaAsAnonClassCreatorImpl;
import io.quarkus.gizmo2.impl.LambdaAsMethodCreatorImpl;

/**
 * A creator for a lambda instance.
 */
public sealed interface LambdaCreator extends BodyCreator, CapturingCreator
        permits LambdaAsAnonClassCreatorImpl, LambdaAsMethodCreatorImpl {
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
    ParamVar parameter(String name, int position);

    /**
     * Build the body of the lambda.
     *
     * @param builder the builder for the lambda body (must not be {@code null})
     */
    void body(Consumer<BlockCreator> builder);
}
