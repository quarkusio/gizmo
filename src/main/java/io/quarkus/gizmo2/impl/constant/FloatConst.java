package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.StackMapBuilder;

public final class FloatConst extends ConstImpl {
    private final float value;

    public FloatConst(float value) {
        super(ConstantDescs.CD_float);
        this.value = value;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        int asInt = (int) value;
        if (value == (float) asInt) {
            switch (asInt) {
                case -5 -> {
                    cb.iconst_5();
                    cb.ineg();
                    cb.i2f();
                }
                case -4 -> {
                    cb.iconst_4();
                    cb.ineg();
                    cb.i2f();
                }
                case -3 -> {
                    cb.iconst_3();
                    cb.ineg();
                    cb.i2f();
                }
                case -2 -> {
                    cb.iconst_2();
                    cb.ineg();
                    cb.i2f();
                }
                case -1 -> {
                    cb.iconst_m1();
                    cb.i2f();
                }
                case 0 -> cb.fconst_0();
                case 1 -> cb.fconst_1();
                case 2 -> cb.fconst_2();
                case 3 -> {
                    cb.iconst_3();
                    cb.i2f();
                }
                case 4 -> {
                    cb.iconst_4();
                    cb.i2f();
                }
                case 5 -> {
                    cb.iconst_5();
                    cb.i2f();
                }
                default -> {
                    if (Byte.MIN_VALUE <= asInt && asInt <= Byte.MAX_VALUE) {
                        cb.bipush(asInt);
                        cb.i2f();
                    } else if (Short.MIN_VALUE <= asInt && asInt <= Short.MAX_VALUE) {
                        cb.sipush(asInt);
                        cb.i2f();
                    } else {
                        cb.ldc(Float.valueOf(value));
                    }
                }
            }
        } else {
            cb.ldc(Float.valueOf(value));
        }
        smb.push(type());
        smb.wroteCode();
    }

    public boolean isZero() {
        return value == 0f;
    }

    public boolean isNonZero() {
        return value != 0f;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof FloatConst other && equals(other);
    }

    public boolean equals(final FloatConst other) {
        return this == other || other != null && Float.floatToRawIntBits(value) == Float.floatToRawIntBits(other.value);
    }

    public int hashCode() {
        return Float.floatToRawIntBits(value);
    }

    public Float desc() {
        return Float.valueOf(value);
    }

    public Optional<Float> describeConstable() {
        return Optional.of(Float.valueOf(value));
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(value).append(" (").append(Float.toHexString(value)).append(')');
    }
}
