package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.smallrye.classfile.Label;

final class GotoStart extends Goto {
    private final BlockCreatorImpl outer;

    GotoStart(final BlockCreator outer) {
        this.outer = (BlockCreatorImpl) outer;
    }

    /**
     * {@return the target block of this goto-start statement}
     */
    BlockCreatorImpl outer() {
        return outer;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        if (outer == sb.currentBlock()) {
            sourceLine = sb.line("goto start;");
        } else {
            sourceLine = sb.startLine();
            sb.body().append("goto ").append(sb.ensureLabel(outer)).append(".start;");
            sb.endLine();
        }
        sb.trackItem(this);
    }

    public String itemName() {
        return "GotoStart:" + outer;
    }

    Label target(final BlockCreatorImpl from, final StackMapBuilder smb) {
        TryFinally tryFinally = from.tryFinally();
        if (tryFinally != null) {
            return tryFinally.cleanup(new GotoStartKey(outer, smb.save()));
        } else {
            return outer.startLabel();
        }
    }

    static class GotoStartKey extends TryFinally.CleanupKey {
        private final BlockCreatorImpl outer;

        GotoStartKey(final BlockCreatorImpl outer, final StackMapBuilder.Saved saved) {
            super(saved);
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
