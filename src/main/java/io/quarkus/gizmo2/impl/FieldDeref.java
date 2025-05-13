package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.desc.FieldDesc;

public final class FieldDeref extends AssignableImpl implements InstanceFieldVar {
    private final Item item;
    private final FieldDesc desc;
    private boolean bound;

    FieldDeref(final Item item, final FieldDesc desc) {
        this.item = item;
        this.desc = desc;
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return item.process(node.prev(), op);
    }

    public boolean bound() {
        return bound;
    }

    protected void bind() {
        if (item.bound()) {
            bound = true;
        }
    }

    public FieldDesc desc() {
        return desc;
    }

    @Override
    public ClassDesc type() {
        return desc.type();
    }

    public String itemName() {
        return item.itemName() + "." + desc.name();
    }

    public Item instance() {
        return item;
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> asBound();
            default -> new FieldGetViaHandle(this, mode);
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared -> new FieldSet(this, value);
            default -> new FieldSetViaHandle(this, value, mode);
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getfield(owner(), name(), type());
    }
}
