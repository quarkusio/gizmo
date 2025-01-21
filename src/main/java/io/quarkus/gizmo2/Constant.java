package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.util.List;

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

/**
 * An expression which wraps a {@link ConstantDesc}.
 */
public sealed interface Constant extends Expr, Constable permits ConstantImpl {
    ConstantDesc desc();

    static Constant of(Constable constable) {
        return ConstantImpl.of(constable);
    }

    static Constant of(ConstantDesc constantDesc) {
        return ConstantImpl.of(constantDesc);
    }

    static Constant of(DynamicConstantDesc<?> dcd) {
        return ConstantImpl.of(dcd);
    }

    static Constant ofNull(ClassDesc type) {
        return ConstantImpl.of(type);
    }

    static Constant ofNull(Class<?> type) {
        return ConstantImpl.of(type);
    }

    static Constant of(ClassDesc value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Class<?> value) {
        return ConstantImpl.of(value);
    }

    static Constant of(VarHandle.VarHandleDesc value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Enum.EnumDesc<?> value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Integer value) {
        return ConstantImpl.of(value);
    }

    static Constant of(int value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Long value) {
        return ConstantImpl.of(value);
    }

    static Constant of(long value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Float value) {
        return ConstantImpl.of(value);
    }

    static Constant of(float value) {
        return ConstantImpl.of(value);
    }

    static Constant of(Double value) {
        return ConstantImpl.of(value);
    }

    static Constant of(double value) {
        return ConstantImpl.of(value);
    }

    static Constant of(boolean value) {
        return ConstantImpl.of(value);
    }

    static Constant of(int value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    static Constant of(long value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    static Constant of(float value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    static Constant of(double value, TypeKind typeKind) {
        return ConstantImpl.of(value, typeKind);
    }

    static Constant of(String value) {
        return ConstantImpl.of(value);
    }

    static Constant ofVoid() {
        return ConstantImpl.ofVoid();
    }

    static Constant ofFieldVarHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldVarHandle(desc);
    }

    static Constant ofStaticFieldVarHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldVarHandle(desc);
    }

    static Constant ofStaticFinalField(FieldDesc desc) {
        return ConstantImpl.ofStaticFinalField(desc);
    }

    static Constant ofArrayVarHandle(ClassDesc arrayType) {
        return ConstantImpl.ofArrayVarHandle(arrayType);
    }

    static Constant ofInvoke(Constant methodHandle, List<Constant> args) {
        return ConstantImpl.ofInvoke(methodHandle, args);
    }

    static Constant ofInvoke(Constant methodHandle, Constant... args) {
        return ConstantImpl.ofInvoke(methodHandle, List.of(args));
    }

    static Constant of(MethodHandleDesc desc) {
        return ConstantImpl.of(desc);
    }

    static Constant ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return ConstantImpl.ofMethodHandle(kind, desc);
    }

    static Constant ofConstructorMethodHandle(ConstructorDesc desc) {
        return ConstantImpl.ofConstructorMethodHandle(desc);
    }

    static Constant ofFieldSetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldSetterMethodHandle(desc);
    }

    static Constant ofFieldGetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofFieldGetterMethodHandle(desc);
    }

    static Constant ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldSetterMethodHandle(desc);
    }

    static Constant ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return ConstantImpl.ofStaticFieldGetterMethodHandle(desc);
    }

    static Constant of(MethodTypeDesc desc) {
        return ConstantImpl.of(desc);
    }
}
