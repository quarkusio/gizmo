package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.This;

public final class ThisExpr extends Item implements This {
    private final ClassDesc type;

    ThisExpr(final ClassDesc type) {
        this.type = type;
    }

    public boolean bound() {
        return false;
    }

    public ClassDesc type() {
        return type;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.aload(0);
    }

    public String itemName() {
        return "this";
    }
}
