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
     * @param name the fully qualified (dot-separated) binary class name
     * @param builder the
     * @return the descriptor
     */
    default ClassDesc class_(String name, Consumer<ClassCreator> builder) {
        return class_(ClassDesc.of(name), builder);
    }
    
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
     * @param name the fully qualified (dot-separated) binary class name
     * @param builder the
     * @return the descriptor
     */
    default ClassDesc interface_(String name, Consumer<InterfaceCreator> builder) {
        return interface_(ClassDesc.of(name), builder);
    }
    
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
