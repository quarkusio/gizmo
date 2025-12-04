package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.smallrye.classfile.CodeBuilder;

final class UncheckedCast extends Cast {

    UncheckedCast(final Expr a, final ClassDesc toType, final GenericType toGenericType) {
        super(a, toType, toGenericType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        // nothing
        smb.pop();
        smb.push(type());
    }
}
