package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class CheckCast extends Cast {
    private Label label;

    CheckCast(final Expr a, final ClassDesc toType, final GenericType toGenericType) {
        super(a, toType, toGenericType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        label = cb.newBoundLabel();
        cb.checkcast(type());
        smb.pop(); // uncast
        smb.push(type()); // cast
        smb.wroteCode();
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (hasGenericType() && genericType().hasAnnotations(retention)) {
            Util.computeAnnotations(genericType(), retention, TypeAnnotation.TargetInfo.ofCastExpr(label, 0), annotations,
                    new ArrayDeque<>());
        }
    }
}
