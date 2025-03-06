package io.quarkus.gizmo2.creator.ops;

import java.util.Iterator;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on an {@link Iterator}.
 */
public class IteratorOps extends ObjectOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator to wrap (must not be {@code null})
     * @param obj the iterator object (must not be {@code null})
     */
    public IteratorOps(final BlockCreator bc, final Expr obj) {
        super(Iterator.class, bc, obj);
    }

    /**
     * Construct a new instance.
     *
     * @param receiverType the type class of the receiver (must not be {@code null})
     * @param bc the block creator to wrap (must not be {@code null})
     * @param obj the iterator object (must not be {@code null})
     */
    protected IteratorOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType, bc, obj);
    }

    /**
     * Call {@link Iterator#hasNext()}.
     *
     * @return the boolean result of the method call (not {@code null})
     */
    public Expr hasNext() {
        return invokeInstance(boolean.class, "hasNext");
    }

    /**
     * Call {@link Iterator#next()}.
     *
     * @return the next iterator item (not {@code null})
     */
    public Expr next() {
        return invokeInstance(Object.class, "next");
    }

    /**
     * Call {@link Iterator#remove}.
     */
    public void remove() {
        invokeInstance(void.class, "remove");
    }

    /**
     * Call {@link Iterator#forEachRemaining(Consumer)}.
     *
     * @param action the consumer to pass to the function (not {@code null})
     */
    public void forEachRemaining(Expr action) {
        invokeInstance(void.class, "forEachRemaining", Consumer.class, action);
    }
}
