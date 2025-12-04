package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.*;
import static java.lang.constant.ConstantDescs.*;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MemoryOrder;
import io.smallrye.classfile.CodeBuilder;

public final class ArrayDeref extends AssignableImpl {
    private final Item item;
    private final Item index;
    private boolean bound;

    ArrayDeref(final Item item, final Expr index) {
        this.item = item;
        this.index = convert(index, CD_int);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        index.process(itr, op);
        item.process(itr, op);
    }

    public Item array() {
        return item;
    }

    public Item index() {
        return index;
    }

    Item emitCompareAndExchange(final BlockCreatorImpl block, final Item expect, final Item update, final MemoryOrder order) {
        return new ArrayCompareAndExchange(this, expect, update, order);
    }

    Item emitCompareAndSet(final BlockCreatorImpl block, final Item expect, final Item update, final boolean weak,
            final MemoryOrder order) {
        return new ArrayCompareAndSet(this, expect, update, weak, order);
    }

    Item emitReadModifyWrite(final BlockCreatorImpl block, final String op, final Item newVal, final MemoryOrder order) {
        return new ArrayReadModifyWrite(this, op, newVal, order);
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        if (!mode.validForReads()) {
            throw new IllegalArgumentException("Invalid mode " + mode);
        }
        return switch (mode) {
            case AsDeclared, Plain -> asBound();
            default -> new ArrayLoadViaHandle(this, mode);
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared, Plain -> new ArrayStore(item, index, value, type());
            default -> new ArrayStoreViaHandle(this, value, mode);
        };
    }

    protected void computeType() {
        initType(item.type().componentType());
        if (item.hasGenericType()) {
            initGenericType(((GenericType.OfArray) item.genericType()).componentType());
        }
    }

    protected void bind() {
        if (item.bound() || index.bound()) {
            bound = true;
        }
    }

    public boolean bound() {
        return bound;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.arrayLoad(Util.actualKindOf(typeKind()));
        smb.pop();
        smb.pop();
        smb.push(type());
        smb.wroteCode();
    }
}
