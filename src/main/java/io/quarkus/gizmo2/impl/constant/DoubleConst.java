package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.StackMapBuilder;

public final class DoubleConst extends ConstImpl {
    private final double value;

    public DoubleConst(double value) {
        super(ConstantDescs.CD_double);
        this.value = value;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        long asLong = (long) value;
        int asInt = (int) value;
        if (asInt == asLong && value == (double) asLong) {
            switch (asInt) {
                case -5 -> {
                    cb.iconst_5();
                    cb.ineg();
                    cb.i2d();
                }
                case -4 -> {
                    cb.iconst_4();
                    cb.ineg();
                    cb.i2d();
                }
                case -3 -> {
                    cb.iconst_3();
                    cb.ineg();
                    cb.i2d();
                }
                case -2 -> {
                    cb.iconst_2();
                    cb.ineg();
                    cb.i2d();
                }
                case -1 -> {
                    cb.iconst_m1();
                    cb.i2d();
                }
                case 0 -> cb.dconst_0();
                case 1 -> cb.dconst_1();
                case 2 -> {
                    cb.iconst_2();
                    cb.i2d();
                }
                case 3 -> {
                    cb.iconst_3();
                    cb.i2d();
                }
                case 4 -> {
                    cb.iconst_4();
                    cb.i2d();
                }
                case 5 -> {
                    cb.iconst_5();
                    cb.i2d();
                }
                default -> {
                    if (Byte.MIN_VALUE <= asInt && asInt <= Byte.MAX_VALUE) {
                        cb.bipush(asInt);
                        cb.i2d();
                    } else if (Short.MIN_VALUE <= asInt && asInt <= Short.MAX_VALUE) {
                        cb.sipush(asInt);
                        cb.i2d();
                    } else {
                        cb.ldc(Double.valueOf(value));
                    }
                }
            }
        } else {
            cb.ldc(Double.valueOf(value));
        }
        smb.push(type());
        smb.wroteCode();
    }

    public boolean isZero() {
        return value == 0d;
    }

    public boolean isNonZero() {
        return value != 0d;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof DoubleConst other && equals(other);
    }

    public boolean equals(final DoubleConst other) {
        return this == other || other != null && Double.doubleToRawLongBits(value) == Double.doubleToRawLongBits(other.value);
    }

    public int hashCode() {
        return Long.hashCode(Double.doubleToRawLongBits(value));
    }

    public Double desc() {
        return Double.valueOf(value);
    }

    public Optional<Double> describeConstable() {
        return Optional.of(Double.valueOf(value));
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(value).append(" (").append(Double.toHexString(value)).append(')');
    }
}
