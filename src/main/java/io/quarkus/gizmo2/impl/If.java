package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.desc.Descs.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;

abstract class If extends Item {
    final Kind kind;
    final BlockCreatorImpl whenTrue;
    final BlockCreatorImpl whenFalse;

    If(final ClassDesc type, final Kind kind, final BlockCreatorImpl whenTrue, final BlockCreatorImpl whenFalse) {
        super(type);
        this.kind = kind;
        this.whenTrue = whenTrue;
        this.whenFalse = whenFalse;
    }

    private static void comparable_acmp(CodeBuilder cb, Label label) {
        cb.invokeinterface(CD_Comparable, "compareTo", MethodTypeDesc.of(CD_int, CD_Object));
    }

    enum Kind {
        // IMPORTANT: preserve order!
        EQ(CodeBuilder::ifeq, CodeBuilder::if_icmpeq, CodeBuilder::ifnull, CodeBuilder::if_acmpeq),
        NE(CodeBuilder::ifne, CodeBuilder::if_icmpne, CodeBuilder::ifnonnull, CodeBuilder::if_acmpne),
        LT(CodeBuilder::iflt, CodeBuilder::if_icmplt, null, IfOp.of(If::comparable_acmp, CodeBuilder::iflt)),
        GE(CodeBuilder::ifge, CodeBuilder::if_icmpge, null, IfOp.of(If::comparable_acmp, CodeBuilder::ifge)),
        LE(CodeBuilder::ifle, CodeBuilder::if_icmple, null, IfOp.of(If::comparable_acmp, CodeBuilder::ifle)),
        GT(CodeBuilder::ifgt, CodeBuilder::if_icmpgt, null, IfOp.of(If::comparable_acmp, CodeBuilder::ifgt)),
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

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        StackMapBuilder.Saved saved;
        if (whenTrue != null) {
            ListIterator<Item> trueItr = whenTrue.iterator();
            if (whenFalse != null) {
                // if-else
                ListIterator<Item> falseItr = whenFalse.iterator();
                if (trueItr.previous() instanceof Goto goto_ && trueItr.previous() instanceof BlockHeader) {
                    // just steal the goto target
                    op(kind).accept(cb, goto_.target(block, smb));
                    smb.wroteCode();
                    saved = smb.save();
                    whenFalse.writeCode(cb, block, smb);
                } else if (falseItr.previous() instanceof Goto goto_ && falseItr.previous() instanceof BlockHeader) {
                    // just steal the goto target
                    op(kind.invert()).accept(cb, goto_.target(block, smb));
                    smb.wroteCode();
                    saved = smb.save();
                    whenTrue.writeCode(cb, block, smb);
                } else {
                    op(kind).accept(cb, whenTrue.startLabel());
                    smb.wroteCode();
                    saved = smb.save();
                    whenFalse.writeCode(cb, block, smb);
                    if (whenFalse.mayFallThrough()) {
                        whenTrue.breakTarget();
                        smb.restore(saved);
                        cb.goto_(whenTrue.endLabel());
                        smb.wroteCode();
                    } else {
                        smb.restore(saved);
                    }
                    whenTrue.branchTarget();
                    whenTrue.writeCode(cb, block, smb);
                }
            } else {
                // if
                if (trueItr.previous() instanceof Goto goto_ && trueItr.previous() instanceof BlockHeader) {
                    // just steal the goto target
                    op(kind).accept(cb, goto_.target(block, smb));
                    smb.wroteCode();
                    saved = smb.save();
                } else {
                    op(kind.invert()).accept(cb, whenTrue.endLabel());
                    smb.wroteCode();
                    saved = smb.save();
                    whenTrue.breakTarget();
                    whenTrue.writeCode(cb, block, smb);
                }
            }
        } else {
            if (whenFalse != null) {
                // if not
                ListIterator<Item> falseItr = whenFalse.iterator();
                if (falseItr.previous() instanceof Goto goto_ && falseItr.previous() instanceof BlockHeader) {
                    // just steal the goto target
                    op(kind.invert()).accept(cb, goto_.target(block, smb));
                    smb.wroteCode();
                    saved = smb.save();
                } else {
                    op(kind).accept(cb, whenFalse.endLabel());
                    smb.wroteCode();
                    saved = smb.save();
                    whenFalse.breakTarget();
                    whenFalse.writeCode(cb, block, smb);
                }
            } else {
                throw new IllegalStateException();
            }
        }
        smb.restore(saved);
        if (!Util.isVoid(type())) {
            smb.push(type());
        }
    }

    public boolean mayFallThrough() {
        return whenTrue == null || whenTrue.mayFallThrough() || whenFalse == null || whenFalse.mayFallThrough();
    }

    abstract IfOp op(Kind kind);

    interface IfOp {
        void accept(CodeBuilder cb, Label a);

        static IfOp of(IfOp op1, IfOp op2) {
            return (cb, a) -> {
                op1.accept(cb, a);
                op2.accept(cb, a);
            };
        }
    }
}
