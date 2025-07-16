package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

public final class IntConst extends IntBasedConst {
    private final Integer value;

    public IntConst(Integer value) {
        super(ConstantDescs.CD_int);
        this.value = value;
    }

    public IntConst(final ConstantDesc constantDesc) {
        this((Integer) constantDesc);
    }

    public int intValue() {
        return value.intValue();
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof IntConst other && equals(other);
    }

    public boolean equals(final IntConst other) {
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
