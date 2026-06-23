package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;
import io.smallrye.common.constraint.Assert;

public final class Pop extends Item {
    private final Item expr;

    public Pop(final Item expr) {
        this.expr = expr;
    }

    /**
     * {@return the expression being popped}
     */
    Item expr() {
        return expr;
    }

    @Override
    protected boolean isSourceStatement() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SourceGenerator.stmtPop(this, sb);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        expr.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        switch (expr.typeKind().slotSize()) {
            case 0 -> {
            }
            case 1 -> {
                cb.pop();
                smb.pop();
                smb.wroteCode();
            }
            case 2 -> {
                cb.pop2();
                smb.pop();
                smb.wroteCode();
            }
            default -> throw Assert.impossibleSwitchCase(expr.typeKind());
        }
    }
}
