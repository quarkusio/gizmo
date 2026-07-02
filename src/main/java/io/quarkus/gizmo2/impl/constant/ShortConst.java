package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_short;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import io.quarkus.gizmo2.impl.SourceBuilder;

public final class ShortConst extends IntBasedConst {
    private final Short value;

    public ShortConst(Short value) {
        super(CD_short);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof ShortConst other && equals(other);
    }

    public boolean equals(final ShortConst other) {
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

    /** {@inheritDoc} */
    @Override
    protected StringBuilder appendSourceExpr(StringBuilder buf, SourceBuilder sb) {
        return buf.append("(short) ").append(intValue());
    }
}
