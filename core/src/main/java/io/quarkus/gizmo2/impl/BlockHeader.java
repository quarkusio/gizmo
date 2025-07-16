package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BlockHeader extends Item {
    static final BlockHeader INSTANCE = new BlockHeader();

    private BlockHeader() {
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // implicit (no operation)
    }
}
