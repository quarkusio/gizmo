package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.creator.BlockCreator;

final class Redo extends Item {
    private final BlockCreatorImpl outer;

    Redo(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "Redo:" + outer;
    }

    protected Node insert(final Node node) {
        Node res = super.insert(node);
        cleanStack(node);
        return res;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.goto_(outer.startLabel());
    }
}
