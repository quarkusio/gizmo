package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BoundItem extends Item {
    private final Item item;

    BoundItem(final Item item) {
        this.item = item;
    }

    Item item() {
        return item;
    }

    public ClassDesc type() {
        return item.type();
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return item.forEachDependency(node, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        item.writeCode(cb, block);
    }

    public String itemName() {
        return item.itemName() + ":bound";
    }
}
