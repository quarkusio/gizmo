package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

/**
 * A node that yields a result value to the enclosing block.
 */
final class Yield extends Item {
    static final Yield YIELD_VOID = new Yield(ConstantImpl.ofVoid());

    private final Item value;

    Yield(final Item value) {
        this.value = value;
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        node = node.prev();
        node = value.isVoid() ? node : value.process(node, op);
        return cleanStack(node);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // no operation
    }

    Expr value() {
        return value;
    }
}
