package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.impl.GizmoImpl;

/**
 * A simplified class file writer.
 */
public sealed interface Gizmo permits GizmoImpl {

    /**
     * {@return a new Gizmo which outputs to the given handler by default}
     *
     * @param outputHandler the output handler (must not be {@code null})
     */
    static Gizmo create(ClassOutput outputHandler) {
        return new GizmoImpl(outputHandler);
    }

    /**
     * {@return a Gizmo instance which uses the given output handler}
     *
     * @param outputHandler the output handler (must not be {@code null})
     */
    Gizmo withOutput(ClassOutput outputHandler);

    /**
     * {@return a Gizmo instance which uses the default modifiers configured by the given configurator}
     *
     * @param builder the builder for the defaults (must not be {@code null})
     */
    Gizmo withDefaultModifiers(Consumer<ModifierConfigurator> builder);

    /**
     * {@return a Gizmo instance which uses the given {@link ClassHierarchyLocator }}
     *
     * @param classHierarchyLocator the class hierarchy locator (must not be {@code null})
     */
    Gizmo withClassHierarchyLocator(ClassHierarchyLocator classHierarchyLocator);

    /**
     * Add a new class.
     *
     * @param name the fully qualified (dot-separated) binary class name (must not be {@code null})
     * @param builder the builder for the class (must not be {@code null})
     * @return the descriptor of the created class (not {@code null})
     */
    default ClassDesc class_(String name, Consumer<ClassCreator> builder) {
        return class_(ClassDesc.of(name), builder);
    }

    /**
     * Add a new class.
     *
     * @param desc the class descriptor (must not be {@code null})
     * @param builder the builder for the class (must not be {@code null})
     * @return the descriptor given for {@code desc}
     */
    ClassDesc class_(ClassDesc desc, Consumer<ClassCreator> builder);

    /**
     * Add a new interface.
     *
     * @param name the fully qualified (dot-separated) binary class name
     * @param builder the builder for the interface (must not be {@code null})
     * @return the descriptor of the created interface (not {@code null})
     */
    default ClassDesc interface_(String name, Consumer<InterfaceCreator> builder) {
        return interface_(ClassDesc.of(name), builder);
    }

    /**
     * Add a new interface.
     *
     * @param desc the class descriptor (must not be {@code null})
     * @param builder the builder for the class (must not be {@code null})
     * @return the descriptor given for {@code desc}
     */
    ClassDesc interface_(ClassDesc desc, Consumer<InterfaceCreator> builder);

    // todo: enum, record, @interface
}
