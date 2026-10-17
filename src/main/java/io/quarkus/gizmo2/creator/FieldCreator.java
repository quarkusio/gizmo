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
    void type(GenericType type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type descriptor (must not be {@code null})
     */
    void type(ClassDesc type);

    /**
     * Change the type of the field to the given type.
     *
     * @param type the class type (must not be {@code null})
     */
    default void type(Class<?> type) {
        type(Util.classDesc(type));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value (must not be {@code null})
     */
    void initial(Const initial);

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(int initial) {
        initial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(long initial) {
        initial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(float initial) {
        initial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(double initial) {
        initial(Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(String initial) {
        initial(initial == null ? Const.ofNull(CD_String) : Const.of(initial));
    }

    /**
     * Provide an initial constant value for this field.
     *
     * @param initial the initial value
     */
    default void initial(Class<?> initial) {
        initial(initial == null ? Const.ofNull(CD_Class) : Const.of(initial));
    }

    /**
     * Provide an initializer for this field which will be concatenated with the class or instance initializer(s).
     *
     * @param init the builder for the initializer which yields the field initial value (must not be {@code null})
     */
    void initializer(Consumer<BlockCreator> init);

    /**
     * Add the {@code volatile} modifier flag to this creator.
     */
    default void volatile_() {
        addFlag(ModifierFlag.VOLATILE);
    }

    /**
     * @deprecated Use {@link #type(GenericType)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(GenericType type) {
        type(type);
    }

    /**
     * @deprecated Use {@link #type(ClassDesc)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(ClassDesc type) {
        type(type);
    }

    /**
     * @deprecated Use {@link #type(Class)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setType(Class<?> type) {
        type(type);
    }

    /**
     * @deprecated Use {@link #initial(Const)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(Const initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(int)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(int initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(long)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(long initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(float)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(float initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(double)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(double initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(String)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(String initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initial(Class)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitial(Class<?> initial) {
        initial(initial);
    }

    /**
     * @deprecated Use {@link #initializer(Consumer)} instead.
     */
    @Deprecated(since = "2.4", forRemoval = true)
    default void setInitializer(Consumer<BlockCreator> init) {
        initializer(init);
    }
}
