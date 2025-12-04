package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.VarHandleConst;
import io.smallrye.classfile.CodeBuilder;

abstract class CompareAndSet extends Item {
    private final Item expect;
    private final Item update;
    private final boolean weak;
    private final MemoryOrder mode;
    private final VarHandleConst handle;
    private final MethodTypeDesc opDesc;

    protected CompareAndSet(final Item expect, final Item update, final boolean weak, final MemoryOrder mode,
            final VarHandleConst handle, final MethodTypeDesc opDesc) {
        super(CD_boolean);
        this.expect = expect;
        this.update = update;
        this.weak = weak;
        this.mode = mode;
        this.handle = handle;
        this.opDesc = opDesc;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        update.process(itr, op);
        expect.process(itr, op);
        forEachCoordinateDependency(itr, op);
        handle.process(itr, op);
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Volatile -> weak ? "weakCompareAndSet" : "compareAndSet";
            case Acquire -> "weakCompareAndSetAcquire";
            case Release -> "weakCompareAndSetRelease";
            case Plain -> "weakCompareAndSetPlain";
            default -> throw impossibleSwitchCase(mode);
        }, opDesc);
        smb.pop(); // update
        smb.pop(); // expect
        popCoordinates(smb); // coordinates
        smb.pop(); // handle
        smb.push(type()); // value
        smb.wroteCode();
    }

    protected void popCoordinates(final StackMapBuilder smb) {
    }
}
