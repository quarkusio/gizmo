package io.quarkus.gizmo2.creator.ops;

import java.util.Iterator;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on an {@link Iterator}.
 */
public class IteratorOps extends ObjectOps {
    public IteratorOps(final BlockCreator bc, final Expr obj) {
        super(Iterator.class, bc, obj);
    }

    protected IteratorOps(final Class<?> receiverType, final BlockCreator bc, final Expr obj) {
        super(receiverType, bc, obj);
    }

    public Expr hasNext() {
        return invokeInstance(boolean.class, "hasNext");
    }

    public Expr next() {
        return invokeInstance(Object.class, "next");
    }

    public void remove() {
        invokeInstance(void.class, "remove");
    }

    public void forEachRemaining(Expr action) {
        invokeInstance(void.class, "forEachRemaining", Consumer.class, action);
    }
}
