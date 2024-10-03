package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class PrimitiveCast extends ExprImpl {
    private final ExprImpl a;
    private final ClassDesc toType;

    PrimitiveCast(final Expr a, final ClassDesc toType) {
        this.a = (ExprImpl) a;
        this.toType = toType;
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        a.process(iter, op);
    }

    public ClassDesc type() {
        return toType;
    }

    public boolean bound() {
        return a.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.conversion(a.typeKind(), TypeKind.from(toType));
    }
}
