package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class Cast extends ExprImpl {
    private final ExprImpl a;
    private final ClassDesc toType;

    Cast(final Expr a, final ClassDesc toType) {
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
        return a.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (a.typeKind()) {
            case INT -> {
                switch (TypeKind.from(toType)) {
                    case BYTE -> cb.i2b();
                    case SHORT -> cb.i2s();
                    case CHAR -> cb.i2c();
                    case INT -> {}
                    case LONG -> cb.i2l();
                    case FLOAT -> cb.i2f();
                    case DOUBLE -> cb.i2d();
                    default -> throw new IllegalStateException();
                }
            }
        }
    }
}
