package io.quarkus.gizmo2.creator;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.FieldCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a field.
 */
public sealed interface FieldCreator extends MemberCreator, SimpleTyped permits InstanceFieldCreator, StaticFieldCreator, FieldCreatorImpl {
    ClassDesc type();

    /**
     * {@return the field type descriptor}
     */
    FieldDesc desc();

    /**
     * Change the type of the field to the given generic type.
     *
     * @param type the class type signature (must not be {@code null})
     */
    void withTypeSignature(Signature type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type descriptor (must not be {@code null})
     */
    void withType(ClassDesc type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type (must not be {@code null})
     */
    default void withType(Class<?> type) {
        withType(Util.classDesc(type));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value (must not be {@code null})
     */
    void withInitial(Constant initial);

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(int initial) {
        withInitial(Constant.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(long initial) {
        withInitial(Constant.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(float initial) {
        withInitial(Constant.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(double initial) {
        withInitial(Constant.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(String initial) {
        withInitial(initial == null ? Constant.ofNull(CD_String) : Constant.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void withInitial(Class<?> initial) {
        withInitial(initial == null ? Constant.ofNull(CD_Class) : Constant.of(initial));
    }

    /**
     * Provide an initializer for this field which will be concatenated with the class or instance initializer(s).
     *
     * @param init the builder for the initializer which yields the field initial value (must not be {@code null})
     */
    void withInitializer(Consumer<BlockCreator> init);
}
