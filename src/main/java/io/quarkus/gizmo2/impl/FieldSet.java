package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class FieldSet extends Item {
    private final FieldDeref fieldDeref;
    private final Item value;

    FieldSet(final FieldDeref fieldDeref, final Item value) {
        this.fieldDeref = fieldDeref;
        this.value = value;
    }

    /**
     * {@return the field dereference being assigned to}
     */
    FieldDeref fieldDeref() {
        return fieldDeref;
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
        SourceGenerator.stmtFieldSet(this, sb);
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        value.process(itr, op);
        fieldDeref.instance().process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.putfield(fieldDeref.owner(), fieldDeref.name(), fieldDeref.desc().type());
        smb.pop(); // receiver
        smb.pop(); // value
        smb.wroteCode();
    }
}
