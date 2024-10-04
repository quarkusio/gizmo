package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;

final class New extends Item {
    private final ClassDesc type;

    New(final ClassDesc type) {
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    public boolean bound() {
        // `new` can float around
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.new_(type);
    }
}
