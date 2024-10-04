package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BlockHeaderExpr extends Item {
    private final BlockCreatorImpl block;
    private final ClassDesc type;

    BlockHeaderExpr(final BlockCreatorImpl block, final ClassDesc type) {
        this.block = block;
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    BlockCreatorImpl block() {
        return block;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // implicit (no operation)
    }
}
