package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_boolean;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;

final class IfZero extends If {
    final Item a;

    IfZero(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse,
            final Item a, final boolean mustBeBoolean) {
        super(type, kind, whenTrue, whenFalse);
        this.a = mustBeBoolean ? convert(a, CD_boolean) : a;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        a.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        smb.pop(); // a
        super.writeCode(cb, block, smb);
    }

    IfOp op(final Kind kind) {
        return switch (a.typeKind().asLoadable()) {
            case INT -> kind.if_;
            case REFERENCE -> kind.if_acmpnull;
            default -> throw impossibleSwitchCase(a.typeKind().asLoadable());
        };
    }
}
