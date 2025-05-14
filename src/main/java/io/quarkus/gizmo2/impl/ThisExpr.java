package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.This;

public final class ThisExpr extends Item implements This {
    private final GenericType type;

    public ThisExpr(final GenericType type) {
        this.type = type;
    }

    public boolean bound() {
        return false;
    }

    public ClassDesc type() {
        return type.desc();
    }

    public GenericType genericType() {
        return type;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.aload(0);
    }

    public String name() {
        return "this";
    }

    public String itemName() {
        return "this";
    }
}
