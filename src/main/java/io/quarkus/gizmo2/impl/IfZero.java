package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

final class IfZero extends If {
    final Item a;

    IfZero(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse,
            final Item a) {
        super(type, kind, whenTrue, whenFalse);
        this.a = a;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }

    IfOp op(final Kind kind) {
        return switch (a.typeKind().asLoadable()) {
            case INT -> kind.if_;
            case REFERENCE -> kind.if_acmpnull;
            default -> throw new IllegalStateException();
        };
    }
}
