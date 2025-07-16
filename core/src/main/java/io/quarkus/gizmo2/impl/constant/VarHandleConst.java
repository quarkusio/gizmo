package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.invoke.VarHandle;
import java.util.Optional;

import io.quarkus.gizmo2.impl.Util;

public abstract class VarHandleConst extends ConstImpl {
    final VarHandle.VarHandleDesc desc;

    VarHandleConst(final VarHandle.VarHandleDesc desc) {
        super(Util.classDesc(VarHandle.class));
        this.desc = desc;
    }

    public boolean isNonZero() {
        return true;
    }

    public final boolean equals(final ConstImpl obj) {
        return obj instanceof VarHandleConst other && equals(other);
    }

    public abstract boolean equals(final VarHandleConst obj);

    public ConstantDesc desc() {
        return desc;
    }

    public Optional<VarHandle.VarHandleDesc> describeConstable() {
        return Optional.of(desc);
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return b.append(desc);
    }
}
