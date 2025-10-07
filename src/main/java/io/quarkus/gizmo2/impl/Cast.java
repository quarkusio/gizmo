package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

abstract class Cast extends Item {
    final Item a;

    private boolean bound;

    public Cast(final Expr a, final ClassDesc toType, final GenericType toGenericType) {
        super(toType, toGenericType);
        this.a = (Item) a;
    }

    @Override
    public boolean bound() {
        return bound;
    }

    @Override
    protected void bind() {
        bound = true;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(node.prev(), op);
    }
}
