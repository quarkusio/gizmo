package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.impl.constant.IntBasedConst;
import io.smallrye.classfile.CodeBuilder;

final class LocalVarDecrement extends Item {
    private final LocalVarImpl localVar;
    private final Const amount;

    LocalVarDecrement(final LocalVarImpl localVar, final Const amount) {
        this.localVar = localVar;
        this.amount = amount;
    }

    /**
     * {@return the local variable being decremented}
     */
    LocalVarImpl localVar() {
        return localVar;
    }

    /**
     * {@return the decrement amount}
     */
    Const amount() {
        return amount;
    }

    @Override
    protected boolean isSourceStatement() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SourceGenerator.stmtLocalVarDecrement(this, sb);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        localVar.checkSlot();
        cb.iinc(localVar.slot, -((IntBasedConst) amount).intValue());
        smb.wroteCode();
    }
}
