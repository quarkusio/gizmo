package io.quarkus.gizmo2.transformer;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ClassTransformerImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A transformer for an existing class or interface.
 * <p>
 * Changes are recorded while the builder runs and are applied afterwards in a single pass.
 * The code of existing methods is copied unchanged.
 */
public sealed interface ClassTransformer extends ModifiableTransformer permits ClassTransformerImpl {
    /**
     * {@return the descriptor of the class being transformed}
     */
    ClassDesc type();

    /**
     * Transform an existing method.
     *
     * @param desc the method descriptor (must not be {@code null})
     * @param builder the method transformer builder (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such method
     */
    void method(MethodDesc desc, Consumer<MethodTransformer> builder);

    /**
     * Transform an existing constructor.
     *
     * @param desc the constructor descriptor (must not be {@code null})
     * @param builder the constructor transformer builder (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such constructor
     */
    void constructor(ConstructorDesc desc, Consumer<MethodTransformer> builder);

    /**
     * Transform an existing field.
     *
     * @param desc the field descriptor (must not be {@code null})
     * @param builder the field transformer builder (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such field
     */
    void field(FieldDesc desc, Consumer<FieldTransformer> builder);

    /**
     * Remove an existing method.
     *
     * @param desc the method descriptor (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such method
     */
    void removeMethod(MethodDesc desc);

    /**
     * Remove an existing constructor.
     *
     * @param desc the constructor descriptor (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such constructor
     */
    void removeConstructor(ConstructorDesc desc);

    /**
     * Remove an existing field.
     *
     * @param desc the field descriptor (must not be {@code null})
     * @throws IllegalArgumentException if the class has no such field
     */
    void removeField(FieldDesc desc);

    /**
     * Add an interface to the list of implemented interfaces, if it is not already there.
     *
     * @param interface_ the interface descriptor (must not be {@code null})
     */
    void implements_(ClassDesc interface_);

    /**
     * Add an interface to the list of implemented interfaces, if it is not already there.
     *
     * @param interface_ the interface (must not be {@code null})
     */
    default void implements_(Class<?> interface_) {
        implements_(Util.classDesc(interface_));
    }
}
