package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.ConstructorDesc;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.impl.ClassCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a class type.
 */
public sealed interface ClassCreator extends TypeCreator permits ClassCreatorImpl {
    void extends_(Signature.ClassTypeSig genericType);

    void extends_(ClassDesc desc);

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
     * @param name    the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<InstanceMethodCreator> builder);

    MethodDesc abstractMethod(String name, Consumer<AbstractMethodCreator> builder);

    MethodDesc nativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    MethodDesc staticNativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    ConstructorDesc constructor(Consumer<ConstructorCreator> builder);
}
