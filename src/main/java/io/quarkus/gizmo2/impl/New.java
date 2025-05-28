package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;

final class New extends Item {
    private final ClassDesc type;

    New(final ClassDesc type) {
        this.type = type;
    }

    @Override
    public String itemName() {
        return "New:" + type().displayName();
    }

    public ClassDesc type() {
        return type;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.new_(type);
    }
}
