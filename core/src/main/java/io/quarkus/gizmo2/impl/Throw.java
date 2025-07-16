package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class Throw extends Item {
    final Item thrown;

    Throw(final Expr val) {
        thrown = (Item) val;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return thrown.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // no cleanup blobs here
        cb.athrow();
    }

    public boolean mayFallThrough() {
        return false;
    }
}
