package io.quarkus.gizmo2.creator.ops;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Throwable}.
 */
public class ThrowableOps extends ObjectOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the collection instance (must not be {@code null})
     */
    public ThrowableOps(final BlockCreator bc, final Expr obj) {
        super(Throwable.class, bc, obj);
    }

    /**
     * Construct a new subclass instance.
     *
     * @param receiverType the type of the receiver (must not be {@code null})
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    protected ThrowableOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType.asSubclass(Throwable.class), bc, obj);
    }

    /**
     * Generate a call to {@link Throwable#getMessage()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getMessage() {
        return invokeInstance(String.class, "getMessage");
    }

    /**
     * Generate a call to {@link Throwable#getLocalizedMessage()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getLocalizedMessage() {
        return invokeInstance(String.class, "getLocalizedMessage");
    }

    /**
     * Generate a call to {@link Throwable#getCause()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getCause() {
        return invokeInstance(Throwable.class, "getCause");
    }

    /**
     * Generate a call to {@link Throwable#getSuppressed()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getSuppressed() {
        return invokeInstance(Throwable[].class, "getSuppressed");
    }

    /**
     * Generate a call to {@link Throwable#addSuppressed()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public void addSuppressed(Expr exception) {
        invokeInstance(void.class, "addSuppressed", Throwable.class, exception);
    }

}
