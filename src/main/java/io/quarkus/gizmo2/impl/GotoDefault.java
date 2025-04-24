package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

class GotoDefault extends Goto {
    private final SwitchCreator switch_;

    public GotoDefault(final SwitchCreator switch_) {
        this.switch_ = switch_;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new GotoDefaultKey(sci));
        } else {
            return sci.findDefault().startLabel();
        }
    }

    static class GotoDefaultKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;

        GotoDefaultKey(final SwitchCreatorImpl<?> switch_) {
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
