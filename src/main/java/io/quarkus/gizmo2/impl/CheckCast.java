package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class CheckCast extends Cast {
    CheckCast(final Expr a, final ClassDesc toType) {
        super(a, toType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.checkcast(toType);
    }
}
