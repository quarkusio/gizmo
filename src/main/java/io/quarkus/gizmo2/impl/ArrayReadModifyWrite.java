package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

final class ArrayReadModifyWrite extends ReadModifyWrite {
    private final Item index;
    private final Item array;

    ArrayReadModifyWrite(final ArrayDeref arrayDeref, final String op, final Item value, final MemoryOrder mode) {
        super(arrayDeref.type(), arrayDeref.hasGenericType() ? arrayDeref.genericType() : null, op, value, mode,
                ConstImpl.ofArrayVarHandle(arrayDeref.type().arrayType()),
                MethodTypeDesc.of(arrayDeref.type(), arrayDeref.type().arrayType(), CD_int, arrayDeref.type()));
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
