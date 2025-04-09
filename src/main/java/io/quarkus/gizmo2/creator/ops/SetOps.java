package io.quarkus.gizmo2.creator.ops;

import java.util.Set;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Set}.
 */
public class SetOps extends CollectionOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the set instance (must not be {@code null})
     */
    public SetOps(final BlockCreator bc, final Expr obj) {
        super(Set.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    protected SetOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType.asSubclass(Set.class), bc, obj);
    }
}
