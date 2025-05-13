package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

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
            default -> new Item() {
                protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
                    return ConstImpl.ofFieldVarHandle(desc)
                            .process(io.quarkus.gizmo2.impl.FieldDeref.this.process(value.process(node.prev(), op), op), op);
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.invokevirtual(CD_VarHandle, switch (mode) {
                        case Plain -> "set";
                        case Opaque -> "setOpaque";
                        case Release -> "setRelease";
                        case Volatile -> "setVolatile";
                        default -> throw new IllegalStateException();
                    }, MethodTypeDesc.of(
                            desc().type(),
                            Util.NO_DESCS));
                }

                public String itemName() {
                    return io.quarkus.gizmo2.impl.FieldDeref.this.itemName() + ":set*";
                }
            };
        };
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getfield(owner(), name(), type());
    }
}
