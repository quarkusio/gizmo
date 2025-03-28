package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;

final class ArrayStore extends Item {
    private final Item arrayExpr;
    private final Item index;
    private final Item value;
    private final ClassDesc componentType;

    ArrayStore(final Item arrayExpr, final Item index, final Item value, final ClassDesc componentType) {
        this.arrayExpr = arrayExpr;
        this.index = index;
        this.value = value;
        this.componentType = componentType;
    }

    Item arrayExpr() {
        return arrayExpr;
    }

    Item index() {
        return index;
    }

    Item value() {
        return value;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return arrayExpr.process(index.process(value.process(node.prev(), op), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.arrayStore(TypeKind.from(componentType));
    }
}
