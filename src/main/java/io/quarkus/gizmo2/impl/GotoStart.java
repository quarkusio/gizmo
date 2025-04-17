package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

final class GotoStart extends Goto {
    private final BlockCreatorImpl outer;

    GotoStart(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "GotoStart:" + outer;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        if (tryFinally != null) {
            return tryFinally.cleanup(new GotoStartKey(outer));
        } else {
            return outer.startLabel();
        }
    }

    static class GotoStartKey extends TryFinally.CleanupKey {
        private final BlockCreatorImpl outer;

        GotoStartKey(final BlockCreatorImpl outer) {
            this.outer = outer;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.goto_(outer);
        }

        public boolean equals(final Object obj) {
            return obj instanceof GotoStartKey rk && equals(rk);
        }

        public boolean equals(final GotoStartKey other) {
            return this == other || other != null && outer == other.outer;
        }

        public int hashCode() {
            return Objects.hash(GotoStartKey.class, outer);
        }
    }
}
