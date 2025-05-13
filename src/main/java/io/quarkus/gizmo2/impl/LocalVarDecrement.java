package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.impl.constant.IntBasedConst;

final class LocalVarDecrement extends Item {
    private final LocalVarImpl localVar;
    private final Const amount;

    LocalVarDecrement(final LocalVarImpl localVar, final Const amount) {
        this.localVar = localVar;
        this.amount = amount;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        localVar.checkSlot();
        cb.iinc(localVar.slot, -((IntBasedConst) amount).intValue());
    }
}
