package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.requireSameLoadableTypeKind;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class BinOp extends Item {
    private final Item a;
    private final Item b;
    private final Kind kind;

    BinOp(final Expr a, final Expr b, final Kind kind) {
        // todo: automatic conversions, unboxing
        switch (kind.operands) {
            case SAME -> requireSameLoadableTypeKind(a, b);
            case SECOND_INT -> {
                // the first operand is checked later by `isValidFor()`
                if (b.typeKind().asLoadable() != TypeKind.INT) {
                    throw new IllegalArgumentException("Second operand must be int, but is " + b.type().displayName());
                }
            }
        }

        this.a = (Item) a;
        this.b = (Item) b;
        this.kind = kind;
        // it is important here that `type()` (to which `typeKind()` delegates) returns the type
        // of the _first_ operand, at least for operations that use `Operands.SECOND_INT` (see above)
        if (!kind.isValidFor(typeKind())) {
            throw new IllegalArgumentException("Operation is not valid for type kind of " + typeKind());
        }
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(b.process(node.prev(), op), op);
    }

    public ClassDesc type() {
        return a.type();
    }

    public boolean bound() {
        return a.bound() || b.bound() || kind == Kind.DIV || kind == Kind.REM;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Operation op = kind.opFor(typeKind());
        // we validated op above
        assert op != null;
        op.apply(cb);
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
