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

    /**
     * {@return the static field being assigned to}
     */
    StaticFieldVarImpl staticFieldVar() {
        return staticFieldVar;
    }

    /**
     * {@return the value being assigned}
     */
    Item value() {
        return value;
    }

    @Override
    protected boolean isSourceStatement() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SourceGenerator.stmtStaticFieldSet(this, sb);
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
