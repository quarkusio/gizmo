package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class CheckCast extends Cast {
    private Label label;

    CheckCast(final Expr a, final GenericType toType) {
        super(a, toType);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        label = cb.newBoundLabel();
        cb.checkcast(toType.desc());
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (toType.hasAnnotations(retention)) {
            Util.computeAnnotations(toType, retention, TypeAnnotation.TargetInfo.ofCastExpr(label, 0), annotations,
                    new ArrayDeque<>());
        }
    }
}
