package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;
import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link Object}.
 */
public class ObjectOps {
    /**
     * The block creator (not {@code null}).
     */
    protected final BlockCreator bc;
    /**
     * The receiver object (not {@code null}).
     */
    protected final Expr obj;

    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver object (must not be {@code null})
     */
    public ObjectOps(final BlockCreator bc, final Expr obj) {
        checkNotNullParam("bc", bc);
        checkNotNullParam("obj", obj);
        this.bc = bc;
        this.obj = obj;
    }

    /**
     * {@return the receiver object expression}
     */
    protected Expr receiver() {
        return obj;
    }

    /**
     * Generate a call to {@link Object#getClass()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr getClass_() {
        return bc.invokeVirtual(MD_Object.getClass, obj);
    }

    /**
     * Generate a call to {@link Object#toString()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objToString(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr toString_() {
        return bc.invokeVirtual(MD_Object.toString, obj);
    }

    /**
     * Generate a call to {@link Object#equals(Object)}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objEquals(Expr, Expr)}.
     *
     * @param otherObj the object to compare (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr equals_(Expr otherObj) {
        return bc.invokeVirtual(MD_Object.equals, obj, otherObj);
    }

    /**
     * Generate a call to {@link Object#hashCode()}.
     * For a {@code null}-safe variation, use {@link BlockCreator#objHashCode(Expr)}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr hashCode_() {
        return bc.invokeVirtual(MD_Object.hashCode, obj);
    }
}
