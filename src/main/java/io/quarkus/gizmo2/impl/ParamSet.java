package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class ParamSet extends Item {
    private final ParamVarImpl paramVar;
    private final Item value;

    ParamSet(final ParamVarImpl paramVar, final Item value) {
        this.paramVar = paramVar;
        this.value = value;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return value.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.storeLocal(Util.actualKindOf(paramVar.typeKind()), paramVar.slot());
    }
}
