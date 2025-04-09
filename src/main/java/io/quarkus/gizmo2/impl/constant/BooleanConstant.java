package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class BooleanConstant extends ConstantImpl {
    private final boolean value;

    private BooleanConstant(final boolean value) {
        super(ConstantDescs.CD_boolean);
        this.value = value;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (value) {
            cb.iconst_1();
        } else {
            cb.iconst_0();
        }
    }

    public static final BooleanConstant FALSE = new BooleanConstant(false);
    public static final BooleanConstant TRUE = new BooleanConstant(true);

    public DynamicConstantDesc<Boolean> desc() {
        return value ? ConstantDescs.TRUE : ConstantDescs.FALSE;
    }

    public Optional<DynamicConstantDesc<Boolean>> describeConstable() {
        return Optional.of(desc());
    }

    public boolean isZero() {
        return !value;
    }

    public boolean isNonZero() {
        return value;
    }

    public boolean equals(final ConstantImpl other) {
        return this == other;
    }

    public String toString() {
        return Boolean.toString(value);
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(value);
    }

    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
