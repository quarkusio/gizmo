package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class LongConstant extends ConstantImpl {
    private final Long value;

    public LongConstant(Long value) {
        super(ConstantDescs.CD_long);
        this.value = value;
    }

    public LongConstant(final ConstantDesc constantDesc) {
        this((Long) constantDesc);
    }

    public long longValue() {
        return value.longValue();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        long unboxed = value.longValue();
        if (Short.MIN_VALUE <= unboxed && unboxed <= Short.MAX_VALUE) {
            int asInt = (int) unboxed;
            switch (asInt) {
                case -5 -> {
                    cb.iconst_5();
                    cb.ineg();
                    cb.i2l();
                }
                case -4 -> {
                    cb.iconst_4();
                    cb.ineg();
                    cb.i2l();
                }
                case -3 -> {
                    cb.iconst_3();
                    cb.ineg();
                    cb.i2l();
                }
                case -2 -> {
                    cb.iconst_2();
                    cb.ineg();
                    cb.i2l();
                }
                case -1 -> {
                    cb.iconst_m1();
                    cb.i2l();
                }
                case 0 -> cb.lconst_0();
                case 1 -> cb.lconst_1();
                case 2 -> {
                    cb.iconst_2();
                    cb.i2l();
                }
                case 3 -> {
                    cb.iconst_3();
                    cb.i2l();
                }
                case 4 -> {
                    cb.iconst_4();
                    cb.i2l();
                }
                case 5 -> {
                    cb.iconst_5();
                    cb.i2l();
                }
                default -> {
                    if (Byte.MIN_VALUE <= unboxed && unboxed <= Byte.MAX_VALUE) {
                        cb.bipush(asInt);
                    } else {
                        cb.sipush(asInt);
                    }
                    cb.i2l();
                }
            }
        } else {
            cb.ldc(Long.valueOf(unboxed));
        }
    }

    public boolean isZero() {
        return value.longValue() == 0;
    }

    public boolean isNonZero() {
        return value.longValue() != 0;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof LongConstant other && equals(other);
    }

    public boolean equals(final LongConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public ConstantDesc desc() {
        return value;
    }

    public Optional<Long> describeConstable() {
        return Optional.of(value);
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(value).append("L (0x").append(Long.toHexString(value.longValue())).append("L)");
    }
}
