package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.GenericTypes.*;
import static io.smallrye.common.constraint.Assert.checkNotNullParam;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.function.BiConsumer;

import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.TypeAnnotation;

/**
 * A single item in the program list of a {@linkplain BlockCreatorImpl block}.
 * <p>
 * The program list is a list of logical instructions that are executed in order
 * from top to bottom.
 * Each item has a {@linkplain #type() type}; if the type of an item is not {@code void},
 * then execution of that item will push its result on to the logical program stack.
 * <p>
 * Some items have input dependencies (also expressed as items), constrained as follows:
 * <ul>
 * <li>The dependency inputs of each item must appear before the consuming item in the program list</li>
 * <li>The dependency items must appear in the same order that they are consumed</li>
 * <li>The dependency items must appear consecutively, with no intervening non-{@code void} items
 * (note that intervening {@code void} items may have non-{@code void} dependencies; this is allowed)</li>
 * </ul>
 * Each item consumes its dependencies from the logical program stack before producing its result.
 * <p>
 * <h3>Bound and unbound items</h3>
 * An item may be <em>bound</em> or <em>unbound</em>.
 * A <em>bound</em> item must be explicitly inserted into the instruction list.
 * An <em>unbound</em> item may not initially be directly present in the instruction list,
 * either because it is <em>reusable</em> (like a {@linkplain Var variable} or a {@linkplain Const constant})
 * or because it is created as a part of another operation (such as an implicit {@link Cast}).
 * <p>
 * When an item is {@linkplain #insert inserted}, usually at the end of the instruction list,
 * its dependencies are {@linkplain #forEachDependency(ListIterator, BiConsumer) traversed} in reverse
 * order.
 * If any dependency item is found to be <em>unbound</em>, it is {@linkplain #insertIfUnbound(ListIterator) inserted in turn}.
 * This may cause the item to {@linkplain #bind() become bound} if it is not a reusable item.
 * <p>
 * <h3>Instruction list operations</h3>
 * The major instruction list operations are:
 * <ul>
 * <li>{@link #insert} - insert this item in the instruction list at the iterator position, and {@link #bind} it</li>
 * <li>{@link #pop} - pop the result of this item (at the iterator position) from the logical stack</li>
 * <li>{@link #remove} - remove this item from the instruction list at the iterator position</li>
 * <li>{@link #revoke} - remove this item, and pop all of its input dependencies, from the instruction list at the iterator
 * position</li>
 * <li>{@link #process} - perform an action on this item</li>
 * <li>{@link #insertIfUnbound} - insert this item <em>if</em> it is unbound</li>
 * <li>{@link #forEachDependency} - perform an operation on each dependency of this item</li>
 * </ul>
 * Instructions that are removed or revoked may be replaced with a {@linkplain Nop#FILL fill item}.
 * Likewise, inserting an instruction at a point adjacent to a fill item will replace that item rather than inserting
 * into the list.
 * Fill items do not produce any bytecode and are just placeholders in the instruction list.
 * This is done to avoid excessive array copying caused by mid-list insertion and removal.
 * <p>
 * <h3>Emitting the program</h3>
 * Once the program is built, the code of the program is {@linkplain #writeCode(CodeBuilder, BlockCreatorImpl, StackMapBuilder)
 * emitted}.
 * Each item writes the bytecodes corresponding to its operation, and also updates the {@linkplain StackMapBuilder stack map}
 * accordingly.
 */
