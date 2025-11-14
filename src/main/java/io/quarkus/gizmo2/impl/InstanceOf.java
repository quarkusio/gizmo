package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.BiConsumer;

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
        super(CD_boolean);
        this.input = (Item) input;
        this.type = type;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        input.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        label = cb.newBoundLabel();
        cb.instanceOf(type.desc());
        smb.pop(); // input
        smb.push(CD_boolean); // result
        smb.wroteCode();
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (type.hasAnnotations(retention)) {
            Util.computeAnnotations(type, retention, TypeAnnotation.TargetInfo.ofInstanceofExpr(label), annotations,
                    new ArrayDeque<>());
        }
    }
}
