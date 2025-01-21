package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class IntConstant extends ConstantImpl {
    private final Integer value;

    public IntConstant(Integer value) {
        super(ConstantDescs.CD_int);
        this.value = value;
    }

    public IntConstant(final ConstantDesc constantDesc) {
        this((Integer) constantDesc);
    }

    public int intValue() {
        return value.intValue();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        int unboxed = value.intValue();
        switch (unboxed) {
            case -5 -> {
                cb.iconst_5();
                cb.ineg();
            }
            case -4 -> {
                cb.iconst_4();
                cb.ineg();
            }
            case -3 -> {
                cb.iconst_3();
                cb.ineg();
            }
            case -2 -> {
                cb.iconst_2();
                cb.ineg();
            }
            default -> cb.loadConstant(value);
        }
    }

    public boolean isZero() {
        return value.intValue() == 0;
    }

    public boolean isNonZero() {
        return value.intValue() != 0;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof IntConstant other && equals(other);
    }

    public boolean equals(final IntConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return Integer.hashCode(value.intValue());
    }

    public Integer desc() {
        return value;
    }

    public Optional<Integer> describeConstable() {
        return Optional.of(value);
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(value).append(" (0x").append(Integer.toHexString(value.intValue())).append(')');
    }
}
