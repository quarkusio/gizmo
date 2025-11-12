package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.StaticFieldVar;
import io.quarkus.gizmo2.desc.FieldDesc;

public final class StaticFieldVarImpl extends AssignableImpl implements StaticFieldVar {
    private final FieldDesc desc;

    public StaticFieldVarImpl(FieldDesc desc, final GenericType genericType) {
        super(desc.type(), genericType);
        this.desc = desc;
    }

    public FieldDesc desc() {
        return desc;
    }

    public boolean bound() {
        return false;
    }

    Item emitCompareAndExchange(final BlockCreatorImpl block, final Item expect, final Item update, MemoryOrder order) {
        return new StaticFieldCompareAndExchange(this, expect, update, order);
    }

    Item emitCompareAndSet(final BlockCreatorImpl block, final Item expect, final Item update, final boolean weak,
            final MemoryOrder order) {
        return new StaticFieldCompareAndSet(this, expect, update, weak, order);
    }

    Item emitReadModifyWrite(final BlockCreatorImpl block, final String op, final Item newVal, final MemoryOrder order) {
        return new StaticFieldReadModifyWrite(this, op, newVal, order);
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> asBound();
            default -> new StaticFieldGetViaHandle(this, mode);
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> new StaticFieldSet(this, value);
            default -> new StaticFieldSetViaHandle(this, mode, value);
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.getstatic(owner(), name(), type());
        smb.push(type());
        smb.wroteCode();
    }
}
