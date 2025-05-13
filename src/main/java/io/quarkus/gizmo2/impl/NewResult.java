package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

public class NewResult extends Item {
    private final Item new_;
    private final Item invoke;

    NewResult(Item new_, Item invoke) {
        this.new_ = new_;
        this.invoke = invoke;
    }

    @Override
    public String itemName() {
        return "NewResult:" + new_.type().displayName();
    }

    @Override
    public ClassDesc type() {
        return new_.type();
    }

    @Override
    protected Node forEachDependency(Node node, BiFunction<Item, Node, Node> op) {
        node = node.prev();
        // processes the constructor arguments and the `Dup` (which is the target instance of the `Invoke`)
        node = invoke.process(node, op);
        node = new_.process(node, op);
        return node;
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        // nothing
    }
}
