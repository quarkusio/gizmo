package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.classfile.CodeBuilder;

public final class FieldDeref extends AssignableImpl implements InstanceFieldVar {
    private final Item item;
    private final FieldDesc desc;
    private final GenericType genericType;
    private boolean bound;

    FieldDeref(final Item item, final FieldDesc desc, final GenericType genericType) {
        this.item = item;
        this.desc = desc;
        this.genericType = genericType;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        item.process(itr, op);
    }

    public boolean bound() {
        return bound;
    }

    protected void bind() {
        if (item.bound()) {
            bound = true;
        }
    }

    public FieldDesc desc() {
        return desc;
    }

    protected void computeType() {
        initType(desc.type());
        if (genericType != null) {
            initGenericType(genericType);
        }
    }

    public String itemName() {
        return item.itemName() + "." + desc.name();
    }

    public Item instance() {
        return item;
    }

    Item emitCompareAndExchange(final BlockCreatorImpl block, final Item expect, final Item update, final MemoryOrder order) {
        return new FieldCompareAndExchange(this, expect, update, order);
    }

    Item emitCompareAndSet(final BlockCreatorImpl block, final Item expect, final Item update, final boolean weak,
            final MemoryOrder order) {
        return new FieldCompareAndSet(this, expect, update, weak, order);
    }

    Item emitReadModifyWrite(final BlockCreatorImpl block, final String op, final Item newVal, final MemoryOrder order) {
        return new FieldReadModifyWrite(this, op, newVal, order);
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> asBound();
            default -> new FieldGetViaHandle(this, mode);
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> new FieldSet(this, value);
            default -> new FieldSetViaHandle(this, value, mode);
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.getfield(owner(), name(), type());
        smb.pop(); // receiver
        smb.push(type()); // value
        smb.wroteCode();
    }
}
