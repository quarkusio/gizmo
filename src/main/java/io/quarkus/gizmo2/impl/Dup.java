package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;

final class Dup extends ExprImpl {
    private final ExprImpl input;

    Dup(final ExprImpl input) {
        this.input = input;
    }

    public ClassDesc type() {
        return input.type();
    }

    public void pop(final ListIterator<Item> iter) {
        // a dup can always be removed
        iter.remove();
    }

    void verify(final ListIterator<Item> iter) {
        if (peek(iter) != input) {

        }
    }

    protected void process(final ListIterator<Item> iter, final Op op) {
        // don't actually process! just verify the previous item
        iter.previous();
        input.verify(iter);
        // roll back up to it
        iter.next();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (typeKind().slotSize()) {
            case 2 -> cb.dup2();
            case 1 -> cb.dup();
            case 0 -> {}
            default -> throw new IllegalStateException();
        }
    }
}
