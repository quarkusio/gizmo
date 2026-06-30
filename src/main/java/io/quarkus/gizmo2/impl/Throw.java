package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Expr;
import io.smallrye.classfile.CodeBuilder;

final class Throw extends Item {
    final Item thrown;

    Throw(final Expr val) {
        thrown = (Item) val;
    }

    @Override
    protected boolean isSourceStatement() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SourceGenerator.stmtThrow(this, sb);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        thrown.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        // no cleanup blobs here
        smb.pop(); // thrown
        cb.athrow();
        smb.wroteCode();
    }

    public boolean mayFallThrough() {
        return false;
    }
}
