package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

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
        return bc.invokeVirtual(MD_Throwable.getMessage, obj);
    }

    /**
     * Generate a call to {@link Throwable#getLocalizedMessage()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getLocalizedMessage() {
        return bc.invokeVirtual(MD_Throwable.getLocalizedMessage, obj);
    }

    /**
     * Generate a call to {@link Throwable#getCause()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getCause() {
        return bc.invokeVirtual(MD_Throwable.getCause, obj);
    }

    /**
     * Generate a call to {@link Throwable#getSuppressed()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getSuppressed() {
        return bc.invokeVirtual(MD_Throwable.getSuppressed, obj);
    }

    /**
     * Generate a call to {@link Throwable#addSuppressed(Throwable)}.
     *
     * @param exception the expression of the exception to add (must not be {@code null})
     */
    public void addSuppressed(Expr exception) {
        bc.invokeVirtual(MD_Throwable.addSuppressed, obj, exception);
    }
}
