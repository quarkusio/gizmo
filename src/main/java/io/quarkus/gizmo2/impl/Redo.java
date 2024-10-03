package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.BlockCreatorImpl.cleanStack;

import java.util.ListIterator;

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

    protected void insert(final ListIterator<Item> iter) {
        super.insert(iter);
        cleanStack(iter);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        block.exitTo(cb, outer);
        cb.goto_(outer.startLabel());
    }

    public boolean exitsBlock() {
        return true;
    }
}
