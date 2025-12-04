package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.classfile.CodeBuilder;

final class StaticFieldSetViaHandle extends Item {
    private final StaticFieldVarImpl staticFieldVar;
    private final MemoryOrder mode;
    private final Item value;

    StaticFieldSetViaHandle(final StaticFieldVarImpl staticFieldVar, final MemoryOrder mode, final Item value) {
        this.staticFieldVar = staticFieldVar;
        this.mode = mode;
        this.value = value;
    }

    protected void forEachDependency(ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        ConstImpl.ofStaticFieldVarHandle(staticFieldVar.desc()).process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(CD_void, staticFieldVar.desc().type()));
        smb.pop(); // handle
        smb.pop(); // value
        smb.wroteCode();
    }
}
