package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.common.constraint.Assert;

final class ArrayStoreViaHandle extends Item {
    private final ArrayDeref arrayDeref;
    private final Item value;
    private final MemoryOrder mode;

    ArrayStoreViaHandle(final ArrayDeref arrayDeref, final Item value, final MemoryOrder mode) {
        this.arrayDeref = arrayDeref;
        this.value = value;
        this.mode = mode;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        arrayDeref.index().process(itr, op);
        arrayDeref.array().process(itr, op);
        ConstImpl.ofArrayVarHandle(arrayDeref.array().type()).process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw Assert.impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(CD_void, arrayDeref.array().type(), CD_int, arrayDeref.type()));
        smb.pop(); // VarHandle constant
        smb.pop(); // array
        smb.pop(); // index
        smb.pop(); // value
        smb.wroteCode();
    }
}
