package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_char;

import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public final class CharConst extends IntBasedConst {
    private final Character value;

    public CharConst(Character value) {
        super(CD_char);
        this.value = value;
    }

    @Override
    public int intValue() {
        return value.charValue();
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof CharConst other && equals(other);
    }

    public boolean equals(final CharConst other) {
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
