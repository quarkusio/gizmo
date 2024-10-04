package io.quarkus.gizmo2.impl;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;

final class ArrayStore extends Item {
    private final Item arrayExpr;
    private final Item index;
    private final Item value;

    ArrayStore(final Item arrayExpr, final Item index, final Item value) {
        this.arrayExpr = arrayExpr;
        this.index = index;
        this.value = value;
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        value.process(iter, op);
        index.process(iter, op);
        arrayExpr.process(iter, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.arrayStore(arrayExpr.typeKind());
    }
}
