package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

import java.util.Collection;

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
        super(bc, obj);
    }

    /**
     * Generate a call to {@link Collection#size()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr size() {
        return bc.invokeInterface(MD_Collection.size, obj);
    }

    /**
     * Generate a call to {@link Collection#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return bc.invokeInterface(MD_Collection.isEmpty, obj);
    }

    /**
     * Generate a call to {@link Collection#add(Object)}.
     *
     * @param item the object to add (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr add(Expr item) {
        return bc.invokeInterface(MD_Collection.add, obj, item);
    }

    /**
     * Generate a call to {@link Collection#addAll(Collection)}.
     *
     * @param other the collection to add (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr addAll(Expr other) {
        return bc.invokeInterface(MD_Collection.addAll, obj, other);
    }

    /**
     * Generate a call to {@link Collection#clear()}.
     */
    public void clear() {
        bc.invokeInterface(MD_Collection.clear, obj);
    }

    /**
     * Generate a call to {@link Collection#contains(Object)}.
     *
     * @param item the item to test for (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr contains(Expr item) {
        return bc.invokeInterface(MD_Collection.contains, obj, item);
    }

    /**
     * Generate a call to {@link Collection#containsAll(Collection)}.
     *
     * @param other the other collection (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr containsAll(Expr other) {
        return bc.invokeInterface(MD_Collection.containsAll, obj, other);
    }

    /**
     * Generate a call to {@link Collection#iterator()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr iterator() {
        return bc.invokeInterface(MD_Iterable.iterator, obj);
    }

    /**
     * Generate a call to {@link Collection#remove(Object)}.
     *
     * @param item the object to remove (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr remove(Expr item) {
        return bc.invokeInterface(MD_Collection.remove, obj, item);
    }

    /**
     * Generate a call to {@link Collection#removeAll(Collection)}.
     *
     * @param other the other collection (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr removeAll(Expr other) {
        return bc.invokeInterface(MD_Collection.removeAll, obj, other);
    }
}
