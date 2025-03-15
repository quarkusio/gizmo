package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.creator.BlockCreator;

final class Break extends Goto {
    private final BlockCreatorImpl outer;

    public Break(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "Break:" + outer;
    }

    Label target() {
        return outer.endLabel();
    }
}
