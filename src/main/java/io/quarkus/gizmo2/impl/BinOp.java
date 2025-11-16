package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Conversions.convert;
import static io.quarkus.gizmo2.impl.Conversions.numericPromotion;
import static io.quarkus.gizmo2.impl.Conversions.unboxingConversion;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_int;

import java.lang.constant.ClassDesc;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class BinOp extends Item {
    private final Item a;
    private final Item b;
    private final Kind kind;

    BinOp(final Expr a, final Expr b, final Kind kind) {
        switch (kind.operands) {
            case SAME -> {
                Optional<ClassDesc> promotedType = numericPromotion(a.type(), b.type());
                if (promotedType.isPresent()) {
                    this.a = convert(a, promotedType.get());
                    this.b = convert(b, promotedType.get());
                } else {
                    throw new IllegalArgumentException("Operation " + kind
                            + " expects both operands to have the same type: " + a.type().displayName()
                            + ", " + b.type().displayName());
                }
            }
            case SECOND_INT -> {
                // the first operand is checked below by `isValidFor()`
                if (CD_int.equals(unboxingConversion(b.type()).orElse(b.type()))) {
                    this.a = convert(a, unboxingConversion(a.type()).orElse(a.type()));
                    this.b = convert(b, CD_int);
                } else {
                    throw new IllegalArgumentException("Operation " + kind
                            + " expects second operand to be int: " + b.type().displayName());
                }
            }
            default -> throw impossibleSwitchCase(kind.operands);
        }

        this.kind = kind;
        // it is important here that `type()` (to which `typeKind()` delegates) returns the type
        // of the _first_ operand, at least for operations that use `Operands.SECOND_INT` (see above)
        if (!kind.isValidFor(typeKind())) {
            throw new IllegalArgumentException("Operation " + kind + " is not valid for type kind of " + typeKind());
        }
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        b.process(itr, op);
        a.process(itr, op);
    }

    protected void computeType() {
        initType(a.type());
        if (a.hasGenericType()) {
            initGenericType(a.genericType());
        }
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        Operation op = kind.opFor(typeKind());
        // we validated op above
        assert op != null;
        op.apply(cb);
        smb.pop(); // left
        smb.pop(); // right
        smb.push(type()); // result
        smb.wroteCode();
    }

    public StringBuilder toShortString(final StringBuilder sb) {
        return b.toShortString(a.toShortString(sb).append(kind));
    }

    enum Operands {
        SAME,
        SECOND_INT,
    }

    interface Operation {
        void apply(CodeBuilder cb);
    }

    enum Kind {
        ADD(Operands.SAME, CodeBuilder::iadd, CodeBuilder::ladd, CodeBuilder::fadd, CodeBuilder::dadd, "+"),
        SUB(Operands.SAME, CodeBuilder::isub, CodeBuilder::lsub, CodeBuilder::fsub, CodeBuilder::dsub, "-"),
        MUL(Operands.SAME, CodeBuilder::imul, CodeBuilder::lmul, CodeBuilder::fmul, CodeBuilder::dmul, "*"),
        DIV(Operands.SAME, CodeBuilder::idiv, CodeBuilder::ldiv, CodeBuilder::fdiv, CodeBuilder::ddiv, "/"),
        REM(Operands.SAME, CodeBuilder::irem, CodeBuilder::lrem, CodeBuilder::frem, CodeBuilder::drem, "%"),

        AND(Operands.SAME, CodeBuilder::iand, CodeBuilder::land, "&"),
        OR(Operands.SAME, CodeBuilder::ior, CodeBuilder::lor, "|"),
        XOR(Operands.SAME, CodeBuilder::ixor, CodeBuilder::lxor, "^"),

        SHL(Operands.SECOND_INT, CodeBuilder::ishl, CodeBuilder::lshl, "<<"),
        SHR(Operands.SECOND_INT, CodeBuilder::ishr, CodeBuilder::lshr, ">>"),
        USHR(Operands.SECOND_INT, CodeBuilder::iushr, CodeBuilder::lushr, ">>>"),
        ;

        final Operands operands;
        final Operation intOp;
        final Operation longOp;
        final Operation floatOp;
        final Operation doubleOp;
        final String symbol;

        Kind(final Operands operands, final Operation intOp, final Operation longOp, final Operation floatOp,
                final Operation doubleOp, final String symbol) {
            this.operands = operands;
            this.intOp = intOp;
            this.longOp = longOp;
            this.floatOp = floatOp;
            this.doubleOp = doubleOp;
            this.symbol = symbol;
        }

        Kind(final Operands operands, final Operation intOp, final Operation longOp, final String symbol) {
            this(operands, intOp, longOp, null, null, symbol);
        }

        Operation opFor(TypeKind tk) {
            return switch (tk.asLoadable()) {
                case INT -> intOp;
                case LONG -> longOp;
                case FLOAT -> floatOp;
                case DOUBLE -> doubleOp;
                default -> null;
            };
        }

        boolean isValidFor(TypeKind tk) {
            return null != opFor(tk);
        }

        public String toString() {
            return symbol;
        }
    }
}
