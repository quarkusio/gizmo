package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.function.BiConsumer;

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

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        a.process(itr, op);
    }
}
