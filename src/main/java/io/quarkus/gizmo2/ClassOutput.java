package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

/**
 * A container for created classes with a specific output strategy.
 */
public interface ClassOutput {
    /**
     * Add a new class.
     *
     * @param desc the class descriptor (must not be {@code null})
     * @param builder the
     * @return the descriptor given for {@code desc}
     */
    ClassDesc class_(ClassDesc desc, Consumer<ClassCreator> builder);

    /**
     * Add a new interface.
     *
     * @param desc the class descriptor (must not be {@code null})
     * @param builder the
     * @return the descriptor given for {@code desc}
     */
    ClassDesc interface_(ClassDesc desc, Consumer<InterfaceCreator> builder);

    // todo: enum, record, @interface
}
