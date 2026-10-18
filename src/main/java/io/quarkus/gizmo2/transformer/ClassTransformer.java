package io.quarkus.gizmo2.transformer;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.This;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ClassTransformerImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A transformer for an existing class or interface.
 * <p>
 * Changes to existing members are recorded while the builder runs and are applied afterwards in a single pass.
 * The code of existing methods is copied unchanged.
 * New members are added with the usual creators; their builders run immediately.
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

    // adding members

    /**
     * Add a new instance field.
     * Field initializers are not supported.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the field builder (must not be {@code null})
     * @return the descriptor of the new field (not {@code null})
     * @throws IllegalArgumentException if the class is an interface or its class file version is older than Java 17
     */
    FieldDesc field(String name, Consumer<InstanceFieldCreator> builder);

    /**
     * Add a new static field.
     * Field initializers are not supported, but constant initial values are.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the field builder (must not be {@code null})
     * @return the new field variable (not {@code null})
     * @throws IllegalArgumentException if the class file version is older than Java 17
     */
    StaticFieldVar staticField(String name, Consumer<StaticFieldCreator> builder);

    /**
     * Add a new instance method.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     * @throws IllegalArgumentException if the class is an interface or its class file version is older than Java 17
     */
    MethodDesc method(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add a new static method.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the descriptor of the new method (not {@code null})
     * @throws IllegalArgumentException if the class file version is older than Java 17
     */
    MethodDesc staticMethod(String name, Consumer<StaticMethodCreator> builder);

    /**
     * Add a new constructor.
     *
     * @param builder the constructor builder (must not be {@code null})
     * @return the descriptor of the new constructor (not {@code null})
     * @throws IllegalArgumentException if the class is an interface or its class file version is older than Java 17
     */
    ConstructorDesc constructor(Consumer<ConstructorCreator> builder);

    /**
     * {@return the {@code this} expression, for use in the bodies of added instance methods and constructors}
     */
    This this_();
}
