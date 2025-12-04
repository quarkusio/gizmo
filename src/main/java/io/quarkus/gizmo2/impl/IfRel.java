package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class IfRel extends If {
    final Item a, b;

    IfRel(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse,
            final Item a, final Item b) {
        super(type, kind, whenTrue, whenFalse);
        this.a = a;
        this.b = b;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        b.process(itr, op);
        a.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        smb.pop(); // a
        smb.pop(); // b
        super.writeCode(cb, block, smb);
    }

    IfOp op(final Kind kind) {
        return switch (a.typeKind().asLoadable()) {
            case INT -> kind.if_icmp;
            case REFERENCE -> kind.if_acmp;
            default -> throw impossibleSwitchCase(a.typeKind().asLoadable());
        };
    }
}