public abstract non-sealed class Item implements Expr {
    protected final String creationSite = Util.trackCaller();
    private ClassDesc type;
    private GenericType genericType;

    protected Item() {
    }

    protected Item(final ClassDesc type) {
        this.type = type;
    }

    protected Item(final GenericType genericType) {
        this.genericType = genericType;
    }

    protected Item(final ClassDesc type, final GenericType genericType) {
        this.type = type;
        this.genericType = genericType;
    }

    public String itemName() {
        return getClass().getSimpleName();
    }

    /**
     * {@return the type of this item}
     */
    public final ClassDesc type() {
        ClassDesc type = this.type;
        if (type != null) {
            return type;
        }
        GenericType genericType = this.genericType;
        if (genericType != null) {
            return this.type = genericType.desc();
        }
        computeType();
        type = this.type;
        if (type != null) {
            return type;
        }
        genericType = this.genericType;
        if (genericType != null) {
            return this.type = genericType.desc();
        }
        throw new IllegalStateException("Type not computed");
    }

    protected final void initType(ClassDesc type) {
        this.type = type;
    }

    protected final void initGenericType(GenericType genericType) {
        this.genericType = genericType;
    }

    public final GenericType genericType() {
        GenericType genericType = this.genericType;
        if (genericType != null) {
            return genericType;
        }
        ClassDesc type = this.type;
        if (type != null) {
            return this.genericType = GenericType.of(type);
        }
        computeType();
        genericType = this.genericType;
        if (genericType != null) {
            return genericType;
        }
        type = this.type;
        if (type != null) {
            return this.genericType = GenericType.of(type);
        }
        throw new IllegalStateException("Type not computed");
    }

    public final boolean hasGenericType() {
        GenericType genericType = this.genericType;
        if (genericType != null) {
            return true;
        }
        if (type != null) {
            // simple type
            return false;
        }
        // not initialized yet
        computeType();
        return this.genericType != null;
    }

    protected void computeType() {
        initType(CD_void);
        initGenericType(GT_void);
    }

    /**
     * {@return {@code true} if the expression is bound to a single point in the instruction list, or {@code false} otherwise}
     * Bound expressions may not be used again.
     */
    public boolean bound() {
        return true;
    }

    /**
     * Return the node before this item iterating backwards from the given item, popping any unused values in between.
     * If the item is not found, an exception is thrown.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     */
    void verify(ListIterator<Item> itr) {
        while (itr.hasPrevious()) {
            Item actual = itr.previous();
            if (equals(actual)) {
                // found it
                forEachDependency(itr, Item::verify);
                return;
            }
            // we don't care about this one
            itr.next();
            actual.pop(itr);
        }
        throw missing();
    }

    /**
     * Pop or skip this item's result from the stack during a stack cleanup.
     * On return, the iterator is positioned before this item's location.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     */
    public void pop(ListIterator<Item> itr) {
        if (isVoid()) {
            // skip myself (no pop needed for void items)
            verify(itr);
            return;
        } else if (!bound()) {
            if (itr.hasPrevious()) {
                Item test = Util.peekPrevious(itr);
                while (test.isVoid()) {
                    // skip over void items
                    test.verify(itr);
                    test = Util.peekPrevious(itr);
                }
                if (equals(test)) {
                    // found
                    itr.set(Nop.FILL);
                    itr.previous();
                }
            }
            // remove our dependencies
            forEachDependency(itr, Item::pop);
            return;
        } else {
            // add an explicit pop
            Pop pop = new Pop(this);
            pop.insert(itr);
            // move before our node
            verify(itr);
            return;
        }
    }

    /**
     * Revoke this node from the instruction list, popping any dependencies that became unused as a result.
     * The iterator is positioned before the first dependency.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     */
    public void revoke(ListIterator<Item> itr) {
        remove(itr);
        forEachDependency(itr, Item::pop);
    }

    /**
     * Remove this node from the instruction list.
     * The iterator is positioned before the former position of this node.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     */
    protected void remove(ListIterator<Item> itr) {
        while (itr.hasPrevious()) {
            Item actual = itr.previous();
            if (equals(actual)) {
                // found
                itr.set(Nop.FILL);
                return;
            } else if (actual.isVoid()) {
                // skip it
                itr.next();
                actual.verify(itr);
            } else {
                throw missing();
            }
        }
        throw missing();
    }

    /**
     * Insert this item into the instruction list at the given position.
     * On return, the iterator should be positioned just before the newly inserted item.
     *
     * @param itr the iterator whose cursor is positioned <em>before</em> this item's new position (must not be {@code null})
     */
    protected void insert(ListIterator<Item> itr) {
        if (itr.hasNext() && Util.peekNext(itr) == Nop.FILL) {
            // avoid array copies
            itr.set(this);
        } else if (itr.hasPrevious() && Util.peekPrevious(itr) == Nop.FILL) {
            // avoid array copies
            itr.set(this);
            itr.previous();
        } else {
            itr.add(this);
            itr.previous();
        }
        bind();
        Util.ensureBefore(itr, this);
    }

    /**
     * If unbound, insert this node into the list; otherwise, verify this node.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     */
    protected void insertIfUnbound(final ListIterator<Item> itr) {
        if (!bound()) {
            insert(itr);
            forEachDependency(itr, Item::insertIfUnbound);
        } else {
            verify(itr);
        }
    }

    /**
     * If unbound and non-reusable, set this item's status to "bound".
     */
    protected void bind() {
    }

    /**
     * Process a single item and recurse to its dependencies in reverse order.
     * Any intervening {@code void}-typed expressions are automatically skipped.
     * Any intervening non-{@code void}-typed expressions are popped from the stack.
     * After this call, the iterator should be positioned just before this node's first dependency.
     *
     * @param itr the iterator whose cursor is positioned after this item (must not be {@code null})
     * @param op the operation (not {@code null})
     */
    protected void process(ListIterator<Item> itr, BiConsumer<Item, ListIterator<Item>> op) {
        op.accept(this, itr);
    }

    /**
     * Process this item's dependencies in the item list by calling {@link #process)}
     * on each one.
     * Dependencies must be processed from "right to left", which is to say that items that should be on the top
     * of the stack should be processed first.
     * The node passed in to the last dependency should be the node previous to this one.
     * The node passed into each previous dependency should be the node previous to the next dependency.
     * This can normally be done by nesting the method calls.
     *
     * @param itr the iterator whose cursor is positioned before this item (must not be {@code null})
     * @param op the operation (not {@code null})
     */
    protected void forEachDependency(ListIterator<Item> itr, BiConsumer<Item, ListIterator<Item>> op) {
        if (!itr.hasPrevious()) {
            throw missing();
        }
        // no dependencies
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

    public abstract void writeCode(CodeBuilder cb, BlockCreatorImpl block, StackMapBuilder smb);

    public void writeAnnotations(final RetentionPolicy retention, ArrayList<TypeAnnotation> annotations) {
    }

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
        return b.append(itemName());
    }

    public Assignable elem(final Expr index) {
        if (!type().isArray()) {
            throw new IllegalArgumentException("Value type is not array: " + type().displayName());
        }
        return new ArrayDeref(this, index);
    }

    public Expr length() {
        if (!type().isArray()) {
            throw new IllegalArgumentException("Value type is not array: " + type().displayName());
        }
        return new ArrayLength(this);
    }

    public InstanceFieldVar field(final FieldDesc desc) {
        checkNotNullParam("desc", desc);
        return new FieldDeref(this, desc, null);
    }

    public InstanceFieldVar field(final FieldDesc desc, final GenericType genericType) {
        checkNotNullParam("desc", desc);
        checkNotNullParam("genericType", genericType);
        if (!Util.equals(desc.type(), genericType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match field type %s".formatted(genericType, desc.type()));
        }
        return new FieldDeref(this, desc, genericType);
    }

    Item asBound() {
        return bound() ? this : new BoundItem(this);
    }
}
