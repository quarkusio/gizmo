package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.IntConstant;

final class NewEmptyArray extends Item {
    private final ClassDesc arrayType;
    private final Item size;

    NewEmptyArray(final ClassDesc elemType, final Item size) {
        arrayType = elemType.arrayType();
        this.size = size;
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        size.process(iter, op);
    }

    public ClassDesc type() {
        return arrayType;
    }

    public boolean bound() {
        return true;
    }

    public Expr length() {
        return size instanceof IntConstant ? size : super.length();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        ClassDesc componentType = type().componentType();
        TypeKind typeKind = TypeKind.from(componentType);
        if (typeKind == TypeKind.REFERENCE) {
            cb.anewarray(componentType);
        } else {
            cb.newarray(typeKind);
        }
    }
}