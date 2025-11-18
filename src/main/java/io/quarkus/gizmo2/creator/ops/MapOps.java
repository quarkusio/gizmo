package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

import java.util.Map;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Map}.
 */
public class MapOps extends ObjectOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the list instance (must not be {@code null})
     */
    public MapOps(final BlockCreator bc, final Expr obj) {
        super(bc, obj);
    }

    /**
     * Generate a call to {@link Map#get(Object)}.
     *
     * @param key the mapping key (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr get(Expr key) {
        return bc.invokeInterface(MD_Map.get, obj, key);
    }

    /**
     * Generate a call to {@link Map#put(Object, Object)}.
     *
     * @param key the mapping key (must not be {@code null})
     * @param value the mapping value (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr put(Expr key, Expr value) {
        return bc.invokeInterface(MD_Map.put, obj, key, value);
    }

    /**
     * Generate a call to {@link Map#remove(Object)}.
     *
     * @param key the mapping key to remove (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr remove(Expr key) {
        return bc.invokeInterface(MD_Map.remove, obj, key);
    }

    /**
     * Generate a call to {@link Map#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return bc.invokeInterface(MD_Map.isEmpty, obj);
    }

    /**
     * Generate a call to {@link Map#size()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr size() {
        return bc.invokeInterface(MD_Map.size, obj);
    }

    /**
     * Generate a call to {@link Map#containsKey(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr containsKey(Expr key) {
        return bc.invokeInterface(MD_Map.containsKey, obj, key);
    }

    /**
     * Generate a call to {@link Map#clear()}.
     */
    public void clear() {
        bc.invokeInterface(MD_Map.clear, obj);
    }

    /**
     * Generate a call to {@link Map#keySet()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr keySet() {
        return bc.invokeInterface(MD_Map.keySet, obj);
    }

    /**
     * Generate a call to {@link Map#values()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr values() {
        return bc.invokeInterface(MD_Map.values, obj);
    }

    /**
     * Generate a call to {@link Map#entrySet()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr entrySet() {
        return bc.invokeInterface(MD_Map.entrySet, obj);
    }
}
