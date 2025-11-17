package io.quarkus.gizmo2.impl;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class FieldCompareAndExchange extends CompareAndExchange {
    private final Item instance;

    FieldCompareAndExchange(final FieldDeref fieldDeref, final Item expect, final Item update, final MemoryOrder mode) {
        super(fieldDeref.type(), fieldDeref.hasGenericType() ? fieldDeref.genericType() : null, expect, update, mode,
                ConstImpl.ofFieldVarHandle(fieldDeref.desc()),
                MethodTypeDesc.of(fieldDeref.type(), fieldDeref.owner(), fieldDeref.type(), fieldDeref.type()));
        instance = fieldDeref.instance();
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        instance.process(itr, op);
    }

    protected void popCoordinates(final StackMapBuilder smb) {
        smb.pop();
    }
}
