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

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        a.process(iter, op);
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
