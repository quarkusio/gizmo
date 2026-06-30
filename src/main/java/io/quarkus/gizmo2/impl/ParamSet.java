package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class ParamSet extends Item {
    private final ParamVarImpl paramVar;
    private final Item value;

    ParamSet(final ParamVarImpl paramVar, final Item value) {
        this.paramVar = paramVar;
        this.value = value;
    }

    /**
     * {@return the parameter variable being set}
     */
    ParamVarImpl paramVar() {
        return paramVar;
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
        SourceGenerator.stmtParamSet(this, sb);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.storeLocal(Util.actualKindOf(paramVar.typeKind()), paramVar.slot());
        smb.store(paramVar.slot(), paramVar.type());
        smb.wroteCode();
    }
}
