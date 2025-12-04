package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;

import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.smallrye.classfile.CodeBuilder;

final class Dup extends Item {
    private final Item input;

    Dup(final Item input) {
        this.input = input;
    }

    protected void computeType() {
        initType(input.type());
        if (input.hasGenericType()) {
            initGenericType(input.genericType());
        }
    }

    public void pop(final ListIterator<Item> itr) {
        // a dup can always be safely removed in lieu of a pop
        Util.ensureAfter(itr, this);
        itr.set(Nop.FILL);
    }

    void verify(ListIterator<Item> itr) {
        super.verify(itr);
        // wind it forward
        while (itr.hasNext() && !itr.next().equals(this)) {
        }
        itr.previous();
    }

    protected void process(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        super.process(itr, op);
        // wind it forward
        while (itr.hasNext() && !itr.next().equals(this)) {
        }
        itr.previous();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        switch (typeKind().slotSize()) {
            case 2 -> {
                cb.dup2();
                smb.push(type());
                smb.wroteCode();
            }
            case 1 -> {
                cb.dup();
                smb.push(type());
                smb.wroteCode();
            }
            case 0 -> {
            }
            default -> throw impossibleSwitchCase(typeKind().asLoadable());
        }
    }
}
