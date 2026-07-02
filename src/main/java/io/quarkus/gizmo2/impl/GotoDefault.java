package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.smallrye.classfile.Label;

class GotoDefault extends Goto {
    private final SwitchCreator switch_;

    public GotoDefault(final SwitchCreator switch_) {
        this.switch_ = switch_;
    }

    /**
     * {@return the target switch of this goto-default statement}
     */
    SwitchCreatorImpl<?> switch_() {
        return (SwitchCreatorImpl<?>) switch_;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SwitchCreatorImpl<?> sw = (SwitchCreatorImpl<?>) switch_;
        if (sw == sb.currentSwitch()) {
            sourceLine = sb.line("goto default;");
        } else {
            sourceLine = sb.startLine();
            sb.body().append("goto ").append(sb.ensureLabel(sw)).append(".default;");
            sb.endLine();
        }
        sb.trackItem(this);
    }

    Label target(final BlockCreatorImpl from, final StackMapBuilder smb) {
        TryFinally tryFinally = from.tryFinally();
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new GotoDefaultKey(sci, smb.save()));
        } else {
            return sci.findDefault().startLabel();
        }
    }

    static class GotoDefaultKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;

        GotoDefaultKey(final SwitchCreatorImpl<?> switch_, final StackMapBuilder.Saved saved) {
            super(saved);
            this.switch_ = switch_;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.gotoDefault(switch_);
        }

        public boolean equals(final Object obj) {
            return obj instanceof GotoDefaultKey rk && equals(rk);
        }

        public boolean equals(final GotoDefaultKey other) {
            return this == other || other != null && switch_ == other.switch_;
        }

        public int hashCode() {
            return Objects.hash(GotoDefaultKey.class, switch_);
        }
    }
}
