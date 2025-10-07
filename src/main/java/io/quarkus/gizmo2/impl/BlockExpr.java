package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;

final class BlockExpr extends Item {
    BlockExpr(final ClassDesc type) {
        super(type);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        // implicit (no operation)
    }
}
