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
import io.quarkus.gizmo2.impl.Util;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

/**
 * An expression which wraps a {@link ConstantDesc}.
 */
public sealed interface Const extends Expr, Constable permits ConstImpl {
    /**
     * {@return the description of the constant}
     */
    ConstantDesc desc();

    /**
     * {@return a constant for the given Constable}
     *
     * @param constable the object to create a constant from (must not be {@code null})
     */
    static Const of(Constable constable) {
        return ConstImpl.of(constable);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param constantDesc the object to create a constant from (must not be {@code null})
     */
    static Const of(ConstantDesc constantDesc) {
        return ConstImpl.of(constantDesc);
    }

    /**
     * {@return a constant for the given description}
     *
     * @param dcd the object to create a constant from (must not be {@code null})
     */
    static Const of(DynamicConstantDesc<?> dcd) {
        return ConstImpl.of(dcd);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Const ofNull(ClassDesc type) {
        return ConstImpl.ofNull(type);
    }

    /**
     * {@return a {@code null} constant with the given type}
     *
     * @param type the type of the null constant (used for inference of type) (must not be {@code null})
     */
    static Const ofNull(Class<?> type) {
        return ConstImpl.ofNull(type);
    }

    /**
     * {@return a constant with the default value of given {@code type}}
     * This is zero for primitive types and {@code null} for reference types.
     *
     * @param type the type of the constant (must not be {@code null})
     */
    static Const ofDefault(ClassDesc type) {
        return switch (type.descriptorString()) {
            case "B" -> of((byte) 0);
            case "S" -> of((short) 0);
            case "C" -> of('\0');
            case "I" -> of(0);
            case "J" -> of(0L);
            case "F" -> of(0.0F);
            case "D" -> of(0.0);
            case "Z" -> of(false);
            case "V" -> ofVoid();
            default -> ofNull(type);
        };
    }

    /**
     * {@return a constant with the default value of given {@code type}}
     * This is zero for primitive types and {@code null} for reference types.
     *
     * @param type the type of the constant (must not be {@code null})
     */
    static Const ofDefault(Class<?> type) {
        return ofDefault(Util.classDesc(type));
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(ClassDesc value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Class<?> value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(VarHandle.VarHandleDesc value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Enum.EnumDesc<?> value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Byte value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(byte value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Short value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(short value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Character value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(char value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Integer value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(int value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Long value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(long value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Float value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(float value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(Double value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(double value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(Boolean value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     */
    static Const of(boolean value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(int value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(long value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(float value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from
     * @param typeKind the (numeric) kind of value to use for inference (must not be {@code null})
     */
    static Const of(double value, TypeKind typeKind) {
        return ConstImpl.of(value, typeKind);
    }

    /**
     * {@return a constant for the given value}
     *
     * @param value the value to create a constant from (must not be {@code null})
     */
    static Const of(String value) {
        return ConstImpl.of(value);
    }

    /**
     * {@return the {@code void} constant}
     */
    static Const ofVoid() {
        return ConstImpl.ofVoid();
    }

    /**
     * {@return a {@code VarHandle} constant for the given instance field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofFieldVarHandle(FieldDesc desc) {
        return ConstImpl.ofFieldVarHandle(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given static field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofStaticFieldVarHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldVarHandle(desc);
    }

    /**
     * {@return a constant with the value of the given {@code static final} field}
     *
     * @param desc the descriptor of the field (must not be {@code null})
     */
    static Const ofStaticFinalField(FieldDesc desc) {
        return ConstImpl.ofStaticFinalField(desc);
    }

    /**
     * {@return a {@code VarHandle} constant for the given array type}
     *
     * @param arrayType the array type (must not be {@code null})
     */
    static Const ofArrayVarHandle(ClassDesc arrayType) {
        return ConstImpl.ofArrayVarHandle(arrayType);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Const ofInvoke(Const methodHandle, List<Const> args) {
        return ConstImpl.ofInvoke(methodHandle, args);
    }

    /**
     * {@return a constant whose value is the result of a one-time method invocation}
     * Note that due to the nature of dynamic constants,
     * parallel threads may cause the method to be invoked more than once.
     *
     * @param methodHandle the method handle to invoke (must not be {@code null})
     * @param args the list of arguments (must not be {@code null})
     */
    static Const ofInvoke(Const methodHandle, Const... args) {
        return ConstImpl.ofInvoke(methodHandle, List.of(args));
    }

    /**
     * {@return a method handle constant from the given descriptor}
     *
     * @param desc the method handle descriptor (must not be {@code null})
     */
    static Const of(MethodHandleDesc desc) {
        return ConstImpl.of(desc);
    }

    /**
     * {@return a method handle constant from the given information}
     *
     * @param kind the invocation kind (must not be {@code null})
     * @param desc the method's descriptor (must not be {@code null})
     */
    static Const ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return ConstImpl.ofMethodHandle(kind, desc);
    }

    /**
     * {@return a method handle constant from the given constructor descriptor}
     *
     * @param desc the constructor descriptor (must not be {@code null})
     */
    static Const ofConstructorMethodHandle(ConstructorDesc desc) {
        return ConstImpl.ofConstructorMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofFieldSetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for an instance field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofFieldGetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field setter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldSetterMethodHandle(desc);
    }

    /**
     * {@return a method handle for a static field getter}
     *
     * @param desc the field descriptor (must not be {@code null})
     */
    static Const ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return ConstImpl.ofStaticFieldGetterMethodHandle(desc);
    }

    /**
     * {@return a method type constant from the given descriptor}
     *
     * @param desc the method type descriptor (must not be {@code null})
     */
    static Const of(MethodTypeDesc desc) {
        return ConstImpl.of(desc);
    }
}
