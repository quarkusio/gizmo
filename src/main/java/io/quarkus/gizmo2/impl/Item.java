package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;
import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

public abstract non-sealed class Item implements Expr {
    protected final String creationSite = Util.trackCaller();

    public String itemName() {
        return getClass().getSimpleName();
    }

    /**
     * {@return the type of this item}
     */
    public ClassDesc type() {
        return ConstantDescs.CD_void;
    }

    public boolean bound() {
        return true;
    }

    /**
     * Return the node before this item iterating backwards from the given item, popping any unused values in between.
     * If the item is not found, an exception is thrown.
     *
     * @param node a node which is either equal to, or after, the node containing this item (not {@code null})
     * @return the node previous to this item (not {@code null})
     */
    Node verify(Node node) {
        while (node.item() != BlockHeader.INSTANCE) {
            Item actual = node.item();
            if (equals(actual)) {
                // found it
                return forEachDependency(node, Item::verify);
            }
            // we don't care about this one
            node = actual.pop(node);
        }
        throw missing();
    }

    /**
     * Pop or skip this item's result from the stack during a stack cleanup.
     *
     * @param node the current item's node (not {@code null})
     * @return the node before the popped node (not {@code null})
     */
    public Node pop(Node node) {
        assert this == node.item();
        if (!bound()) {
            // remove our dependencies
            Node result = forEachDependency(node, Item::pop);
            if (result == null) {
                throw new IllegalStateException();
            }
            // remove ourselves
            node.remove();
            return result;
        } else if (isVoid()) {
            // no operation; skip over dependencies
            Node result = forEachDependency(node, Item::verify);
            if (result == null) {
                throw new IllegalStateException();
            }
            return result;
        } else {
            // add an explicit pop
            Pop pop = new Pop(this);
            pop.insert(node.next());

            // skip over dependencies
            Node result = forEachDependency(node, Item::verify);
            if (result == null) {
                throw new IllegalStateException();
            }

            return result;
        }
    }

    /**
     * Insert this item into the instruction list before the given node.
     *
     * @param node the node which this item should be inserted before (must not be {@code null})
     * @return the node for the newly inserted item (not {@code null})
     */
    protected Node insert(Node node) {
        return node.insertPrev(this);
    }

    /**
     * If unbound, insert this node into the list after the given node; otherwise, verify this node.
     *
     * @param node the node where this item is expected to be (must not be {@code null})
     * @return the node before the first dependency of this node (not {@code null})
     */
    protected Node insertIfUnbound(Node node) {
        if (!bound()) {
            return forEachDependency(node.insertNext(this), Item::insertIfUnbound);
        } else {
            return verify(node);
        }
    }

    /**
     * Replace this item with the given replacement.
     *
     * @param node the item's node (must not be {@code null})
     * @param replacement the replacement item (must not be {@code null})
     */
    protected void replace(Node node, Item replacement) {
        assert this == node.item();
        node.set(replacement);
    }

    /**
     * Delete this item from the list.
     *
     * @param node the item's node (must not be {@code null})
     */
    protected void remove(Node node) {
        assert this == node.item();
        node.remove();
    }

    /**
     * Process a single item and recurse to its dependencies in reverse order.
     * Any intervening {@code void}-typed expressions are automatically skipped.
     * Any intervening non-{@code void}-typed expressions are popped from the stack.
     *
     * @param node this item's node (not {@code null})
     * @param op the operation (not {@code null})
     * @return the node previous to this one (not {@code null})
     */
    protected Node process(Node node, BiFunction<Item, Node, Node> op) {
        return op.apply(this, node);
    }

    /**
     * Process this item's dependencies in the item list by calling {@link #process(Node, BiFunction)}
     * on each one.
     * Dependencies must be processed from "right to left", which is to say that items that should be on the top
     * of the stack should be processed first.
     * The node passed in to the last dependency should be the node previous to this one.
     * The node passed into each previous dependency should be the node previous to the next dependency.
     * This can normally be done by nesting the method calls.
     *
     * @param node the node for this current item (not {@code null})
     * @param op the operation (not {@code null})
     * @return the node previous to the first dependency (must not be {@code null})
     */
    protected Node forEachDependency(Node node, BiFunction<Item, Node, Node> op) {
        if (node == null) {
            throw missing();
        }
        // no dependencies
        return node.prev();
    }

    private IllegalStateException missing() {
        if (creationSite == null) {
            return new IllegalStateException("Item " + this + " is not at its expected location (declare a LocalVar"
                    + " to store values which are used away from their creation site)" + Util.trackingMessage);
        } else {
            return new IllegalStateException("Item " + this + " created at " + creationSite + " is not at its expected"
                    + " location (declare a LocalVar to store values which are used away from their creation site)");
        }
    }

    public abstract void writeCode(CodeBuilder cb, BlockCreatorImpl block);

    /**
     * {@return true if this node may fall through to the next node}
     */
    public boolean mayFallThrough() {
        return true;
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder b) {
        toShortString(b);
        return b;
    }

    public StringBuilder toShortString(StringBuilder b) {
        return b.append(itemName()).append('@').append(Integer.toHexString(hashCode()));
    }

    public Assignable elem(final Expr index) {
        if (!type().isArray()) {
            throw new IllegalArgumentException("Value type is not array: " + type().displayName());
        }
        return new ArrayDeref(type().componentType(), index);
    }

    public Expr length() {
        if (!type().isArray()) {
            throw new IllegalArgumentException("Value type is not array: " + type().displayName());
        }
        return new Item() {
            @Override
            public ClassDesc type() {
                return ConstantDescs.CD_int;
            }

            @Override
            public boolean bound() {
                return Item.this.bound();
            }

            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return Item.this.process(node.prev(), op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.arraylength();
            }
        };
    }

    public InstanceFieldVar field(final FieldDesc desc) {
        checkNotNullParam("desc", desc);
        return new FieldDeref(desc);
    }

    Item asBound() {
        return bound() ? this : new Item() {
            public ClassDesc type() {
                return Item.this.type();
            }

            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return Item.this.forEachDependency(node, op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                Item.this.writeCode(cb, block);
            }

            public String itemName() {
                return Item.this.itemName() + ":bound";
            }
        };
    }

    public final class FieldDeref extends AssignableImpl implements InstanceFieldVar {
        private final FieldDesc desc;

        private FieldDeref(final FieldDesc desc) {
            this.desc = desc;
        }

        protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
            return Item.this.process(node.prev(), op);
        }

        public boolean bound() {
            return false;
        }

        public FieldDesc desc() {
            return desc;
        }

        @Override
        public ClassDesc type() {
            return desc.type();
        }

        public String itemName() {
            return Item.this.itemName() + "." + desc.name();
        }

        public Item instance() {
            return Item.this;
        }

        Item emitGet(final BlockCreatorImpl block, final MemoryOrder mode) {
            return switch (mode) {
                case AsDeclared -> asBound();
                default -> new Item() {
                    public ClassDesc type() {
                        return FieldDeref.this.type();
                    }

                    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                        return ConstImpl.ofFieldVarHandle(desc).process(FieldDeref.this.process(node.prev(), op), op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Plain -> "get";
                            case Opaque -> "getOpaque";
                            case Acquire -> "getAcquire";
                            case Volatile -> "getVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                                type(),
                                Util.NO_DESCS));
                    }

                    public String itemName() {
                        return FieldDeref.this.itemName() + ":get*";
                    }
                };
            };
        }

        Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
            return switch (mode) {
                case AsDeclared -> new Item() {
                    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                        return Item.this.process(value.process(node.prev(), op), op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.putfield(owner(), name(), desc().type());
                    }

                    public String itemName() {
                        return FieldDeref.this.itemName() + ":set";
                    }
                };
                default -> new Item() {
                    protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
                        return ConstImpl.ofFieldVarHandle(desc)
                                .process(FieldDeref.this.process(value.process(node.prev(), op), op), op);
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
                        return FieldDeref.this.itemName() + ":set*";
                    }
                };
            };
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.getfield(owner(), name(), type());
        }
    }

    public final class ArrayDeref extends AssignableImpl {
        private final ClassDesc componentType;
        private final Item index;

        public ArrayDeref(final ClassDesc componentType, final Expr index) {
            this.componentType = componentType;
            this.index = (Item) index;
        }

        protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
            return Item.this.process(index.process(node.prev(), op), op);
        }

        public Item array() {
            return Item.this;
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
                        return ConstImpl.ofArrayVarHandle(Item.this.type())
                                .process(Item.this.process(index.process(node.prev(), op), op), op);
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
                                Item.this.type(),
                                CD_int));
                    }
                };
            };
        }

        Item emitSet(final BlockCreatorImpl block, final Item value, final MemoryOrder mode) {
            return switch (mode) {
                case AsDeclared, Plain -> new ArrayStore(Item.this, index, value, componentType);
                default -> new Item() {
                    public String itemName() {
                        return "ArrayDeref$SetVolatile" + super.itemName();
                    }

                    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                        return ConstImpl.ofArrayVarHandle(Item.this.type())
                                .process(Item.this.process(index.process(value.process(node.prev(), op), op), op), op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, "setVolatile", MethodTypeDesc.of(
                                type(),
                                Item.this.type(),
                                CD_int));
                    }
                };
            };
        }

        public ClassDesc type() {
            return componentType;
        }

        public boolean bound() {
            return false;
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.arrayLoad(Util.actualKindOf(typeKind()));
        }
    }
}
