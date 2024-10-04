package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_boolean;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class Rel extends Item {
    private final Item a;
    private final Item b;
    private final If.Kind kind;

    Rel(final Expr a, final Expr b, final If.Kind kind) {
        this.kind = kind;
        requireSameType(a, b);
        this.a = (Item) a;
        this.b = (Item) b;
        if (a.typeKind() == TypeKind.REFERENCE) {
            if (kind.if_acmp == null) {
                throw new IllegalStateException("Invalid comparison for reference types");
            }
        } else if (a.typeKind() != TypeKind.INT) {
            throw new UnsupportedOperationException("Only supported on int and reference types");
        }
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        b.process(iter, op);
        a.process(iter, op);
    }

    Item left() {
        return a;
    }

    Item right() {
        return b;
    }

    public ClassDesc type() {
        return CD_boolean;
    }

    public boolean bound() {
        return a.bound() || b.bound();
    }

    If.Kind kind() {
        return kind;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Label true_ = cb.newLabel();
        Label end = cb.newLabel();
        switch (typeKind().asLoadable()) {
            case INT -> kind.if_icmp.accept(cb, true_);
            case REFERENCE -> kind.if_acmp.accept(cb, true_);
            default -> throw new IllegalStateException();
        }
        cb.iconst_0();
        cb.goto_(end);
        cb.labelBinding(true_);
        cb.iconst_1();
        cb.labelBinding(end);
    }
}
