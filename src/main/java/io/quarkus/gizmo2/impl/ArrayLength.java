package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.attribute.StackMapFrameInfo;

final class ArrayLength extends Item {
    private final Item item;
    boolean bound;

    ArrayLength(final Item item) {
        this.item = item;
    }

    protected void computeType() {
        initType(CD_int);
    }

    protected void bind() {
        if (item.bound()) {
            bound = true;
        }
    }

    @Override
    public boolean bound() {
        return bound;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        item.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        cb.arraylength();
        smb.pop();
        smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
        smb.wroteCode();
    }
}
