package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class VoidConstant extends ConstantImpl{
    public static final VoidConstant INSTANCE = new VoidConstant();

    private VoidConstant() {
        super(ConstantDescs.CD_void);
    }

    public boolean equals(final ConstantImpl other) {
        return other == this;
    }

    public int hashCode() {
        return 0;
    }

    public ConstantDesc desc() {
        throw new IllegalArgumentException("No constant description for void");
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // no operation (invisible)
    }

    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }
}
