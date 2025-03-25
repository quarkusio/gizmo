package io.quarkus.gizmo2.creator.ops;

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
        super(Map.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    protected MapOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType.asSubclass(Map.class), bc, obj);
    }

    /**
     * Generate a call to {@link Map#get(Object)}.
     *
     * @param key
     * @return the expression of the result (not {@code null})
     */
    public Expr get(Expr key) {
        return invokeInstance(Object.class, "get", Object.class, key);
    }

    /**
     * Generate a call to {@link Map#put(Object, Object)}.
     *
     * @param key
     * @param value
     * @return the expression of the result (not {@code null})
     */
    public Expr put(Expr key, Expr value) {
        return invokeInstance(Object.class, "put", Object.class, Object.class, key, value);
    }

    /**
     * Generate a call to {@link Map#remove(Object)}.
     *
     * @param key
     * @return the expression of the result (not {@code null})
     */
    public Expr remove(Expr key) {
        return invokeInstance(Object.class, "remove", Object.class, key);
    }

    /**
     * Generate a call to {@link Map#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return invokeInstance(boolean.class, "isEmpty");
    }

    /**
     * Generate a call to {@link Map#size()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr size() {
        return invokeInstance(int.class, "size");
    }

    /**
     * Generate a call to {@link Map#containsKey(Object)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr containsKey(Expr key) {
        return invokeInstance(boolean.class, "containsKey", Object.class, key);
    }

    /**
     * Generate a call to {@link Map#clear()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr clear() {
        return invokeInstance(void.class, "clear");
    }

}
