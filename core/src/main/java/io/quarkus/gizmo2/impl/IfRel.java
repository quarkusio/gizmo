package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

final class IfRel extends If {
    final Item a, b;

    IfRel(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse,
            final Item a, final Item b) {
        super(type, kind, whenTrue, whenFalse);
        this.a = a;
        this.b = b;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(b.process(node.prev(), op), op);
    }

    IfOp op(final Kind kind) {
        return switch (a.typeKind().asLoadable()) {
            case INT -> kind.if_icmp;
            case REFERENCE -> kind.if_acmp;
            default -> throw new IllegalStateException();
        };
    }
}
