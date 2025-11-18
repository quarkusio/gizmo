package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

import java.util.Optional;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Optional}.
 */
public final class OptionalOps extends ObjectOps {

    /**
     * Construct a new instance.
     *
     * @param bc the block creator to wrap (must not be {@code null})
     * @param obj the optional object (must not be {@code null})
     */
    public OptionalOps(BlockCreator bc, Expr obj) {
        super(bc, obj);
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr get() {
        return bc.invokeVirtual(MD_Optional.get, obj);
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isPresent() {
        return bc.invokeVirtual(MD_Optional.isPresent, obj);
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return bc.invokeVirtual(MD_Optional.isEmpty, obj);
    }

    /**
     * Generate a call to {@link Optional#get()}.
     *
     * @param other the expression to be returned, if no value is present
     * @return the expression of the result (not {@code null})
     */
    public Expr orElse(Expr other) {
        return bc.invokeVirtual(MD_Optional.orElse, obj, other);
    }

}
