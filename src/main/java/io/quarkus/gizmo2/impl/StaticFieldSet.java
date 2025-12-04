package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class StaticFieldSet extends Item {
    private final StaticFieldVarImpl staticFieldVar;
    private final Item value;

    StaticFieldSet(final StaticFieldVarImpl staticFieldVar, final Item value) {
        this.staticFieldVar = staticFieldVar;
        this.value = value;
    }

    protected void forEachDependency(ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.putstatic(staticFieldVar.owner(), staticFieldVar.name(), staticFieldVar.desc().type());
        smb.pop(); // value
        smb.wroteCode();
    }
}
