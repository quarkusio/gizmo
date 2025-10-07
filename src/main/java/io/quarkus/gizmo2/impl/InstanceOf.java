package io.quarkus.gizmo2.impl;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ConstantDescs;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class InstanceOf extends Item {
    private final Item input;
    private final GenericType type;
    private Label label;

    InstanceOf(final Expr input, final GenericType type) {
        super(ConstantDescs.CD_boolean);
        this.input = (Item) input;
        this.type = type;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return input.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        label = cb.newBoundLabel();
        cb.instanceOf(type.desc());
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (type.hasAnnotations(retention)) {
            Util.computeAnnotations(type, retention, TypeAnnotation.TargetInfo.ofInstanceofExpr(label), annotations,
                    new ArrayDeque<>());
        }
    }
}
