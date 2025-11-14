package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.desc.Descs.*;
import static io.quarkus.gizmo2.impl.Conversions.convert;
import static io.quarkus.gizmo2.impl.Conversions.numericPromotion;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
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
import java.util.Optional;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.attribute.StackMapFrameInfo;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class Cmp extends Item {

    private final Item a;
    private final Item b;
    private final Kind kind;

    Cmp(final Expr a, final Expr b, final Kind kind) {
        super(CD_int);
        Optional<ClassDesc> promotedType = numericPromotion(a.type(), b.type());
        if (promotedType.isPresent()) {
            this.a = convert(a, promotedType.get());
            this.b = convert(b, promotedType.get());
        } else {
            if (TypeKind.from(a.type()) != TypeKind.REFERENCE || TypeKind.from(b.type()) != TypeKind.REFERENCE) {
                throw new IllegalArgumentException("Comparison expects both operands to have the same type: "
                        + a.type().displayName() + ", " + b.type().displayName());
            }
            this.a = (Item) a;
            this.b = (Item) b;
        }
        this.kind = kind;
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        b.process(itr, op);
        a.process(itr, op);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        switch (a.typeKind().asLoadable()) {
            case INT -> kind.intOp.apply(cb);
            case LONG -> kind.longOp.apply(cb);
            case FLOAT -> kind.floatOp.apply(cb);
            case DOUBLE -> kind.doubleOp.apply(cb);
            case REFERENCE -> kind.refOp.apply(cb);
            default -> throw impossibleSwitchCase(a.typeKind().asLoadable());
        }
        smb.pop(); // a
        smb.pop(); // b
        smb.push(StackMapFrameInfo.SimpleVerificationTypeInfo.INTEGER);
        smb.wroteCode();
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
