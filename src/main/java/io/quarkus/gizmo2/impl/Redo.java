package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

final class Redo extends Goto {
    private final BlockCreatorImpl outer;

    Redo(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "Redo:" + outer;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        if (tryFinally != null) {
            return tryFinally.cleanup(new RedoKey(outer));
        } else {
            return outer.startLabel();
        }
    }

    static class RedoKey extends TryFinally.CleanupKey {
        private final BlockCreatorImpl outer;

        RedoKey(final BlockCreatorImpl outer) {
            this.outer = outer;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.redo(outer);
        }

        public boolean equals(final Object obj) {
            return obj instanceof RedoKey rk && equals(rk);
        }

        public boolean equals(final RedoKey other) {
            return this == other || other != null && outer == other.outer;
        }

        public int hashCode() {
            return Objects.hash(RedoKey.class, outer);
        }
    }
}
