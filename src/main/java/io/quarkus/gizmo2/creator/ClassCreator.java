package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ClassCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a class type.
 */
public sealed interface ClassCreator extends TypeCreator permits ClassCreatorImpl {
    /**
     * Extend the given generic class.
     *
     * @param genericType the generic class (must not be {@code null})
     */
    void extends_(Signature.ClassTypeSig genericType);

    /**
     * Extend the given class.
     *
     * @param desc the class (must not be {@code null})
     */
    void extends_(ClassDesc desc);

    /**
     * Extend the given class.
     *
     * @param clazz the class (must not be {@code null})
     */
    default void extends_(Class<?> clazz) {
        if (clazz.isInterface()) {
            throw new IllegalArgumentException("Classes may only extend classes");
        }
        extends_(Util.classDesc(clazz));
    }

    /**
     * Add an instance field to this class.
     *
     * @param name the field name (must not be {@code null})
     * @param builder the builder (must not be {@code null})
     * @return the field variable (not {@code null})
     */
    FieldDesc field(String name, Consumer<InstanceFieldCreator> builder);

    /**
     * Add an instance method to the class.
     * The builder accepts the method builder plus the {@code this} expression for the method.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add an abstract instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc abstractMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc nativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native static method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc staticNativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a constructor to the class.
     *
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    ConstructorDesc constructor(Consumer<ConstructorCreator> builder);

    /**
     * Add the {@code abstract} access flag to the class.
     */
    void abstract_();

    /**
     * Add the {@code final} access flag to the class.
     */
    void final_();

}
