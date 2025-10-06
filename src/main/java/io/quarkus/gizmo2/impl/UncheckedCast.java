package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class UncheckedCast extends Cast {

    UncheckedCast(final Expr a, final ClassDesc toType, final GenericType toGenericType) {
        super(a, toType, toGenericType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // nothing
    }
}
