package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class UncheckedCast extends Cast {

    UncheckedCast(final Expr a, final GenericType toType) {
        super(a, toType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // nothing
    }
}
