package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.ArrayList;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.InstanceFieldVar;
import io.quarkus.gizmo2.desc.FieldDesc;

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
     * Revoke this node from the instruction list, popping any dependencies that became unused as a result.
     *
     * @param node the current item's node (not {@code null})
     * @return the node before the first dependency of this node (not {@code null})
     */
    public Node revoke(Node node) {
        assert this == node.item();
        Node prev = forEachDependency(node, Item::pop);
        remove(node);
        return prev;
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
            Node prev = forEachDependency(node.insertNext(this), Item::insertIfUnbound);
            bind();
            return prev;
        } else {
            return verify(node);
        }
    }

    /**
     * If unbound and non-reusable, set this item's status to "bound".
     */
    protected void bind() {
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
        return new ArrayDeref(this, ((GenericType.OfArray) genericType()).componentType(), index);
    }

    public Expr length() {
        if (!type().isArray()) {
            throw new IllegalArgumentException("Value type is not array: " + type().displayName());
        }
        return new ArrayLength(this);
    }

    public InstanceFieldVar field(final FieldDesc desc, final GenericType genericType) {
        checkNotNullParam("desc", desc);
        checkNotNullParam("genericType", genericType);
        if (!desc.type().equals(genericType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match field type %s".formatted(genericType, desc.type()));
        }
        return new FieldDeref(this, desc, genericType);
    }

    Item asBound() {
        return bound() ? this : new BoundItem(this);
    }
}
