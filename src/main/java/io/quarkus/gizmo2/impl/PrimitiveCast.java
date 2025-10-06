package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class PrimitiveCast extends Cast {

    PrimitiveCast(final Expr a, final ClassDesc toType) {
        super(a, toType, null);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.conversion(Util.actualKindOf(a.typeKind()), TypeKind.from(type()));
    }
}
