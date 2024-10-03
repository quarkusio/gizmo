package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class Throw extends Item {
    final ExprImpl thrown;

    Throw(final Expr val) {
        thrown = (ExprImpl) val;
    }

    protected void insert(final ListIterator<Item> iter) {
        super.insert(iter);
        cleanStack(iter);
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        thrown.process(iter, op);
    }

    public boolean exitsAll() {
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // TODO: cleanup within a try is handled in a catch-all; yet return still needs cleanup in this case...
        System.err.println("Throw-inside-try is currently broken, so don't release until it's fixed!");
        block.exitAll(cb);
        cb.athrow();
    }
}
