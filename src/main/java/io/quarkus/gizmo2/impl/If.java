package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

abstract class If extends Item {
    private final ClassDesc type;
    final Kind kind;
    final BlockCreatorImpl whenTrue;
    final BlockCreatorImpl whenFalse;

    If(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse) {
        this.type = type;
        this.kind = kind;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    public ClassDesc type() {
        return type;
    }

    enum Kind {
        // IMPORTANT: preserve order!
        EQ(CodeBuilder::ifeq, CodeBuilder::if_icmpeq, CodeBuilder::ifnull, CodeBuilder::if_acmpeq),
        NE(CodeBuilder::ifne, CodeBuilder::if_icmpne, CodeBuilder::ifnonnull, CodeBuilder::if_acmpne),
        LT(CodeBuilder::iflt, CodeBuilder::if_icmplt),
        GE(CodeBuilder::ifge, CodeBuilder::if_icmpge),
        LE(CodeBuilder::ifle, CodeBuilder::if_icmple),
        GT(CodeBuilder::ifgt, CodeBuilder::if_icmpgt),
        ;
        static final List<Kind> values = List.of(values());

        final IfOp if_;
        final IfOp if_icmp;
        final IfOp if_acmpnull;
        final IfOp if_acmp;

        Kind(final IfOp if_, final IfOp if_icmp, final IfOp if_acmpnull, final IfOp if_acmp) {
            this.if_ = if_;
            this.if_icmp = if_icmp;
            this.if_acmpnull = if_acmpnull;
            this.if_acmp = if_acmp;
        }

        Kind(final IfOp if_, final IfOp if_icmp) {
            this(if_, if_icmp, null, null);
        }


        Kind invert() {
            return values.get(ordinal() ^ 1);
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        if (whenTrue != null) {
            if (whenFalse != null) {
                // if-else
                op(kind).accept(cb, whenTrue.startLabel());
                whenFalse.writeCode(cb, block);
                if (whenFalse.mayFallThrough()) {
                    cb.goto_(whenTrue.endLabel());
                }
                whenTrue.writeCode(cb, block);
            } else {
                // if
                op(kind.invert()).accept(cb, whenTrue.endLabel());
                whenTrue.writeCode(cb, block);
            }
        } else {
            if (whenFalse != null) {
                // unless
                op(kind).accept(cb, whenFalse.endLabel());
                whenFalse.writeCode(cb, block);
            } else {
                throw new IllegalStateException();
            }
        }
    }

    public boolean mayThrow() {
        return whenTrue != null && whenTrue.mayThrow() || whenFalse != null && whenFalse.mayThrow();
    }

    public boolean mayFallThrough() {
        return whenTrue != null && whenTrue.mayFallThrough() || whenFalse != null && whenFalse.mayFallThrough();
    }

    public boolean mayReturn() {
        return whenTrue != null && whenTrue.mayReturn() || whenFalse != null && whenFalse.mayReturn();
    }

    abstract IfOp op(Kind kind);

    interface IfOp {
        void accept(CodeBuilder cb, Label a);
    }
}
