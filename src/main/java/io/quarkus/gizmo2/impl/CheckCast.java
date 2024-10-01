package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class CheckCast extends ExprImpl {
    private final ExprImpl a;
    private final ClassDesc toType;

    CheckCast(final Expr a, final ClassDesc toType) {
        this.a = (ExprImpl) a;
        this.toType = toType;
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        a.process(block, iter, verifyOnly);
    }

    public ClassDesc type() {
        return toType;
    }

    public boolean bound() {
        // has side effects
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.checkcast(toType);
    }
}
