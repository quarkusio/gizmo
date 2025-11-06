package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldGetViaHandle extends Item {
    private final FieldDeref fieldDeref;
    private final MemoryOrder mode;

    FieldGetViaHandle(final FieldDeref fieldDeref, final MemoryOrder mode) {
        this.fieldDeref = fieldDeref;
        this.mode = mode;
    }

    protected void computeType() {
        initType(fieldDeref.type());
        if (fieldDeref.hasGenericType()) {
            initGenericType(fieldDeref.genericType());
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        fieldDeref.instance().process(itr, op);
        ConstImpl.ofFieldVarHandle(fieldDeref.desc()).process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.invokevirtual(CD_VarHandle, switch (mode) {
            case Plain -> "get";
            case Opaque -> "getOpaque";
            case Acquire -> "getAcquire";
            case Volatile -> "getVolatile";
            default -> throw impossibleSwitchCase(mode);
        }, MethodTypeDesc.of(type(), fieldDeref.instance().type()));
        smb.pop(); // VarHandle
        smb.pop(); // receiver
        smb.push(type()); // value
        smb.wroteCode();
    }
}
