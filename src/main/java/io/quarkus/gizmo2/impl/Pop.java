package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

public final class Pop extends Item {
    private final Item expr;

    public Pop(final Item expr) {
        this.expr = expr;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return expr.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (expr.typeKind().slotSize() == 2) {
            cb.pop2();
        } else {
            cb.pop();
        }
    }
}
