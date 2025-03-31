package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ClassCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a class type.
 */
public sealed interface ClassCreator extends TypeCreator, SimpleTyped permits AnonymousClassCreator, ClassCreatorImpl {
    /**
     * {@return the superclass}
     * @see #extends_(ClassDesc)
     */
    ClassDesc superClass();

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
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc method(String name, Consumer<InstanceMethodCreator> builder);

    /**
     * Add an instance method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type    the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(String name, MethodTypeDesc type, Consumer<InstanceMethodCreator> builder) {
        return method(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add an instance method to the class having the same name and type as the given method.
     *
     * @param desc    the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc method(MethodDesc desc, Consumer<InstanceMethodCreator> builder) {
        return method(desc.name(), desc.type(), builder);
    }

    /**
     * Add an abstract instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc abstractMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add an abstract instance method to the class having the given predefined type.
     *
     * @param name the method name (must not be {@code null})
     * @param type    the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc abstractMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return abstractMethod(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add an abstract instance method to the class having the same name and type as the given method.
     *
     * @param desc    the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc abstractMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return abstractMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a native instance method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc nativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native instance method to the class having the given predefined type.
     *
     * @param name    the method name (must not be {@code null})
     * @param type    the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc nativeMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return nativeMethod(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a native instance method to the class having the same name and type as the given method.
     *
     * @param desc    the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc nativeMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return nativeMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a native static method to the class.
     *
     * @param name the method name (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    MethodDesc staticNativeMethod(String name, Consumer<AbstractMethodCreator> builder);

    /**
     * Add a native static method to the class having the given predefined type.
     *
     * @param name    the method name (must not be {@code null})
     * @param type    the method type (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc staticNativeMethod(String name, MethodTypeDesc type, Consumer<AbstractMethodCreator> builder) {
        return staticNativeMethod(name, imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a native static method to the class having the same name and type as the given method.
     *
     * @param desc    the original method descriptor (must not be {@code null})
     * @param builder the method builder (must not be {@code null})
     * @return the built method's selector for invocation (not {@code null})
     */
    default MethodDesc staticNativeMethod(MethodDesc desc, Consumer<AbstractMethodCreator> builder) {
        return staticNativeMethod(desc.name(), desc.type(), builder);
    }

    /**
     * Add a constructor to the class.
     *
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    ConstructorDesc constructor(Consumer<ConstructorCreator> builder);

    /**
     * Add a constructor to the class having the given predefined type.
     * The type must have a {@code void} return type.
     *
     * @param type    the method type (must not be {@code null})
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc constructor(MethodTypeDesc type, Consumer<ConstructorCreator> builder) {
        return constructor(imc -> {
            imc.withType(type);
            builder.accept(imc);
        });
    }

    /**
     * Add a constructor to the class having the same type as the given constructor.
     *
     * @param desc    the original constructor descriptor (must not be {@code null})
     * @param builder the constructor builder (must not be {@code null})
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc constructor(ConstructorDesc desc, Consumer<ConstructorCreator> builder) {
        return constructor(desc.type(), builder);
    }

    /**
     * Add a default constructor to this class.
     *
     * @return the built constructor's selector for invocation (must not be {@code null})
     */
    default ConstructorDesc defaultConstructor() {
        ConstructorDesc superCtor = ConstructorDesc.of(superClass());
        return constructor(superCtor, cc -> {
            cc.public_();
            Var this_ = cc.this_();
            cc.body(bc -> {
                bc.invokeSpecial(superCtor, this_);
                bc.return_();
            });
        });
    }

    /**
     * Add the {@code abstract} access flag to the class.
     */
    void abstract_();

    /**
     * Add the {@code final} access flag to the class.
     */
    void final_();

}
