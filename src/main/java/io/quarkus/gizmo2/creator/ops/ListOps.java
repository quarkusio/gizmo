package io.quarkus.gizmo2.creator.ops;

import java.util.List;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link List}.
 */
public class ListOps extends CollectionOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the list instance (must not be {@code null})
     */
    public ListOps(final BlockCreator bc, final Expr obj) {
        super(List.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    protected ListOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType.asSubclass(List.class), bc, obj);
    }

    /**
     * Generate a call to {@link List#get(int)}.
     *
     * @param index the index of the element to return (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr get(Expr index) {
        return invokeInstance(Object.class, "get", int.class, index);
    }

    /**
     * Generate a call to {@link List#get(int)}.
     *
     * @param index the index of the element to return
     * @return the expression of the result (not {@code null})
     */
    public Expr get(int index) {
        return invokeInstance(Object.class, "get", int.class, Constant.of(index));
    }

}
