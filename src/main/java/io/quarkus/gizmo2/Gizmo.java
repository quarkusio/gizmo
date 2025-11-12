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
     * {@return a Gizmo instance which has debug info output enabled or disabled}
     * <p>
     * By default, debug info is included.
     *
     * @param debugInfo {@code true} to include debug info, or {@code false} to exclude debug info
     */
    Gizmo withDebugInfo(boolean debugInfo);

    /**
     * {@return a Gizmo instance which has parameters output enabled or disabled}
     * This setting affects whether method parameter names are recorded in a {@code MethodParameters}
     * attribute, which is separate from debug info and appears when using runtime reflection.
     * Some frameworks require this attribute to be present.
     * <p>
     * By default, parameter name info is included.
     *
     * @param parameters {@code true} to include parameter name info, or {@code false} to exclude parameter name info
     */
    Gizmo withParameters(boolean parameters);

    /**
     * {@return a Gizmo instance which emits lambdas as anonymous classes if the parameter is {@code true}}
     * When enabled, lambda bodies are not generated into synthetic methods on the generated class,
     * but into additional anonymous classes. Further, lambda instances are not obtained through invokedynamic,
     * but through regular instantiation of the additional anonymous classes.
     * <p>
     * This doesn't affect runtime semantics of generated code, but it does affect the number
     * of generated classes and, as of this writing, also runtime memory consumption.
     * <p>
     * By default, lambdas are <em>not</em> generated as anonymous classes.
     *
     * @param lambdasAsAnonymousClasses whether lambdas should be generated as anonymous classes
     */
    Gizmo withLambdasAsAnonymousClasses(boolean lambdasAsAnonymousClasses);

    /**
     * {@return a Gizmo instance which uses the default modifiers configured by the given configurator}
     *
     * @param builder the builder for the defaults (must not be {@code null})
     */
    Gizmo withDefaultModifiers(Consumer<ModifierConfigurator> builder);

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
