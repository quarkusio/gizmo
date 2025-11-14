package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BoundItem extends Item {
    private final Item item;

    BoundItem(final Item item) {
        this.item = item;
    }

    Item item() {
        return item;
    }

    protected void computeType() {
        initType(item.type());
        if (item.hasGenericType()) {
            initGenericType(item.genericType());
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        item.forEachDependency(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        item.writeCode(cb, block, smb);
    }

    public String itemName() {
        return item.itemName() + ":bound";
    }
}
