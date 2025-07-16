package io.quarkus.gizmo2.creator.ops;

import java.util.Optional;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Optional}.
 */
public class OptionalOps extends ObjectOps {

    /**
     * Construct a new instance.
     *
     * @param bc the block creator to wrap (must not be {@code null})
     * @param obj the optional object (must not be {@code null})
     */
    public OptionalOps(BlockCreator bc, Expr obj) {
        super(Optional.class, bc, obj);
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr get() {
        return invokeInstance(Object.class, "get");
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isPresent() {
        return invokeInstance(boolean.class, "isPresent");
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return invokeInstance(boolean.class, "isEmpty");
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @param other the expression to be returned, if no value is present
     * @return the expression of the result (not {@code null})
     */
    public Expr orElse(Expr other) {
        return invokeInstance(Object.class, "orElse", Object.class, other);
    }

}
