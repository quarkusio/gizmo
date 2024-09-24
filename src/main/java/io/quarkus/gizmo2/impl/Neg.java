package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class Neg extends ExprImpl {
    private final ExprImpl a;

    Neg(final Expr a) {
        this.a = (ExprImpl) a;
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        a.process(block, iter, verifyOnly);
    }

    public ClassDesc type() {
        return a.type();
    }

    public boolean bound() {
        return a.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (typeKind()) {
            case INT -> cb.ineg();
            case LONG -> cb.lneg();
            case FLOAT -> cb.fneg();
            case DOUBLE -> cb.dneg();
            default -> throw new IllegalStateException();
        }
    }
}
