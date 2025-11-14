package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class ArrayLoadViaHandle extends Item {
    private final ArrayDeref arrayDeref;
    private final MemoryOrder mode;

    ArrayLoadViaHandle(final ArrayDeref arrayDeref, final MemoryOrder mode) {
        this.arrayDeref = arrayDeref;
        this.mode = mode;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        arrayDeref.index().process(itr, op);
        arrayDeref.array().process(itr, op);
        ConstImpl.ofArrayVarHandle(arrayDeref.array().type()).process(itr, op);
    }

    protected void computeType() {
        initType(arrayDeref.type());
        if (arrayDeref.hasGenericType()) {
            initGenericType(arrayDeref.genericType());
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "get";
            case Opaque -> "getOpaque";
            case Acquire -> "getAcquire";
            case Volatile -> "getVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(type(), arrayDeref.array().type(), CD_int));
        smb.pop(); // array
        smb.pop(); // index
        smb.push(type()); // result
        smb.wroteCode();
    }
}
