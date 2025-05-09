package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

import io.quarkus.gizmo2.TypeVariable;
import io.quarkus.gizmo2.TypeVariableCreator;

/**
 * A creator for things which can have type parameters.
 */
public sealed interface TypeParameterizedCreator permits ClassCreator, InterfaceCreator, ExecutableCreator {
    TypeVariable typeParameter(String name, Consumer<TypeVariableCreator> builder);
}
