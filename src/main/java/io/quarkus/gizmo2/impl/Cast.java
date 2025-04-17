package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.quarkus.gizmo2.Expr;

abstract class Cast extends Item {
    final Item a;
    final ClassDesc toType;

    public Cast(final Expr a, final ClassDesc toType) {
        this.a = (Item) a;
        this.toType = toType;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }

    public ClassDesc type() {
        return toType;
    }
}
