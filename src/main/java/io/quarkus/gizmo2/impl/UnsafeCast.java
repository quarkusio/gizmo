package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class UnsafeCast extends Cast {

    UnsafeCast(final Expr a, final ClassDesc toType) {
        super(a, toType);
    }

    public boolean bound() {
        // has side effects
        return a.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // nothing
    }
}
