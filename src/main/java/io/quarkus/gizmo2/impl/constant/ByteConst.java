package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_byte;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public final class ByteConst extends IntBasedConst {
    private final Byte value;

    public ByteConst(Byte value) {
        super(CD_byte);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof ByteConst other && equals(other);
    }

    public boolean equals(final ByteConst other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public DynamicConstantDesc<Byte> desc() {
        return describeConstable().orElseThrow();
    }

    public Optional<DynamicConstantDesc<Byte>> describeConstable() {
        return value.describeConstable();
    }
}
