package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class LocalVarSet extends Item {
    private final LocalVarImpl localVar;
    private final Item value;

    LocalVarSet(final LocalVarImpl localVar, final Item value) {
        this.localVar = localVar;
        this.value = value;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return value.process(node.prev(), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        localVar.checkSlot();
        cb.storeLocal(Util.actualKindOf(localVar.typeKind()), localVar.slot);
    }

    @Override
    public boolean isVoid() {
        return true;
    }
}
