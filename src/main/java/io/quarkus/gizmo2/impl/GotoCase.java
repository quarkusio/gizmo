package io.quarkus.gizmo2.impl;

import java.util.Objects;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.smallrye.classfile.Label;

class GotoCase extends Goto {
    private final SwitchCreator switch_;
    private final Const case_;

    public GotoCase(final SwitchCreator switch_, final Const case_) {
        this.switch_ = switch_;
        this.case_ = case_;
    }

    /**
     * {@return the target switch of this goto-case statement}
     */
    SwitchCreatorImpl<?> switch_() {
        return (SwitchCreatorImpl<?>) switch_;
    }

    /**
     * {@return the target case constant}
     */
    Const case_() {
        return case_;
    }

    /** {@inheritDoc} */
    @Override
    protected void appendSourceStatement(SourceBuilder sb) {
        SwitchCreatorImpl<?> sw = (SwitchCreatorImpl<?>) switch_;
        sourceLine = sb.startLine();
        if (sw == sb.currentSwitch()) {
            sb.body().append("goto case ");
        } else {
            sb.body().append("goto ").append(sb.ensureLabel(sw)).append(".case ");
        }
        SourceGenerator.expr(sb.body(), (Item) case_, sb);
        sb.body().append(';');
        sb.endLine();
        sb.trackItem(this);
    }

    Label target(final BlockCreatorImpl from, final StackMapBuilder smb) {
        TryFinally tryFinally = from.tryFinally();
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (tryFinally != null) {
            return tryFinally.cleanup(new GotoCaseKey(sci, case_, smb.save()));
        } else {
            return findBlock(sci, case_).startLabel();
        }
    }

    private static BlockCreatorImpl findBlock(final SwitchCreatorImpl<?> sci, final Const case_) {
        SwitchCreatorImpl<?>.CaseCreatorImpl matched = sci.findCase(case_);
        if (matched == null) {
            return sci.default_;
        }
        return matched.body;
    }

    static class GotoCaseKey extends TryFinally.CleanupKey {
        private final SwitchCreatorImpl<?> switch_;
        private final Const case_;

        GotoCaseKey(final SwitchCreatorImpl<?> switch_, final Const case_, final StackMapBuilder.Saved saved) {
            super(saved);
            this.switch_ = switch_;
            this.case_ = case_;
        }

        void terminate(final BlockCreatorImpl bci, final Expr input) {
            bci.gotoCase(switch_, case_);
        }

        public boolean equals(final Object obj) {
            return obj instanceof GotoCaseKey rk && equals(rk);
        }

        public boolean equals(final GotoCaseKey other) {
            return this == other || other != null && switch_ == other.switch_ && case_.equals(other.case_);
        }

        public int hashCode() {
            return Objects.hash(GotoCaseKey.class, switch_, case_);
        }
    }
}
