package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.requireSameTypeKind;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_boolean;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.attribute.StackMapFrameInfo;

final class Rel extends Item {
    private final Item a;
    private final Item b;
    private final If.Kind kind;

    Rel(final Expr a, final Expr b, final If.Kind kind) {
        super(CD_boolean);
        this.kind = kind;
        requireSameTypeKind(a, b);
        this.a = (Item) a;
        this.b = (Item) b;
        if (a.typeKind() == TypeKind.REFERENCE) {
            if (kind.if_acmp == null) {
                throw new IllegalStateException("Invalid comparison for reference types");
            }
        } else if (a.typeKind().asLoadable() != TypeKind.INT) {
            throw new UnsupportedOperationException("Only supported on int and reference types");
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        b.process(itr, op);
        a.process(itr, op);
    }

    Item left() {
        return a;
    }

    Item right() {
        return b;
    }

    If.Kind kind() {
        return kind;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        Label true_ = cb.newLabel();
        Label end = cb.newLabel();
        switch (a.typeKind().asLoadable()) {
            case INT -> kind.if_icmp.accept(cb, true_);
            case REFERENCE -> kind.if_acmp.accept(cb, true_);
            default -> throw impossibleSwitchCase(typeKind().asLoadable());
        }
        smb.pop(); // a
        smb.pop(); // b
        cb.iconst_0();
        cb.goto_(end);
        smb.wroteCode();
        cb.labelBinding(true_);
        smb.addFrameInfo(cb);
        cb.iconst_1();
        smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
        smb.wroteCode();
        cb.labelBinding(end);
        smb.addFrameInfo(cb);
        smb.wroteCode();
    }
}
