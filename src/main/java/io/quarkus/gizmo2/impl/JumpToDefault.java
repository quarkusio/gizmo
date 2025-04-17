package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

class JumpToDefault extends Jump {
    private final SwitchCreator switch_;

    public JumpToDefault(final SwitchCreator switch_) {
        this.switch_ = switch_;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new JumpToDefaultKey(sci));
        } else {
            return sci.findDefault().startLabel();
        }
    }

    static class JumpToDefaultKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;

        JumpToDefaultKey(final SwitchCreatorImpl<?> switch_) {
            this.switch_ = switch_;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.jumpToDefault(switch_);
        }

        public boolean equals(final Object obj) {
            return obj instanceof JumpToDefaultKey rk && equals(rk);
        }

        public boolean equals(final JumpToDefaultKey other) {
            return this == other || other != null && switch_ == other.switch_;
        }

        public int hashCode() {
            return Objects.hash(JumpToDefaultKey.class, switch_);
        }
    }
}
