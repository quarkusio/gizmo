package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class ArrayLength extends Item {
    private final Item item;
    boolean bound;

    ArrayLength(final Item item) {
        this.item = item;
    }

    @Override
    public ClassDesc type() {
        return ConstantDescs.CD_int;
    }

    protected void bind() {
        if (item.bound()) {
            bound = true;
        }
    }

    @Override
    public boolean bound() {
        return bound;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return item.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.arraylength();
    }
}
