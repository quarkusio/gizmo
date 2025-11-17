package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.VarHandleConst;

abstract class CompareAndExchange extends Item {
    private final Item expect;
    private final Item update;
    private final MemoryOrder mode;
    private final VarHandleConst handle;
    private final MethodTypeDesc opDesc;

    protected CompareAndExchange(final ClassDesc type, final GenericType genericType, final Item expect, final Item update,
            final MemoryOrder mode, final VarHandleConst handle, final MethodTypeDesc opDesc) {
        super(type, genericType);
        this.expect = expect;
        this.update = update;
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
            case Volatile -> "compareAndExchange";
            case Acquire -> "compareAndExchangeAcquire";
            case Release -> "compareAndExchangeRelease";
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
