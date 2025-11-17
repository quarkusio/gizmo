package io.quarkus.gizmo2.impl;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldReadModifyWrite extends ReadModifyWrite {
    private final Item instance;

    FieldReadModifyWrite(final FieldDeref fieldDeref, final String op, final Item value, final MemoryOrder mode) {
        super(fieldDeref.type(), fieldDeref.hasGenericType() ? fieldDeref.genericType() : null, op, value, mode,
                ConstImpl.ofFieldVarHandle(fieldDeref.desc()),
                MethodTypeDesc.of(fieldDeref.type(), fieldDeref.owner(), fieldDeref.type()));
        instance = fieldDeref.instance();
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        instance.process(itr, op);
    }

    protected void popCoordinates(final StackMapBuilder smb) {
        smb.pop();
    }
}
