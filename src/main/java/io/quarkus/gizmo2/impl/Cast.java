package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.TypeKind;

abstract class Cast extends Item {
    final Item a;
    final GenericType toType;
    final TypeKind toTypeKind;

    private boolean bound;

    public Cast(final Expr a, final GenericType toType) {
        this.a = (Item) a;
        this.toType = toType;
        this.toTypeKind = TypeKind.from(toType.desc());
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

    public GenericType genericType() {
        return toType;
    }

    public ClassDesc type() {
        return toType.desc();
    }

    @Override
    public TypeKind typeKind() {
        return toTypeKind;
    }
}
