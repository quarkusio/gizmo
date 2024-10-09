package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class CheckCast extends Item {
    private final Item a;
    private final ClassDesc toType;

    CheckCast(final Expr a, final ClassDesc toType) {
        this.a = (Item) a;
        this.toType = toType;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }

    public ClassDesc type() {
        return toType;
    }

    public boolean bound() {
        // has side effects
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.checkcast(toType);
    }
}
