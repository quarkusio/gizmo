package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.CD_byte;

public class ByteConstant extends IntBasedConstant {
    private final Byte value;

    public ByteConstant(Byte value) {
        super(CD_byte);
        this.value = value;
    }

    @Override
    int intValue() {
        return value;
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
        return describeConstable().get();
    }

    public Optional<DynamicConstantDesc<Byte>> describeConstable() {
        return value.describeConstable();
    }
}
