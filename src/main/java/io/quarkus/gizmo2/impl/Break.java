package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

final class Break extends Goto {
    private final BlockCreatorImpl outer;

    public Break(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "Break:" + outer;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally();
        if (tryFinally != null) {
            return tryFinally.cleanup(new BreakKey(outer));
        } else {
            return outer.endLabel();
        }
    }

    static class BreakKey extends TryFinally.CleanupKey {
        private final BlockCreatorImpl outer;

        BreakKey(final BlockCreatorImpl outer) {
            this.outer = outer;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.break_(outer);
        }

        public boolean equals(final Object obj) {
            return obj instanceof BreakKey bk && equals(bk);
        }

        public boolean equals(final BreakKey other) {
            return this == other || other != null && outer == other.outer;
        }

        public int hashCode() {
            return Objects.hash(BreakKey.class, outer);
        }
    }
}
