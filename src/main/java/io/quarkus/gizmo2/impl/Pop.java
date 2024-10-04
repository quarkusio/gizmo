package io.quarkus.gizmo2.impl;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;

public final class Pop extends Item {
    private final Item expr;

    public Pop(final Item expr) {
        this.expr = expr;
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        expr.process(iter, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (expr.typeKind().slotSize() == 2) {
            cb.pop2();
        } else {
            cb.pop();
        }
    }
}
