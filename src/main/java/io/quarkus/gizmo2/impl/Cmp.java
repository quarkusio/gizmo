package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ListIterator;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;

final class Cmp extends Item {
    private static final ClassDesc CD_Comparable = ClassDesc.of("java.lang.Comparable");
    private final Item a;
    private final Item b;
    private final Kind kind;

    Cmp(final Expr a, final Expr b, final Kind kind) {
        this.a = (Item) a;
        this.b = (Item) b;
        this.kind = kind;
    }

    protected void processDependencies(final ListIterator<Item> iter, final Op op) {
        b.process(iter, op);
        a.process(iter, op);
    }

    public ClassDesc type() {
        return CD_int;
    }

    public boolean bound() {
        return a.bound() && b.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        switch (a.typeKind().asLoadable()) {
            case INT -> kind.intOp.apply(cb);
            case LONG -> kind.longOp.apply(cb);
            case FLOAT -> kind.floatOp.apply(cb);
            case DOUBLE -> kind.doubleOp.apply(cb);
            case REFERENCE -> kind.refOp.apply(cb);
            default -> throw new IllegalStateException();
        }
    }

    enum Kind {
        CMP(Cmp::icmp, CodeBuilder::lcmp, Cmp::fcmp, Cmp::dcmp, Cmp::acmp),
        CMPG(Cmp::icmp, CodeBuilder::lcmp, CodeBuilder::fcmpg, CodeBuilder::dcmpg, Cmp::acmp),
        CMPL(Cmp::icmp, CodeBuilder::lcmp, CodeBuilder::fcmpl, CodeBuilder::dcmpl, Cmp::acmp),
        ;

        final CmpOp intOp;
        final CmpOp longOp;
        final CmpOp floatOp;
        final CmpOp doubleOp;
        final CmpOp refOp;

        Kind(final CmpOp intOp, final CmpOp longOp, final CmpOp floatOp, final CmpOp doubleOp, final CmpOp refOp) {
            this.doubleOp = doubleOp;
            this.floatOp = floatOp;
            this.intOp = intOp;
            this.longOp = longOp;
            this.refOp = refOp;
        }
    }

    private static void acmp(CodeBuilder cb) {
        cb.invokeinterface(CD_Comparable, "compareTo", MethodTypeDesc.of(CD_int, CD_Object));
    }

    private static void icmp(CodeBuilder cb) {
        cb.invokestatic(CD_Integer, "compare", MethodTypeDesc.of(CD_int, CD_int, CD_int));
    }

    private static void dcmp(CodeBuilder cb) {
        cb.invokestatic(CD_Double, "compare", MethodTypeDesc.of(CD_int, CD_double, CD_double));
    }

    private static void fcmp(CodeBuilder cb) {
        cb.invokestatic(CD_Float, "compare", MethodTypeDesc.of(CD_int, CD_float, CD_float));
    }

    interface CmpOp {
        void apply(CodeBuilder cb);
    }
}
