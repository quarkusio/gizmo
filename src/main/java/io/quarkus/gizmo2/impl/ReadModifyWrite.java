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

abstract class ReadModifyWrite extends Item {
    private final String op;
    private final Item value;
    private final MemoryOrder mode;
    private final VarHandleConst handle;
    private final MethodTypeDesc opDesc;

    protected ReadModifyWrite(final ClassDesc type, final GenericType genericType, final String op, final Item value,
            final MemoryOrder mode, final VarHandleConst handle, final MethodTypeDesc opDesc) {
        super(type, genericType);
        this.op = op;
        this.value = value;
        this.mode = mode;
        this.handle = handle;
        this.opDesc = opDesc;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        forEachCoordinateDependency(itr, op);
        handle.process(itr, op);
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Volatile -> "getAnd" + op;
            case Acquire -> "getAnd" + op + "Acquire";
            case Release -> "getAnd" + op + "Release";
            default -> throw impossibleSwitchCase(mode);
        }, opDesc);
        smb.pop(); // value
        popCoordinates(smb); // coordinates
        smb.pop(); // handle
        smb.push(type()); // result
        smb.wroteCode();
    }

    protected void popCoordinates(final StackMapBuilder smb) {
    }
}
