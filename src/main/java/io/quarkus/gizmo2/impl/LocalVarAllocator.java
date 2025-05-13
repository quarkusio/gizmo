package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.CodeBuilder;

final class LocalVarAllocator extends Item {
    private final LocalVarImpl localVar;

    LocalVarAllocator(final LocalVarImpl localVar) {
        this.localVar = localVar;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        int slot = cb.allocateLocal(Util.actualKindOf(localVar.typeKind()));
        // we reserve the slot for the full remainder of the block to avoid control-flow analysis
        cb.localVariable(slot, localVar.name(), localVar.type(), cb.newBoundLabel(), block.endLabel());
        int lvSlot = localVar.slot;
        if (lvSlot != -1 && slot != lvSlot) {
            throw new IllegalStateException("Local variable reallocated into a different slot");
        }
        localVar.slot = slot;
    }
}
