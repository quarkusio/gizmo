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
        if (! value) {
            cb.iconst_0();
        } else {
            cb.iconst_1();
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

    public boolean equals(final ConstantImpl other) {
        return this == other;
    }

    public int hashCode() {
        return Boolean.hashCode(value);
    }
}
