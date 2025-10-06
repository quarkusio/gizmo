package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

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
