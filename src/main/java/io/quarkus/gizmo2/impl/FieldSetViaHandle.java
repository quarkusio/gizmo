package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldSetViaHandle extends Item {
    private final FieldDeref fieldDeref;
    private final Item value;
    private final MemoryOrder mode;

    FieldSetViaHandle(final FieldDeref fieldDeref, final Item value, final MemoryOrder mode) {
        this.fieldDeref = fieldDeref;
        this.value = value;
        this.mode = mode;
    }

    protected void forEachDependency(ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        fieldDeref.instance().process(itr, op);
        ConstImpl.ofFieldVarHandle(fieldDeref.desc()).process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "set";
            case Opaque -> "setOpaque";
            case Release -> "setRelease";
            case Volatile -> "setVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(CD_void, fieldDeref.instance().type(), fieldDeref.desc().type()));
        smb.pop(); // VarHandle
        smb.pop(); // receiver
        smb.pop(); // value
        smb.wroteCode();
    }
}
