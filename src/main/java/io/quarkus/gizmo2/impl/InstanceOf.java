package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class InstanceOf extends Item {
    private final Item input;
    private final ClassDesc type;

    InstanceOf(final Expr input, final ClassDesc type) {
        this.input = (Item) input;
        this.type = type;
    }

    public ClassDesc type() {
        return ConstantDescs.CD_boolean;
    }

    public boolean bound() {
        return input.bound();
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return input.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.instanceOf(type);
    }
}
