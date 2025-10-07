package io.quarkus.gizmo2.impl;

import java.lang.constant.DynamicCallSiteDesc;
import java.util.List;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class InvokeDynamic extends Item {
    private final List<? extends Expr> args;
    private final DynamicCallSiteDesc callSiteDesc;

    InvokeDynamic(final List<? extends Expr> args, final DynamicCallSiteDesc callSiteDesc) {
        super(callSiteDesc.invocationType().returnType());
        this.args = args;
        this.callSiteDesc = callSiteDesc;
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        node = node.prev();
        for (int i = args.size() - 1; i >= 0; i--) {
            final Item arg = (Item) args.get(i);
            node = arg.process(node, op);
        }
        return node;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.invokedynamic(callSiteDesc);
    }
}
