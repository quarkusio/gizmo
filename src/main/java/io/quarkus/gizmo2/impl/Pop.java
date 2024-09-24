package io.quarkus.gizmo2.impl;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;

public final class Pop extends Item {
    private final ExprImpl expr;

    public Pop(final ExprImpl expr) {
        this.expr = expr;
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        expr.process(block, iter, verifyOnly);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (expr.typeKind().slotSize() == 2) {
            cb.pop2();
        } else {
            cb.pop();
        }
    }
}
