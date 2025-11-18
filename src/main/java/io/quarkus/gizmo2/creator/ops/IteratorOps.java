package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

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
        super(bc, obj);
    }

    /**
     * Call {@link Iterator#hasNext()}.
     *
     * @return the boolean result of the method call (not {@code null})
     */
    public Expr hasNext() {
        return bc.invokeInterface(MD_Iterator.hasNext, obj);
    }

    /**
     * Call {@link Iterator#next()}.
     *
     * @return the next iterator item (not {@code null})
     */
    public Expr next() {
        return bc.invokeInterface(MD_Iterator.next, obj);
    }

    /**
     * Call {@link Iterator#remove}.
     */
    public void remove() {
        bc.invokeInterface(MD_Iterator.remove, obj);
    }

    /**
     * Call {@link Iterator#forEachRemaining(Consumer)}.
     *
     * @param action the consumer to pass to the function (not {@code null})
     */
    public void forEachRemaining(Expr action) {
        bc.invokeInterface(MD_Iterator.forEachRemaining, obj, action);
    }
}
