package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.impl.StaticFieldCreatorImpl;

/**
 * A creator for a static field.
 */
public sealed interface StaticFieldCreator extends FieldCreator permits StaticFieldCreatorImpl {
    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value (must not be {@code null})
     */
    void withInitial(Constant initial);

    /**
     * Provide an initializer for this field which will be concatenated with the class initializer(s).
     *
     * @param init the builder for the initializer which yields the field initial value (must not be {@code null})
     */
    void withInitializer(Consumer<BlockCreator> init);
}
