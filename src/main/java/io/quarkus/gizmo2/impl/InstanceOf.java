package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class InstanceOf extends ExprImpl {
    private final ExprImpl input;
    private final ClassDesc type;

    InstanceOf(final Expr input, final ClassDesc type) {
        this.input = (ExprImpl) input;
        this.type = type;
    }

    public ClassDesc type() {
        return type;
    }

    public boolean bound() {
        return input.bound();
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        input.process(iter, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.instanceOf(type);
    }
}
