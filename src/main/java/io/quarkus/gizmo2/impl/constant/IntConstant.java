package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

public final class IntConstant extends IntBasedConstant {
    private final Integer value;

    public IntConstant(Integer value) {
        super(ConstantDescs.CD_int);
        this.value = value;
    }

    public IntConstant(final ConstantDesc constantDesc) {
        this((Integer) constantDesc);
    }

    public int intValue() {
        return value;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof IntConstant other && equals(other);
    }

    public boolean equals(final IntConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public Integer desc() {
        return value;
    }

    public Optional<Integer> describeConstable() {
        return Optional.of(value);
    }
}
