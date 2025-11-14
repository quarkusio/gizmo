package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.github.dmlloyd.classfile.attribute.StackMapFrameInfo;
import io.quarkus.gizmo2.GenericType;

final class New extends Item {
    private Label label;

    New(final ClassDesc type, final GenericType genericType) {
        super(type, genericType);
    }

    @Override
    public String itemName() {
        return "New:" + type().displayName();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        label = cb.newBoundLabel();
        cb.new_(type());
        smb.push(StackMapFrameInfo.UninitializedVerificationTypeInfo.of(label));
        smb.wroteCode();
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (hasGenericType() && genericType().hasAnnotations(retention)) {
            Util.computeAnnotations(genericType(), retention, TypeAnnotation.TargetInfo.ofNewExpr(label), annotations,
                    new ArrayDeque<>());
        }
    }
}
