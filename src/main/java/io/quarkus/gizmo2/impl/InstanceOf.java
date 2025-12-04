package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.TypeAnnotation;

final class InstanceOf extends Item {
    private final Item input;
    private final ClassDesc checkType;
    private final GenericType checkGenericType;
    private Label label;

    InstanceOf(final Expr input, final ClassDesc checkType, final GenericType checkGenericType) {
        super(CD_boolean);
        this.input = (Item) input;
        this.checkGenericType = checkGenericType;
        if (checkType == null) {
            this.checkType = checkGenericType.desc();
        } else {
            this.checkType = checkType;
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        input.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        label = cb.newBoundLabel();
        cb.instanceOf(checkType);
        smb.pop(); // input
        smb.push(CD_boolean); // result
        smb.wroteCode();
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        if (checkGenericType != null && checkGenericType.hasAnnotations(retention)) {
            Util.computeAnnotations(checkGenericType, retention, TypeAnnotation.TargetInfo.ofInstanceofExpr(label), annotations,
                    new ArrayDeque<>());
        }
    }
}
