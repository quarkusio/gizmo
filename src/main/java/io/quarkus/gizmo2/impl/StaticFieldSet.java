package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class StaticFieldSet extends Item {
    private final StaticFieldVarImpl staticFieldVar;
    private final Item value;

    StaticFieldSet(final StaticFieldVarImpl staticFieldVar, final Item value) {
        this.staticFieldVar = staticFieldVar;
        this.value = value;
    }

    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
        return value.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.putstatic(staticFieldVar.owner(), staticFieldVar.name(), staticFieldVar.desc().type());
    }
}
