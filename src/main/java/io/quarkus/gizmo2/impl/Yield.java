package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.smallrye.classfile.CodeBuilder;

/**
 * A node that yields a result value to the enclosing block.
 */
final class Yield extends Item {
    static final Yield YIELD_VOID = new Yield(ConstImpl.ofVoid());

    private final Item value;

    Yield(final Expr value) {
        this.value = (Item) value;
    }

    protected void forEachDependency(ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        // no operation
    }

    Expr value() {
        return value;
    }
}
