package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.Expr;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.TypeKind;

final class PrimitiveCast extends Cast {

    PrimitiveCast(final Expr a, final ClassDesc toType) {
        super(a, toType, null);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.conversion(Util.actualKindOf(a.typeKind()), TypeKind.from(type()));
        smb.pop(); // old value
        smb.push(type()); // result
        smb.wroteCode();
    }
}
