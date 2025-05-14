package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.GenericType;

final class New extends Item {
    private final GenericType type;
    private Label label;

    New(final GenericType type) {
        this.type = type;
    }

    @Override
    public String itemName() {
        return "New:" + type().displayName();
    }

    public GenericType genericType() {
        return type;
    }

    public ClassDesc type() {
        return type.desc();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        label = cb.newBoundLabel();
        cb.new_(type.desc());
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (type.hasAnnotations(retention)) {
            Util.computeAnnotations(type, retention, TypeAnnotation.TargetInfo.ofNewExpr(label), annotations,
                    new ArrayDeque<>());
        }
    }
}
