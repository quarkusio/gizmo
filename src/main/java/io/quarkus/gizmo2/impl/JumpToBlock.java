package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

final class JumpToBlock extends Jump {
    private final BlockCreatorImpl outer;

    JumpToBlock(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    public String itemName() {
        return "JumpToBlock:" + outer;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        if (tryFinally != null) {
            return tryFinally.cleanup(new JumpToBlockKey(outer));
        } else {
            return outer.startLabel();
        }
    }

    static class JumpToBlockKey extends TryFinally.CleanupKey {
        private final BlockCreatorImpl outer;

        JumpToBlockKey(final BlockCreatorImpl outer) {
            this.outer = outer;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.jumpToBlock(outer);
        }

        public boolean equals(final Object obj) {
            return obj instanceof JumpToBlockKey rk && equals(rk);
        }

        public boolean equals(final JumpToBlockKey other) {
            return this == other || other != null && outer == other.outer;
        }

        public int hashCode() {
            return Objects.hash(JumpToBlockKey.class, outer);
        }
    }
}
