package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_boolean;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class RelZero extends Item {
    private final Item a;
    private final If.Kind kind;

    RelZero(final Expr a, final If.Kind kind) {
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

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }

    public ClassDesc type() {
        return CD_boolean;
    }

    public boolean bound() {
        return a.bound();
    }

    If.Kind kind() {
        return kind;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Label true_ = cb.newLabel();
        Label end = cb.newLabel();
        switch (typeKind().asLoadable()) {
            case INT -> kind.if_.accept(cb, true_);
            case REFERENCE -> kind.if_acmpnull.accept(cb, true_);
            case LONG -> {
                cb.lconst_0();
                cb.lcmp();
                cb.iconst_1();
                cb.iand();
                return;
            }
            case FLOAT -> {
                cb.fconst_0();
                cb.fcmpg();
                cb.iconst_1();
                cb.iand();
                return;
            }
            case DOUBLE -> {
                cb.dconst_0();
                cb.dcmpg();
                cb.iconst_1();
                cb.iand();
                return;
            }
            default -> throw new IllegalStateException();
        }
        cb.iconst_0();
        cb.goto_(end);
        cb.labelBinding(true_);
        cb.iconst_1();
        cb.labelBinding(end);
    }
}
