package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;

class RedoCase extends Goto {
    private final SwitchCreator switch_;
    private final Constant case_;

    public RedoCase(final SwitchCreator switch_, final Constant case_) {
        this.switch_ = switch_;
        this.case_ = case_;
    }

    Label target(final BlockCreatorImpl from) {
        TryFinally tryFinally = from.tryFinally;
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new RedoCaseKey(sci, case_));
        } else {
            return findBlock(sci, case_).startLabel();
        }
    }

    private static BlockCreatorImpl findBlock(final SwitchCreatorImpl<?> sci, final Constant case_) {
        SwitchCreatorImpl<?>.CaseCreatorImpl matched = sci.findCase(case_);
        if (matched == null) {
            return sci.default_;
        }
        return matched.body;
    }

    static class RedoCaseKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;
        private final Constant case_;

        RedoCaseKey(final SwitchCreatorImpl<?> switch_, final Constant case_) {
            this.switch_ = switch_;
            this.case_ = case_;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.redo(switch_, case_);
        }

        public boolean equals(final Object obj) {
            return obj instanceof RedoCaseKey rk && equals(rk);
        }

        public boolean equals(final RedoCaseKey other) {
            return this == other || other != null && switch_ == other.switch_ && case_.equals(other.case_);
        }

        public int hashCode() {
            return Objects.hash(RedoCaseKey.class, switch_, case_);
        }
    }
}
