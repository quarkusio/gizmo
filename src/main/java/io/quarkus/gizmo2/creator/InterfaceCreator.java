package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.impl.InterfaceCreatorImpl;

/**
 * A creator for an interface type.
 */
public sealed interface InterfaceCreator extends TypeCreator permits InterfaceCreatorImpl {

    /**
     * Add a default method to the interface.
     * The builder accepts the method builder plus the {@code this} expression for the method.
     *
     * @param name    the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc defaultMethod(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add an interface method to the interface.
     *
     * @param name    the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<AbstractMethodCreator> builder);
}
