package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.requireSameLoadableTypeKind;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;

final class BinOp extends Item {
    private final Item a;
    private final Item b;
    private final Kind kind;

    BinOp(final Expr a, final Expr b, final Kind kind) {
        // todo: automatic conversions, unboxing
        requireSameLoadableTypeKind(a, b);
        this.a = (Item) a;
        this.b = (Item) b;
        this.kind = kind;
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
        Op op = kind.opFor(typeKind());
        // we validated op above
        assert op != null;
        op.apply(cb);
    }

    public StringBuilder toShortString(final StringBuilder sb) {
        return b.toShortString(a.toShortString(sb).append(kind));
    }

    enum Kind {
        ADD(CodeBuilder::iadd, CodeBuilder::ladd, CodeBuilder::fadd, CodeBuilder::dadd, "+"),
        SUB(CodeBuilder::isub, CodeBuilder::lsub, CodeBuilder::fsub, CodeBuilder::dsub, "-"),
        MUL(CodeBuilder::imul, CodeBuilder::lmul, CodeBuilder::fmul, CodeBuilder::dmul, "*"),
        DIV(CodeBuilder::idiv, CodeBuilder::ldiv, CodeBuilder::fdiv, CodeBuilder::ddiv, "/"),
        REM(CodeBuilder::irem, CodeBuilder::lrem, CodeBuilder::frem, CodeBuilder::drem, "%"),

        AND(CodeBuilder::iand, CodeBuilder::land, "&"),
        OR(CodeBuilder::ior, CodeBuilder::lor, "|"),
        XOR(CodeBuilder::ixor, CodeBuilder::lxor, "^"),

        SHL(CodeBuilder::ishl, CodeBuilder::lshl, "<<"),
        SHR(CodeBuilder::ishr, CodeBuilder::lshr, ">>"),
        USHR(CodeBuilder::iushr, CodeBuilder::lushr, ">>>"),
        ;

        final Op intOp;
        final Op longOp;
        final Op floatOp;
        final Op doubleOp;
        final String symbol;

        Kind(final Op intOp, final Op longOp, final Op floatOp, final Op doubleOp, final String symbol) {
            this.intOp = intOp;
            this.longOp = longOp;
            this.floatOp = floatOp;
            this.doubleOp = doubleOp;
            this.symbol = symbol;
        }

        Kind(final Op intOp, final Op longOp, final String symbol) {
            this(intOp, longOp, null, null, symbol);
        }

        Op opFor(TypeKind tk) {
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

    interface Op {
        void apply(CodeBuilder cb);
    }
}
