package io.quarkus.gizmo2.impl;

import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;

final class FieldSet extends Item {
    private final FieldDeref fieldDeref;
    private final Item value;

    FieldSet(final FieldDeref fieldDeref, final Item value) {
        this.fieldDeref = fieldDeref;
        this.value = value;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return fieldDeref.instance().process(value.process(node.prev(), op), op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.putfield(fieldDeref.owner(), fieldDeref.name(), fieldDeref.desc().type());
    }
}
