package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class LocalVarSet extends Item {
    private final LocalVarImpl localVar;
    private final Item value;

    LocalVarSet(final LocalVarImpl localVar, final Item value) {
        this.localVar = localVar;
        this.value = value;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        localVar.checkSlot();
        cb.storeLocal(Util.actualKindOf(localVar.typeKind()), localVar.slot);
        smb.pop();
        smb.store(localVar.slot, localVar.type());
        smb.wroteCode();
    }
}
