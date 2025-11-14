package io.quarkus.gizmo2.impl.constant;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Item;
import io.quarkus.gizmo2.impl.StackMapBuilder;
import io.quarkus.gizmo2.impl.Util;

public abstract non-sealed class ConstImpl extends Item implements Const {
    ConstImpl(final ClassDesc type) {
        super(type);
    }

    public static StringConst of(final String value) {
        return new StringConst(value);
    }

    public static ConstImpl of(Constable constable) {
        checkNotNullParam("constable", constable);
        if (constable instanceof ConstImpl con) {
            return con;
        } else if (constable instanceof Boolean val) {
            return of(val);
        } else if (constable instanceof Byte val) {
            return of(val);
        } else if (constable instanceof Short val) {
            return of(val);
        } else if (constable instanceof Character val) {
            return of(val);
        } else {
            return of(constable.describeConstable().orElseThrow(IllegalArgumentException::new));
        }
    }

    public static ConstImpl of(ConstantDesc constantDesc) {
        checkNotNullParam("constantDesc", constantDesc);
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

    public static ConstImpl of(DynamicConstantDesc<?> dcd) {
        if (dcd instanceof Enum.EnumDesc<?> desc) {
            return of(desc);
        } else if (dcd instanceof VarHandle.VarHandleDesc desc) {
            return of(desc);
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
            return ofInvoke(of(dcd.bootstrapMethod()), dcd.bootstrapArgsList().stream().map(Const::of).toList());
        } else if (dcd.bootstrapMethod().equals(ConstantDescs.BSM_EXPLICIT_CAST)
                && dcd.constantName().equals(ConstantDescs.DEFAULT_NAME)) {
            // "wrong spelling" of primitive constant
            if (dcd.constantType().equals(ConstantDescs.CD_byte)) {
                return of(((Integer) dcd.bootstrapArgs()[0]).byteValue());
            } else if (dcd.constantType().equals(ConstantDescs.CD_short)) {
                return of(((Integer) dcd.bootstrapArgs()[0]).shortValue());
            } else if (dcd.constantType().equals(ConstantDescs.CD_char)) {
                return of((char) ((Integer) dcd.bootstrapArgs()[0]).intValue());
            } else {
                // primitive constants of other types don't reach here
                return new DynamicConst(dcd);
            }
        } else {
            return new DynamicConst(dcd);
        }
    }

    public static NullConst ofNull(ClassDesc type) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Type is not a reference type: " + type);
        }
        return new NullConst(type);
    }

    public static NullConst ofNull(Class<?> type) {
        return ofNull(Util.classDesc(type));
    }

    public static ClassConst of(ClassDesc value) {
        return new ClassConst(value);
    }

    public static ClassConst of(Class<?> value) {
        return of(Util.classDesc(value));
    }

    public static FieldVarHandleConst ofFieldVarHandle(FieldDesc desc) {
        return new FieldVarHandleConst(desc);
    }

    public static StaticFieldVarHandleConst ofStaticFieldVarHandle(FieldDesc desc) {
        return new StaticFieldVarHandleConst(desc);
    }

    public static StaticFinalFieldConst ofStaticFinalField(FieldDesc desc) {
        return new StaticFinalFieldConst(desc);
    }

    public static ArrayVarHandleConst ofArrayVarHandle(ClassDesc arrayType) {
        if (!arrayType.isArray()) {
            throw new IllegalArgumentException("Array var handles can only be created for array types");
        }
        return new ArrayVarHandleConst(arrayType);
    }

    public static ConstImpl of(VarHandle.VarHandleDesc value) {
        List<ConstantDesc> args = value.bootstrapArgsList();
        return switch (value.bootstrapMethod().methodName()) {
            case "fieldVarHandle" ->
                ofFieldVarHandle(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
            case "staticFieldVarHandle" ->
                ofStaticFieldVarHandle(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
            case "getStaticFinal" -> switch (args.size()) {
                case 0 -> ofStaticFinalField(FieldDesc.of(value.varType(), value.constantName(), value.varType()));
                case 1 -> ofStaticFinalField(FieldDesc.of((ClassDesc) args.get(0), value.constantName(), value.varType()));
                default -> throw new IllegalArgumentException("Unknown var handle type");
            };
            default -> throw new IllegalArgumentException("Unknown var handle type");
        };
    }

    public static EnumConst of(Enum.EnumDesc<?> value) {
        return new EnumConst(value);
    }

    public static ByteConst of(Byte value) {
        return new ByteConst(value);
    }

    public static ByteConst of(byte value) {
        return of(Byte.valueOf(value));
    }

    public static ShortConst of(Short value) {
        return new ShortConst(value);
    }

    public static ShortConst of(short value) {
        return of(Short.valueOf(value));
    }

    public static CharConst of(Character value) {
        return new CharConst(value);
    }

    public static CharConst of(char value) {
        return of(Character.valueOf(value));
    }

    public static IntConst of(Integer value) {
        return new IntConst(value);
    }

    public static IntConst of(int value) {
        return of(Integer.valueOf(value));
    }

    public static LongConst of(Long value) {
        return new LongConst(value);
    }

    public static LongConst of(long value) {
        return of(Long.valueOf(value));
    }

    public static FloatConst of(Float value) {
        return of(value.floatValue());
    }

    public static FloatConst of(float value) {
        return new FloatConst(value);
    }

    public static DoubleConst of(Double value) {
        return of(value.doubleValue());
    }

    public static DoubleConst of(double value) {
        return new DoubleConst(value);
    }

    public static BooleanConst of(Boolean value) {
        return value.booleanValue() ? BooleanConst.TRUE : BooleanConst.FALSE;
    }

    public static BooleanConst of(boolean value) {
        return value ? BooleanConst.TRUE : BooleanConst.FALSE;
    }

    public static VoidConst ofVoid() {
        return VoidConst.INSTANCE;
    }

    public static ConstImpl of(int value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case INT -> of(value);
            case LONG -> of((long) value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast integer constant to " + typeKind);
        };
    }

    public static ConstImpl of(long value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case INT -> of((int) value);
            case LONG -> of(value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast long constant to " + typeKind);
        };
    }

    public static ConstImpl of(float value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case INT -> of((int) value);
            case LONG -> of((long) value);
            case FLOAT -> of(value);
            case DOUBLE -> of((double) value);
            default -> throw new IllegalArgumentException("Cannot cast float constant to " + typeKind);
        };
    }

    public static ConstImpl of(double value, TypeKind typeKind) {
        return switch (typeKind.asLoadable()) {
            case INT -> of((int) value);
            case LONG -> of((long) value);
            case FLOAT -> of((float) value);
            case DOUBLE -> of(value);
            default -> throw new IllegalArgumentException("Cannot cast double constant to " + typeKind);
        };
    }

    public static InvokeConst ofInvoke(Const handle, List<Const> args) {
        return new InvokeConst((MethodHandleConst) handle, Util.reinterpretCast(args));
    }

    public static MethodHandleConst of(MethodHandleDesc desc) {
        return new MethodHandleConst(desc, null);
    }

    public static MethodHandleConst ofMethodHandle(InvokeKind kind, MethodDesc desc) {
        return new MethodHandleConst(kind, desc);
    }

    public static MethodHandleConst ofConstructorMethodHandle(ConstructorDesc desc) {
        return new MethodHandleConst(desc);
    }

    public static MethodHandleConst ofFieldSetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConst(desc, false, false);
    }

    public static MethodHandleConst ofFieldGetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConst(desc, false, true);
    }

    public static MethodHandleConst ofStaticFieldSetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConst(desc, true, false);
    }

    public static MethodHandleConst ofStaticFieldGetterMethodHandle(FieldDesc desc) {
        return new MethodHandleConst(desc, true, true);
    }

    public static MethodTypeConst of(MethodTypeDesc desc) {
        return new MethodTypeConst(desc);
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
        return obj instanceof ConstImpl other && equals(other);
    }

    public abstract boolean equals(ConstImpl other);

    public abstract int hashCode();

    public void writeCode(CodeBuilder cb, BlockCreatorImpl block, final StackMapBuilder smb) {
        // most implementations are constant table entries no matter what
        cb.ldc(desc());
        smb.push(type());
        smb.wroteCode();
    }
}
