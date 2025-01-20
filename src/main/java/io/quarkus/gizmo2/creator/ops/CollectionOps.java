package io.quarkus.gizmo2.creator.ops;

import java.util.Collection;
import java.util.Iterator;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Collection}.
 */
public class CollectionOps extends ObjectOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the collection instance (must not be {@code null})
     */
    public CollectionOps(final BlockCreator bc, final Expr obj) {
        super(Collection.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc           the block creator (must not be {@code null})
     * @param obj          the receiver object (must not be {@code null})
     */
    protected CollectionOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType.asSubclass(Collection.class), bc, obj);
    }

    /**
     * Generate a call to {@link Collection#size()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr size() {
        return invokeInstance(int.class, "size");
    }

    /**
     * Generate a call to {@link Collection#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return invokeInstance(boolean.class, "isEmpty");
    }

    /**
     * Generate a call to {@link Collection#add(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr add(Expr item) {
        return invokeInstance(boolean.class, "add", Object.class, item);
    }

    /**
     * Generate a call to {@link Collection#addAll(Collection)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr addAll(Expr other) {
        return invokeInstance(boolean.class, "addAll", Collection.class, other);
    }

    /**
     * Generate a call to {@link Collection#clear()}.
     */
    public void clear() {
        invokeInstance("clear");
    }

    /**
     * Generate a call to {@link Collection#contains(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr contains(Expr item) {
        return invokeInstance(boolean.class, "contains", Object.class, item);
    }

    /**
     * Generate a call to {@link Collection#containsAll(Collection)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr containsAll(Expr other) {
        return invokeInstance(boolean.class, "containsAll", Collection.class, other);
    }

    /**
     * Generate a call to {@link Collection#iterator()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr iterator() {
        return invokeInstance(Iterator.class, "iterator");
    }

    /**
     * Generate a call to {@link Collection#remove(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr remove(Expr item) {
        return invokeInstance(boolean.class, "remove", Object.class, item);
    }

    /**
     * Generate a call to {@link Collection#removeAll(Collection)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr removeAll(Expr other) {
        return invokeInstance(boolean.class, "removeAll", Collection.class, other);
    }
}
