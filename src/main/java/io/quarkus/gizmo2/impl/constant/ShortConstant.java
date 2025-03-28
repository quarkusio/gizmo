package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.CD_short;

public class ShortConstant extends IntBasedConstant {
    private final Short value;

    public ShortConstant(Short value) {
        super(CD_short);
        this.value = value;
    }

    @Override
    int intValue() {
        return value;
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
        return describeConstable().get();
    }

    public Optional<DynamicConstantDesc<Short>> describeConstable() {
        return value.describeConstable();
    }
}
