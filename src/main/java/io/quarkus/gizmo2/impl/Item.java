package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_VarHandle;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.LValueExpr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

public abstract non-sealed class Item implements Expr {

    public String itemName() {
        return getClass().getSimpleName();
    }

    /**
     * {@return the type of this item}
     */
    public ClassDesc type() {
        return ConstantDescs.CD_void;
    }

    public TypeKind typeKind() {
        return TypeKind.from(type());
    }

    public int slotSize() {
        return typeKind().slotSize();
    }

    public boolean bound() {
        return true;
    }

    /**
     * Move the cursor before the given {@code item}, popping any unused values in between.
     * If the item is not found, an exception is thrown.
     *
     * @param iter the item list iterator (must not be {@code null})
     */
    void verify(ListIterator<Item> iter) {
        while (iter.hasPrevious()) {
            Item actual = iter.previous();
            if (equals(actual)) {
                // found it
                return;
            }
            // we don't care about this one
            actual.pop(iter);
        }
        throw missing();
    }

    /**
     * Pop the item's result from the stack during a stack cleanup.
     * This item will have just been returned by {@code iter.previous()}.
     *
     * @param iter the item list iterator (not {@code null})
     */
    public void pop(ListIterator<Item> iter) {
        if (type().equals(ConstantDescs.CD_void)) {
            // no operation
        } else if (! bound()) {
            // we can safely remove ourselves
            iter.remove();
            // and our dependencies
            processDependencies(iter, Op.POP);
        } else {
            iter.next();
            // add an explicit pop
            Pop pop = new Pop(this);
            pop.insert(iter);
            // move back before `this` again
            iter.previous();
        }
    }

    /**
     * Insert this item into the instruction list at the current point
     * and position the cursor immediately before it.
     *
     * @param iter the item list iterator (not {@code null})
     */
    protected void insert(ListIterator<Item> iter) {
        iter.add(this);
        iter.previous();
    }

    /**
     * Peek at the previous item in the list iterator.
     * If there is no previous item, an exception is thrown.
     * The item will be considered to be the "last returned" for the purposes of {@linkplain ListIterator#remove() removal}
     * or {@linkplain ListIterator#add addition}.
     *
     * @param iter the list iterator (must not be {@code null})
     * @return the previous item (not {@code null})
     * @throws NoSuchElementException if there is no previous item
     */
    protected static Item peek(ListIterator<Item> iter) throws NoSuchElementException {
        Item item = iter.previous();
        iter.next();
        return item;
    }

    /**
     * Replace this item with the given replacement.
     * This item must be the previous item in the iterator.
     *
     * @param iter        the list iterator (must not be {@code null})
     * @param replacement the replacement item (must not be {@code null})
     */
    protected void replace(ListIterator<Item> iter, Item replacement) {
        Item item = iter.previous();
        if (item != this) {
            throw new IllegalStateException("Item mismatch");
        }
        iter.remove();
        iter.add(replacement);
    }

    /**
     * Process a single item and recurse to its dependencies in reverse order.
     * Any intervening {@code void}-typed expressions are automatically skipped.
     * Any intervening non-{@code void}-typed expressions are popped from the stack.
     *
     * @param iter the item iterator (not {@code null})
     * @param op   the operation (not {@code null})
     */
    protected void process(ListIterator<Item> iter, Op op) {
        switch (op) {
            case VERIFY -> {
                // expect each dependency to exist in order
                verify(iter);
                processDependencies(iter, op);
            }
            case INSERT -> {
                // insert all unbound dependencies
                if (! bound()) {
                    insert(iter);
                    processDependencies(iter, op);
                }
            }
            case POP -> {
                // remove this value and its dependencies
                pop(iter);
                processDependencies(iter, op);
            }
        }
    }

    /**
     * Process this item's dependencies in the item list by calling {@link #process(ListIterator, Op)}
     * on each one.
     * Dependencies must be processed from "right to left", which is to say that items that should be on the top
     * of the stack should be processed first.
     *
     * @param iter the item iterator (not {@code null})
     * @param op   the operation (not {@code null})
     */
    protected void processDependencies(ListIterator<Item> iter, Op op) {
        // no dependencies
    }

    static IllegalStateException missing() {
        return new IllegalStateException("Item is not at its expected location (use a variable to store values which are used away from their definition site");
    }

    public abstract void writeCode(CodeBuilder cb, BlockCreatorImpl block);

    public boolean exitsAll() {
        return false;
    }

    public boolean exitsBlock() {
        return exitsAll();
    }

    public final String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(StringBuilder b) {
        toShortString(b);
        return b;
    }

    public StringBuilder toShortString(StringBuilder b) {
        return b.append(itemName()).append('@').append(Integer.toHexString(hashCode()));
    }

    void requireSameType(final Expr a, final Expr b) {
        if (! a.type().equals(b.type())) {
            throw new IllegalArgumentException("Type mismatch between " + a.type() + " and " + b.type());
        }
    }

    public LValueExpr elem(final Expr index) {
        if (! type().isArray()) {
            throw new IllegalArgumentException("Value type is not array");
        }
        final ClassDesc type = type().componentType();
        return new ArrayDeref(type, index);
    }

    public Expr length() {
        if (! type().isArray()) {
            throw new IllegalArgumentException("Length is only allowed on arrays (expression type is actually " + type() + ")");
        }
        return null;
    }

    public InstanceFieldVar field(final FieldDesc desc) {
        return new FieldDeref(Objects.requireNonNull(desc, "desc"));
    }

    Item asBound() {
        return bound() ? this : new Item() {
            public ClassDesc type() {
                return Item.this.type();
            }

            public boolean bound() {
                return true;
            }

            protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                Item.this.processDependencies(iter, op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                Item.this.writeCode(cb, block);
            }
        };
    }

    protected enum Op {
        INSERT,
        VERIFY,
        POP,
        ;
    }

    public final class FieldDeref extends LValueExprImpl implements InstanceFieldVar {
        private final FieldDesc desc;

        private FieldDeref(final FieldDesc desc) {
            this.desc = desc;
        }

        protected void processDependencies(final ListIterator<Item> iter, final Op op) {
            Item.this.process(iter, op);
        }

        public FieldDesc desc() {
            return desc;
        }

        Item emitGet(final BlockCreatorImpl block, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared -> asBound();
                default -> new Item() {
                    public ClassDesc type() {
                        return FieldDeref.this.type();
                    }

                    public boolean bound() {
                        return true;
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        Item.this.process(iter, op);
                        ConstantImpl.ofFieldVarHandle(desc).process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Plain -> "get";
                            case Opaque -> "getOpaque";
                            case Acquire -> "getAcquire";
                            case Volatile -> "getVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                            type()
                        ));
                    }
                };
            };
        }

        Item emitSet(final BlockCreatorImpl block, final Item value, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared -> new Item() {
                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        Item.this.process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.putfield(owner(), name(), type());
                    }
                };
                default -> new Item() {
                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        Item.this.process(iter, op);
                        ConstantImpl.ofFieldVarHandle(desc).process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, switch (mode) {
                            case Plain -> "set";
                            case Opaque -> "setOpaque";
                            case Release -> "setRelease";
                            case Volatile -> "setVolatile";
                            default -> throw new IllegalStateException();
                        }, MethodTypeDesc.of(
                            type()
                        ));
                    }
                };
            };
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.getfield(owner(), name(), type());
        }
    }

    final class ArrayDeref extends LValueExprImpl {
        private final ClassDesc type;
        private final Item index;

        public ArrayDeref(final ClassDesc type, final Expr index) {
            this.type = type;
            this.index = (Item) index;
        }

        protected void processDependencies(final ListIterator<Item> iter, final Op op) {
            index.process(iter, op);
            Item.this.process(iter, op);
        }

        Item emitGet(final BlockCreatorImpl block, final AccessMode mode) {
            if (! mode.validForReads()) {
                throw new IllegalArgumentException("Invalid mode " + mode);
            }
            return switch (mode) {
                case AsDeclared, Plain -> asBound();
                default -> new Item() {
                    public String itemName() {
                        return "ArrayDeref$Get" + super.itemName();
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        index.processDependencies(iter, op);
                        Item.this.processDependencies(iter, op);
                        ConstantImpl.ofArrayVarHandle(Item.this.type()).processDependencies(iter, op);
                    }

                    public ClassDesc type() {
                        return type;
                    }

                    public boolean bound() {
                        return true;
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
                            CD_int
                        ));
                    }
                };
            };
        }

        Item emitSet(final BlockCreatorImpl block, final Item value, final AccessMode mode) {
            return switch (mode) {
                case AsDeclared, Plain -> new ArrayStore(Item.this, index, value);
                default -> new Item() {
                    public String itemName() {
                        return "ArrayDeref$SetVolatile" + super.itemName();
                    }

                    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
                        value.process(iter, op);
                        index.process(iter, op);
                        Item.this.process(iter, op);
                        ConstantImpl.ofArrayVarHandle(Item.this.type()).process(iter, op);
                    }

                    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                        cb.invokevirtual(CD_VarHandle, "setVolatile", MethodTypeDesc.of(
                            type(),
                            Item.this.type(),
                            CD_int
                        ));
                    }
                };
            };
        }

        public ClassDesc type() {
            return type;
        }

        public boolean bound() {
            return false;
        }

        public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
            cb.arrayLoad(typeKind());
        }
    }
}
