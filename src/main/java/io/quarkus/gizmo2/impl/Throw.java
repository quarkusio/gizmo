package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class Throw extends Item {
    final Item thrown;

    Throw(final Expr val) {
        thrown = (Item) val;
    }

    protected Node insert(final Node node) {
        Node res = super.insert(node);
        cleanStack(node.prev());
        return res;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return thrown.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // TODO: cleanup within a try is handled in a catch-all; yet return still needs cleanup in this case...
        System.err.println("Throw-inside-try is currently broken, so don't release until it's fixed!");
        cb.athrow();
    }

    public boolean mayFallThrough() {
        return false;
    }

    public boolean mayThrow() {
        return true;
    }
}
