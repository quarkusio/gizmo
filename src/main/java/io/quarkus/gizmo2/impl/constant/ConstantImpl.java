package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.util.List;
import java.util.Objects;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Item;
import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.Util;

public abstract non-sealed class ConstantImpl extends Item implements Constant {
    private final ClassDesc type;

    ConstantImpl(final ClassDesc type) {
        this.type = type;
    }

    public static StringConstant of(final String value) {
        return GizmoImpl.current().stringConstant(value);
    }

    public static ConstantImpl of(Constable constable) {
        if (constable instanceof ConstantImpl con) {
            return con;
        } else {
            return of(constable.describeConstable().orElseThrow(IllegalArgumentException::new));
        }
    }

    public static ConstantImpl of(ConstantDesc constantDesc) {
        Objects.requireNonNull(constantDesc, "constantDesc");
        if (constantDesc instanceof Integer val) {
            return of(val);
        } else if (constantDesc instanceof Long val) {
            return of(val);
        } else if (constantDesc instanceof Float val) {
            return of(val);
        } else if (constantDesc instanceof Double val) {
            return of(val);
        } else if (constantDesc instanceof String val) {
            return of(val);
        } else if (constantDesc instanceof ClassDesc val) {
            return of(val);
        } else if (constantDesc instanceof MethodTypeDesc val) {
            return of(val);
        } else if (constantDesc instanceof MethodHandleDesc val) {
            return of(val);
        } else if (constantDesc instanceof DynamicConstantDesc<?> dcd) {
            return of(dcd);
        } else {
            throw new IllegalStateException("Unknown constant desc " + constantDesc);
        }
    }

    public static ConstantImpl of(DynamicConstantDesc<?> dcd) {
        if (dcd instanceof Enum.EnumDesc<?> desc) {
            return of(desc);
        } else if (dcd instanceof VarHandle.VarHandleDesc desc) {
            return of(desc);
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_NULL_CONSTANT)) {
            return ofNull(dcd.constantType());
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_ENUM_CONSTANT)) {
            // "wrong spelling" of enum constant
            return of(Enum.EnumDesc.of(dcd.constantType(), dcd.constantName()));
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_NULL_CONSTANT)) {
            // "wrong spelling" of null constant
            return ofNull(dcd.constantType());
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_PRIMITIVE_CLASS)) {
            // "wrong spelling" of primitive class
            return of(switch (dcd.constantName()) {
                case "byte" -> byte.class;
                case "char" -> char.class;
                case "short" -> short.class;
                case "int" -> int.class;
                case "long" -> long.class;
                case "float" -> float.class;
                case "double" -> double.class;
                case "boolean" -> boolean.class;
                default ->
                    throw new IllegalArgumentException("Invalid primitive type name \"" + dcd.constantName() + "\"");
            });
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_VARHANDLE_FIELD)) {
            // "wrong spelling" of var handle constant
            List<ConstantDesc> args = dcd.bootstrapArgsList();
            return ofFieldVarHandle(FieldDesc.of((ClassDesc) args.get(0), dcd.constantName(), (ClassDesc) args.get(1)));
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_INVOKE)) {
            // "wrong spelling" of invoke constant
            return ofInvoke(of(dcd.bootstrapMethod()), dcd.bootstrapArgsList().stream().map(Constant::of).toList());
        } else {
            return GizmoImpl.current().dynamicConstant(dcd);
        }
    }

    public static NullConstant ofNull(ClassDesc type) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Type is not a reference type: " + type);
        }
        return GizmoImpl.current().nullConstant(type);
    }

    public static NullConstant ofNull(Class<?> type) {
        return ofNull(Util.classDesc(type));
    }

    public static ClassConstant of(ClassDesc value) {
        return GizmoImpl.current().classConstant(value);
    }

    public static ClassConstant of(Class<?> value) {
        return of(Util.classDesc(value));
    }

    public static FieldVarHandleConstant ofFieldVarHandle(FieldDesc desc) {
        return GizmoImpl.current().fieldVarHandleConstant(desc);
    }

    public static StaticFieldVarHandleConstant ofStaticFieldVarHandle(FieldDesc desc) {
        return GizmoImpl.current().staticFieldVarHandleConstant(desc);
    }

    public static StaticFinalFieldConstant ofStaticFinalField(FieldDesc desc) {
        return GizmoImpl.current().staticFinalFieldConstant(desc);
    }

    public static ArrayVarHandleConstant ofArrayVarHandle(ClassDesc arrayType) {
        if (! arrayType.isArray()) {
            throw new IllegalArgumentException("Array var handles can only be created for array types");
        }
        return GizmoImpl.current().arrayVarHandleConstant(arrayType);
    }

    public static ConstantImpl of(VarHandle.VarHandleDesc value) {
        List<ConstantDesc> args = value.bootstrapArgsList();
        return switch (value.bootstrapMethod().methodName()) {
            case "fieldVarHandle" -> ofFieldVarHandle(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
            case "staticFieldVarHandle" -> ofStaticFieldVarHandle(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
            case "getStaticFinal" -> switch (args.size()) {
                case 0 -> ofStaticFinalField(FieldDesc.of(value.varType(), value.constantName(), value.varType()));
                case 1 -> ofStaticFinalField(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
                default -> throw new IllegalArgumentException("Unknown var handle type");
            };
            default -> throw new IllegalArgumentException("Unknown var handle type");
        };
    }

    public static EnumConstant of(Enum.EnumDesc<?> value) {
        return GizmoImpl.current().enumConstant(value);
    }

    public static IntConstant of(Integer value) {
        return GizmoImpl.current().intConstant(value);
    }

    public static IntConstant of(int value) {
        return of(Integer.valueOf(value));
    }

    public static LongConstant of(Long value) {
        return GizmoImpl.current().longConstant(value);
    }

    public static LongConstant of(long value) {
        return of(Long.valueOf(value));
    }

    public static FloatConstant of(Float value) {
        return of(value.floatValue());
    }

    public static FloatConstant of(float value) {
        return GizmoImpl.current().floatConstant(value);
    }

    public static DoubleConstant of(Double value) {
        return of(value.doubleValue());
    }

    public static DoubleConstant of(double value) {
        return GizmoImpl.current().doubleConstant(value);
    }

    public static BooleanConstant of(boolean value) {
        return value ? BooleanConstant.TRUE : BooleanConstant.FALSE;
    }

    public static VoidConstant ofVoid() {
        return VoidConstant.INSTANCE;
    }

    public static ConstantImpl of(int value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case BOOLEAN -> of(value != 0);
            case INT -> of(value);
            case LONG -> of((long) value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast integer constant to " + typeKind);
        };
    }

    public static ConstantImpl of(long value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case BOOLEAN -> of(value != 0);
            case INT -> of((int) value);
            case LONG -> of(value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast integer constant to " + typeKind);
        };
    }

    public static ConstantImpl of(float value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case BOOLEAN -> of(value != 0);
            case INT -> of((int) value);
            case LONG -> of((long) value);
            case FLOAT -> of(value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast integer constant to " + typeKind);
        };
    }

    public static ConstantImpl of(double value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case BOOLEAN -> of(value != 0);
            case INT -> of((int) value);
            case LONG -> of((long) value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of(value);
            default -> throw new IllegalArgumentException("Cannot cast integer constant to " + typeKind);
        };
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static InvokeConstant ofInvoke(Constant handle, List<Constant> args) {
        // we could theoretically use a stream to cast the list properly, but instead let's cheat and save some CPU
        return new InvokeConstant((MethodHandleConstant) handle, (List<ConstantImpl>) (List) args);
    }

    public static MethodHandleConstant of(MethodHandleDesc desc) {
        return new MethodHandleConstant(desc);
    }

    public static MethodHandleConstant ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return new MethodHandleConstant(kind, desc);
    }

    public static MethodHandleConstant ofConstructorMethodHandle(ConstructorDesc desc) {
        return new MethodHandleConstant(desc);
    }

    public static MethodHandleConstant ofFieldSetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConstant(desc, false, false);
    }

    public static MethodHandleConstant ofFieldGetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConstant(desc, false, true);
    }

    public static MethodHandleConstant ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConstant(desc, true, false);
    }

    public static MethodHandleConstant ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConstant(desc, true, true);
    }

    public static MethodTypeConstant of(MethodTypeDesc desc) {
        return new MethodTypeConstant(desc);
    }

    public ClassDesc type() {
        return type;
    }

    public boolean bound() {
        return false;
    }

    public boolean isZero() {
        return false;
    }

    public boolean isNonZero() {
        return false;
    }

    public final boolean equals(final Object obj) {
        return obj instanceof ConstantImpl other && equals(other);
    }

    public abstract boolean equals(ConstantImpl other);

    public abstract int hashCode();

    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        // most implementations are constant table entries no matter what
        cb.ldc(desc());
    }
}
