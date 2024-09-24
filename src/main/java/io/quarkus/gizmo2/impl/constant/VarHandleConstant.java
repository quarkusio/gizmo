package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

public abstract class VarHandleConstant extends ConstantImpl {
    protected final VarHandle.VarHandleDesc desc;

    VarHandleConstant(final VarHandle.VarHandleDesc desc) {
        super(Util.classDesc(VarHandle.class));
        this.desc = desc;
    }

    public final boolean equals(final ConstantImpl obj) {
        return obj instanceof VarHandleConstant other && equals(other);
    }

    public abstract boolean equals(final VarHandleConstant obj);

    public ConstantDesc desc() {
        return desc;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.ldc(desc);
    }

    public Optional<VarHandle.VarHandleDesc> describeConstable() {
        return Optional.of(desc);
    }
}
