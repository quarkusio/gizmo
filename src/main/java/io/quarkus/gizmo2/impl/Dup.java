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

    protected void process(final ListIterator<Item> iter, final Op op) {
        // don't actually process! just verify the previous item
        iter.previous();
        input.verify(iter);
        // roll back up to it
        iter.next();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (typeKind().slotSize() == 2) {
            cb.dup2();
        } else {
            cb.dup();
        }
    }
}
