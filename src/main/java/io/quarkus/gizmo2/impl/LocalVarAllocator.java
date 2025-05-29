package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

final class LocalVarAllocator extends Item {
    private final LocalVarImpl localVar;

    LocalVarAllocator(final LocalVarImpl localVar) {
        this.localVar = localVar;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        int slot = cb.allocateLocal(Util.actualKindOf(localVar.typeKind()));
        // we reserve the slot for the full remainder of the block to avoid control-flow analysis
        Label startScope = cb.newBoundLabel();
        Label endScope = block.endLabel();
        cb.localVariable(slot, localVar.name(), localVar.type(), startScope, endScope);
        localVar.slot = slot;
    }
}
