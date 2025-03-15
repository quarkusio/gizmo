package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.creator.BlockCreator;

final class Redo extends Goto {
    private final BlockCreatorImpl outer;

    Redo(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "Redo:" + outer;
    }

    Label target() {
        return outer.startLabel();
    }
}
