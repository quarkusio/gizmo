package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_short;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public final class ShortConstant extends IntBasedConstant {
    private final Short value;

    public ShortConstant(Short value) {
        super(CD_short);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof ShortConstant other && equals(other);
    }

    public boolean equals(final ShortConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public DynamicConstantDesc<Short> desc() {
        return describeConstable().orElseThrow();
    }

    public Optional<DynamicConstantDesc<Short>> describeConstable() {
        return value.describeConstable();
    }
}
