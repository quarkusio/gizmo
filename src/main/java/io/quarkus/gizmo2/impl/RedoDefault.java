package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

class RedoDefault extends Goto {
    private final SwitchCreator switch_;

    public RedoDefault(final SwitchCreator switch_) {
        this.switch_ = switch_;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new RedoDefaultKey(sci));
        } else {
            return sci.findDefault().startLabel();
        }
    }

    static class RedoDefaultKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;

        RedoDefaultKey(final SwitchCreatorImpl<?> switch_) {
            this.switch_ = switch_;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.redoDefault(switch_);
        }

        public boolean equals(final Object obj) {
            return obj instanceof RedoDefaultKey rk && equals(rk);
        }

        public boolean equals(final RedoDefaultKey other) {
            return this == other || other != null && switch_ == other.switch_;
        }

        public int hashCode() {
            return Objects.hash(RedoDefaultKey.class, switch_);
        }
    }
}
