package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class BinOp extends ExprImpl {
    private final ExprImpl a;
    private final ExprImpl b;
    private final Kind kind;

    BinOp(final Expr a, final Expr b, final Kind kind) {
        // todo: automatic conversions, unboxing
        requireSameType(a, b);
        this.a = (ExprImpl) a;
        this.b = (ExprImpl) b;
        this.kind = kind;
        if (! kind.isValidFor(typeKind())) {
            throw new IllegalArgumentException("Operation is not valid for type kind of " + typeKind());
        }
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        b.process(block, iter, verifyOnly);
        a.process(block, iter, verifyOnly);
    }

    public ClassDesc type() {
        return a.type();
    }

    public boolean bound() {
        return a.bound() || b.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (typeKind()) {
            case REFERENCE -> {
                // objects (strings)
                if (! b.type().equals(CD_String)) {
                    cb.invokestatic(CD_String, "valueOf", MethodTypeDesc.of(CD_String, CD_Object));
                }
                if (! a.type().equals(CD_String)) {
                    cb.invokestatic(CD_String, "valueOf", MethodTypeDesc.of(CD_String, CD_Object));
                }
            }
            default -> {
            }
        }
        Op op = kind.opFor(typeKind());
        // we validated op above
        assert op != null;
        op.apply(cb);
    }

    enum Kind {
        ADD(CodeBuilder::iadd, CodeBuilder::ladd, CodeBuilder::fadd, CodeBuilder::dadd, BinOp::aadd),
        SUB(CodeBuilder::isub, CodeBuilder::lsub, CodeBuilder::fsub, CodeBuilder::dsub),
        MUL(CodeBuilder::imul, CodeBuilder::lmul, CodeBuilder::fmul, CodeBuilder::dmul),
        DIV(CodeBuilder::idiv, CodeBuilder::ldiv, CodeBuilder::fdiv, CodeBuilder::ddiv),
        REM(CodeBuilder::irem, CodeBuilder::lrem, CodeBuilder::frem, CodeBuilder::drem),

        AND(CodeBuilder::iand, CodeBuilder::land),
        OR(CodeBuilder::ior, CodeBuilder::lor),
        XOR(CodeBuilder::ixor, CodeBuilder::lxor),

        SHL(CodeBuilder::ishl, CodeBuilder::lshl),
        SHR(CodeBuilder::ishr, CodeBuilder::lshr),
        USHR(CodeBuilder::iushr, CodeBuilder::lushr),
        ;

        final Op intOp;
        final Op longOp;
        final Op floatOp;
        final Op doubleOp;
        final Op refOp;

        Kind(final Op intOp, final Op longOp, final Op floatOp, final Op doubleOp, final Op refOp) {
            this.intOp = intOp;
            this.longOp = longOp;
            this.floatOp = floatOp;
            this.doubleOp = doubleOp;
            this.refOp = refOp;
        }

        Kind(final Op intOp, final Op longOp, final Op floatOp, final Op doubleOp) {
            this(intOp, longOp, floatOp, doubleOp, null);
        }

        Kind(final Op intOp, final Op longOp) {
            this(intOp, longOp, null, null);
        }

        Op opFor(TypeKind tk) {
            return switch (tk.asLoadable()) {
                case INT -> intOp;
                case LONG -> longOp;
                case FLOAT -> floatOp;
                case DOUBLE -> doubleOp;
                case REFERENCE -> refOp;
                default -> null;
            };
        }

        boolean isValidFor(TypeKind tk) {
            return null != opFor(tk);
        }
    }

    private static void aadd(CodeBuilder cb) {
        cb.invokevirtual(CD_String, "concat", MethodTypeDesc.of(CD_String, CD_String));
    }

    interface Op {
        void apply(CodeBuilder cb);
    }
}
