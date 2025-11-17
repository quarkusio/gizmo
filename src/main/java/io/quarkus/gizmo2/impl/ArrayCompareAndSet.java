package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class ArrayCompareAndSet extends CompareAndSet {
    private final Item index;
    private final Item array;

    ArrayCompareAndSet(final ArrayDeref arrayDeref, final Item expect, final Item update, final boolean weak,
            final MemoryOrder order) {
        super(expect, update, weak, order, ConstImpl.ofArrayVarHandle(arrayDeref.type().arrayType()),
                MethodTypeDesc.of(CD_boolean, arrayDeref.type().arrayType(), CD_int, arrayDeref.type(), arrayDeref.type()));
        array = arrayDeref.array();
        index = arrayDeref.index();
    }

    protected void forEachCoordinateDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        index.process(itr, op);
        array.process(itr, op);
    }

    protected void popCoordinates(final StackMapBuilder smb) {
        smb.pop();
        smb.pop();
    }
}
