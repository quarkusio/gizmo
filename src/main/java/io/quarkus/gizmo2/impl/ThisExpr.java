package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.This;

public final class ThisExpr extends Item implements This {
    public ThisExpr(final ClassDesc type, final GenericType genericType) {
        super(type, genericType);
    }

    public boolean bound() {
        return false;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.aload(0);
        smb.push(type());
        smb.wroteCode();
    }

    public String name() {
        return "this";
    }

    public String itemName() {
        return "this";
    }
}
