package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import static java.lang.constant.ConstantDescs.CD_char;

public final class CharConstant extends IntBasedConstant {
    private final Character value;

    public CharConstant(Character value) {
        super(CD_char);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.charValue();
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof CharConstant other && equals(other);
    }

    public boolean equals(final CharConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public DynamicConstantDesc<Character> desc() {
        return describeConstable().orElseThrow();
    }

    public Optional<DynamicConstantDesc<Character>> describeConstable() {
        return value.describeConstable();
    }
}
