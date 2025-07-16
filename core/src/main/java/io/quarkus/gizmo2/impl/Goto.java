package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

abstract class Goto extends Item {
    Goto() {
    }

    public boolean mayFallThrough() {
        return false;
    }

    /**
     * {@return the target label}
     * Must only be called from {@link Item#writeCode(CodeBuilder, BlockCreatorImpl)}.
     *
     * @param from the block that is the originating point of the jump (must not be {@code null})
     */
    abstract Label target(BlockCreatorImpl from);

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.goto_(target(block));
    }
}
