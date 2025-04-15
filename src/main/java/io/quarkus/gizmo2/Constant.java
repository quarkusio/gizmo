package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.util.List;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

/**
 * An expression which wraps a {@link ConstantDesc}.
 */
public sealed interface Constant extends Expr, Constable permits ConstantImpl {
    /**
     * {@return the description of the constant}
     */
    ConstantDesc desc();

    /**
     * {@return a constant for the given Constable}
     *
     * @param constable the object to create a constant from (must not be {@code null})
     */
    static Constant of(Constable constable) {
        return ConstantImpl.of(constable);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param constantDesc the object to create a constant from (must not be {@code null})
     */
    static Constant of(ConstantDesc constantDesc) {
        return ConstantImpl.of(constantDesc);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param dcd the object to create a constant from (must not be {@code null})
     */
    static Constant of(DynamicConstantDesc<?> dcd) {
        return ConstantImpl.of(dcd);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Constant ofNull(ClassDesc type) {
        return ConstantImpl.ofNull(type);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Constant ofNull(Class<?> type) {
        return ConstantImpl.ofNull(type);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(ClassDesc value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Class<?> value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(VarHandle.VarHandleDesc value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Enum.EnumDesc<?> value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Byte value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(byte value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Short value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(short value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Character value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(char value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Integer value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(int value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Long value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(long value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Float value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(float value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(Double value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(double value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(Boolean value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Constant of(boolean value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Constant of(int value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Constant of(long value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Constant of(float value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Constant of(double value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Constant of(String value) {
        return ConstantImpl.of(value);
    }

    /**
     * {@return the {@code void} constant}
     */
    static Constant ofVoid() {
        return ConstantImpl.ofVoid();
    }

    /**
     * {@return a {@code VarHandle} constant for the given instance field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Constant ofFieldVarHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldVarHandle(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given static field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Constant ofStaticFieldVarHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldVarHandle(desc);
    }

    /**
     * {@return a constant with the value of the given {@code static final} field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Constant ofStaticFinalField(FieldDesc desc) {
        return ConstantImpl.ofStaticFinalField(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given array type}
     *
     * @param arrayType the array type (must not be {@code null})
     */
    static Constant ofArrayVarHandle(ClassDesc arrayType) {
        return ConstantImpl.ofArrayVarHandle(arrayType);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Constant ofInvoke(Constant methodHandle, List<Constant> args) {
        return ConstantImpl.ofInvoke(methodHandle, args);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Constant ofInvoke(Constant methodHandle, Constant... args) {
        return ConstantImpl.ofInvoke(methodHandle, List.of(args));
    }

    /**
     * {@return a method handle constant from the given descriptor}
     *
     * @param desc the method handle descriptor (must not be {@code null})
     */
    static Constant of(MethodHandleDesc desc) {
        return ConstantImpl.of(desc);
    }

    /**
     * {@return a method handle constant from the given information}
     *
     * @param kind the invocation kind (must not be {@code null})
     * @param desc the method's descriptor (must not be {@code null})
     */
    static Constant ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return ConstantImpl.ofMethodHandle(kind, desc);
    }

    /**
     * {@return a method handle constant from the given constructor descriptor}
     *
     * @param desc the constructor descriptor (must not be {@code null})
     */
    static Constant ofConstructorMethodHandle(ConstructorDesc desc) {
        return ConstantImpl.ofConstructorMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Constant ofFieldSetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Constant ofFieldGetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Constant ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Constant ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method type constant from the given descriptor}
     *
     * @param desc the method type descriptor (must not be {@code null})
     */
    static Constant of(MethodTypeDesc desc) {
        return ConstantImpl.of(desc);
    }
}
