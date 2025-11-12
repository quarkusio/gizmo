package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldCompareAndSet extends CompareAndSet {
    private final Item instance;

    FieldCompareAndSet(final FieldDeref fieldDeref, final Item expect, final Item update, final boolean weak,
            final MemoryOrder mode) {
        super(expect, update, weak, mode, ConstImpl.ofFieldVarHandle(fieldDeref.desc()),
                MethodTypeDesc.of(CD_boolean, fieldDeref.owner(), fieldDeref.type(), fieldDeref.type()));
        instance = fieldDeref.instance();
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        instance.process(itr, op);
    }

    protected void popCoordinates(final StackMapBuilder smb) {
        smb.pop();
    }
}
