package io.quarkus.gizmo2.impl;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;

public class NewResult extends Item {
    private final New new_;
    private final Invoke invoke;

    NewResult(New new_, Invoke invoke) {
        super(new_.type(), new_.hasGenericType() ? new_.genericType() : null);
        this.new_ = new_;
        this.invoke = invoke;
    }

    @Override
    public String itemName() {
        return "NewResult:" + new_.type().displayName();
    }

    @Override
    protected void forEachDependency(ListIterator<Item> itr, BiConsumer<Item, ListIterator<Item>> op) {
        invoke.process(itr, op);
        new_.process(itr, op);
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block, final StackMapBuilder smb) {
        // nothing
        smb.pop(); // uninitialized
        smb.push(type());
    }
}
