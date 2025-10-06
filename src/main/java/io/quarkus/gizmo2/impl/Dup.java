package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class Dup extends Item {
    private final Item input;

    Dup(final Item input) {
        this.input = input;
    }

    protected void computeType() {
        initType(input.type());
        if (input.hasGenericType()) {
            initGenericType(input.genericType());
        }
    }

    public Node pop(final Node node) {
        // a dup can always be safely removed in lieu of a pop
        Node prev = node.prev();
        node.remove();
        return prev;
    }

    Node verify(Node node) {
        super.verify(node);
        // re-process the node
        return node.prev();
    }

    protected Node process(final Node node, final BiFunction<Item, Node, Node> op) {
        Node prev = node.prev();
        super.process(node, op);
        return prev;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (typeKind().slotSize()) {
            case 2 -> cb.dup2();
            case 1 -> cb.dup();
            case 0 -> {
            }
            default -> throw impossibleSwitchCase(typeKind().asLoadable());
        }
    }
}
