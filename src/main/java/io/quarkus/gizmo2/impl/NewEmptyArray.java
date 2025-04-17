package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.IntConst;

final class NewEmptyArray extends Item {
    private final ClassDesc arrayType;
    private final Item size;

    NewEmptyArray(final ClassDesc componentType, final Item size) {
        arrayType = componentType.arrayType();
        this.size = size;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return size.process(node.prev(), op);
    }

    public ClassDesc type() {
        return arrayType;
    }

    public Expr length() {
        return size instanceof IntConst ? size : super.length();
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