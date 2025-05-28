package io.quarkus.gizmo2.creator;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.GenericTyped;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.FieldCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for a field.
 */
public sealed interface FieldCreator extends MemberCreator, GenericTyped
        permits InstanceFieldCreator, StaticFieldCreator, FieldCreatorImpl {
    ClassDesc type();

    /**
     * {@return the field type descriptor}
     */
    FieldDesc desc();

    /**
     * Change the type of the field to the given type.
     *
     * @param type the generic type (must not be {@code null})
     */
    void setType(GenericType type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type descriptor (must not be {@code null})
     */
    void setType(ClassDesc type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type (must not be {@code null})
     */
    default void setType(Class<?> type) {
        setType(Util.classDesc(type));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value (must not be {@code null})
     */
    void setInitial(Const initial);

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(int initial) {
        setInitial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(long initial) {
        setInitial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(float initial) {
        setInitial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(double initial) {
        setInitial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(String initial) {
        setInitial(initial == null ? Const.ofNull(CD_String) : Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void setInitial(Class<?> initial) {
        setInitial(initial == null ? Const.ofNull(CD_Class) : Const.of(initial));
    }

    /**
     * Provide an initializer for this field which will be concatenated with the class or instance initializer(s).
     *
     * @param init the builder for the initializer which yields the field initial value (must not be {@code null})
     */
    void setInitializer(Consumer<BlockCreator> init);
}
