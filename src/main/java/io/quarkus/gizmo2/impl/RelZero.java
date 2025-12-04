package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_boolean;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.attribute.StackMapFrameInfo;

final class RelZero extends Item {
    private final Item a;
    private final If.Kind kind;

    RelZero(final Expr a, final If.Kind kind) {
        super(CD_boolean);
        this.kind = kind;
        this.a = (Item) a;
        if (a.typeKind() == TypeKind.REFERENCE) {
            if (kind.if_acmp == null) {
                throw new IllegalStateException("Invalid comparison for reference types");
            }
        }
    }

    Item input() {
        return a;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        a.process(itr, op);
    }

    If.Kind kind() {
        return kind;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        Label true_ = cb.newLabel();
        Label end = cb.newLabel();
        smb.pop(); // a
        switch (a.typeKind().asLoadable()) {
            case INT -> {
                kind.if_.accept(cb, true_);
                smb.wroteCode();
            }
            case REFERENCE -> {
                kind.if_acmpnull.accept(cb, true_);
                smb.wroteCode();
            }
            case LONG -> {
                cb.lconst_0();
                cb.lcmp();
                cb.iconst_1();
                cb.iand();
                smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
                smb.wroteCode();
                return;
            }
            case FLOAT -> {
                cb.fconst_0();
                cb.fcmpg();
                cb.iconst_1();
                cb.iand();
                smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
                smb.wroteCode();
                return;
            }
            case DOUBLE -> {
                cb.dconst_0();
                cb.dcmpg();
                cb.iconst_1();
                cb.iand();
                smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
                smb.wroteCode();
                return;
            }
            default -> throw impossibleSwitchCase(a.typeKind().asLoadable());
        }
        cb.iconst_0();
        cb.goto_(end);
        cb.labelBinding(true_);
        smb.addFrameInfo(cb);
        cb.iconst_1();
        smb.wroteCode();
        smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
        cb.labelBinding(end);
        smb.addFrameInfo(cb);
    }
}
