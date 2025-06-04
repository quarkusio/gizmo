package io.quarkus.gizmo2.creator;

import java.util.function.Consumer;

import io.quarkus.gizmo2.GenericType;

/**
 * A creator for things which can have type parameters.
 */
public sealed interface TypeParameterizedCreator permits ClassCreator, InterfaceCreator, ExecutableCreator {
    /**
     * Creates a type parameter with given {@code name} and allows configuring its bounds.
     * Returns a reference to the type parameter in the form of a {@linkplain GenericType.OfTypeVariable type variable}.
     *
     * @param name the name of the type parameter
     * @param builder the builder to configure bounds of the type parameter
     * @return the type variable corresponding to the created type parameter
     */
    GenericType.OfTypeVariable typeParameter(String name, Consumer<TypeParameterCreator> builder);

    /**
     * Creates a type parameter with given {@code name} and no bounds.
     * Returns a reference to the type parameter in the form of a {@linkplain GenericType.OfTypeVariable type variable}.
     *
     * @param name the name of the type parameter
     * @return the type variable corresponding to the created type parameter
     */
    default GenericType.OfTypeVariable typeParameter(String name) {
        return typeParameter(name, tpc -> {
        });
    }
}
