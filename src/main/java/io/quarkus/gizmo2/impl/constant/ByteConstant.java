package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_byte;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public final class ByteConstant extends IntBasedConstant {
    private final Byte value;

    public ByteConstant(Byte value) {
        super(CD_byte);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.intValue();
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof ByteConstant other && equals(other);
    }

    public boolean equals(final ByteConstant other) {
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
