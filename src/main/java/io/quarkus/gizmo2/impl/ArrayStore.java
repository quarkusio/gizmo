package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

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

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return arrayExpr.process(index.process(value.process(node.prev(), op), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.arrayStore(arrayExpr.typeKind());
    }
}
