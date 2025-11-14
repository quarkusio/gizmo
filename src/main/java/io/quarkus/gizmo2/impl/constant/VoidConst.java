package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.StackMapBuilder;

public final class VoidConst extends ConstImpl {
    public static final VoidConst INSTANCE = new VoidConst();

    private VoidConst() {
        super(ConstantDescs.CD_void);
    }

    public boolean equals(final ConstImpl other) {
        return other == this;
    }

    public int hashCode() {
        return 0;
    }

    public ConstantDesc desc() {
        throw new IllegalArgumentException("No constant description for void");
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        // no operation (invisible)
    }

    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.empty();
    }

    public String toString() {
        return "void";
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append("void");
    }
}
