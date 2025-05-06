package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class PrimitiveCast extends Cast {

    PrimitiveCast(final Expr a, final GenericType toType) {
        super(a, toType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.conversion(Util.actualKindOf(a.typeKind()), TypeKind.from(toType.desc()));
    }
}
