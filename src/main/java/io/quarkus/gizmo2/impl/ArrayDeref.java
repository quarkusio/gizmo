package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.MemoryOrder;

public final class ArrayDeref extends AssignableImpl {
    private final Item item;
    private final ClassDesc componentType;
    private final Item index;
    private boolean bound;

    ArrayDeref(final Item item, final ClassDesc componentType, final Expr index) {
        this.item = item;
        this.componentType = componentType;
        this.index = convert(index, CD_int);
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return item.process(index.process(node.prev(), op), op);
    }

    public Item array() {
        return item;
    }

    public Item index() {
        return index;
    }

    Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
        if (!mode.validForReads()) {
            throw new IllegalArgumentException("Invalid mode " + mode);
        }
        return switch (mode) {
            case AsDeclared, Plain -> asBound();
            default -> new ArrayLoadViaHandle(this, mode);
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared, Plain -> new ArrayStore(item, index, value, componentType);
            default -> new ArrayStoreViaHandle(this, value);
        };
    }

    public ClassDesc type() {
        return componentType;
    }

    protected void bind() {
        if (item.bound() || index.bound()) {
            bound = true;
        }
    }

    public boolean bound() {
        return bound;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.arrayLoad(Util.actualKindOf(typeKind()));
    }
}
