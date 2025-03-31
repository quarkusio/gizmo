package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

abstract class Goto extends Item {
    Goto() {}

    public boolean mayFallThrough() {
        return false;
    }

    abstract Label target();

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.goto_(target());
    }
}
