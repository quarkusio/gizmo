package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BlockExpr extends Item {
    private final ClassDesc type;

    BlockExpr(final ClassDesc type) {
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // implicit (no operation)
    }
}
