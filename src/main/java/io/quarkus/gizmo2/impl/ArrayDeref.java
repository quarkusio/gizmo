package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

public final class ArrayDeref extends AssignableImpl {
    private final Item item;
    private final ClassDesc componentType;
    private final Item index;
    private boolean bound;

    ArrayDeref(final Item item, final ClassDesc componentType, final Expr index) {
        this.item = item;
        this.componentType = componentType;
        this.index = (Item) index;
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
            default -> new Item() {
                public String itemName() {
                    return "ArrayDeref$Get" + super.itemName();
                }

                protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                    return ConstImpl.ofArrayVarHandle(item.type())
                            .process(item.process(index.process(node.prev(), op), op), op);
                }

                public ClassDesc type() {
                    return componentType;
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.invokevirtual(CD_VarHandle, switch (mode) {
                        case Opaque -> "getOpaque";
                        case Acquire -> "getAcquire";
                        case Volatile -> "getVolatile";
                        default -> throw new IllegalStateException();
                    }, MethodTypeDesc.of(
                            type(),
                            item.type(),
                            CD_int));
                }
            };
        };
    }

    Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
        return switch (mode) {
            case AsDeclared, Plain -> new ArrayStore(item, index, value, componentType);
            default -> new Item() {
                public String itemName() {
                    return "ArrayDeref$SetVolatile" + super.itemName();
                }

                protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                    return ConstImpl.ofArrayVarHandle(item.type())
                            .process(item.process(index.process(value.process(node.prev(), op), op), op), op);
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.invokevirtual(CD_VarHandle, "setVolatile", MethodTypeDesc.of(
                            type(),
                            item.type(),
                            CD_int));
                }
            };
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
