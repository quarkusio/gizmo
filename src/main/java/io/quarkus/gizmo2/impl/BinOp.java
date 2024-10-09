package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.BiFunction;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;

final class BinOp extends Item {
    private final Item a;
    private final Item b;
    private final Kind kind;

    BinOp(final Expr a, final Expr b, final Kind kind) {
        // todo: automatic conversions, unboxing
        requireSameType(a, b);
        this.a = (Item) a;
        this.b = (Item) b;
        this.kind = kind;
        if (! kind.isValidFor(typeKind())) {
            throw new IllegalArgumentException("Operation is not valid for type kind of " + typeKind());
        }
    }

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return a.process(b.process(node.prev(), op), op);
    }

    public boolean mayThrow() {
        TypeKind loadableKind = typeKind().asLoadable();
        return (kind == Kind.DIV || kind == Kind.REM)
            && (loadableKind == TypeKind.INT || loadableKind == TypeKind.LONG)
            && ! (b instanceof ConstantImpl bc && bc.isNonZero());
    }

    public ClassDesc type() {
        return a.type();
    }

    public boolean bound() {
        return a.bound() || b.bound();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        Op op = kind.opFor(typeKind());
        // we validated op above
        assert op != null;
        op.apply(cb);
    }

    enum Kind {
        ADD(CodeBuilder::iadd, CodeBuilder::ladd, CodeBuilder::fadd, CodeBuilder::dadd),
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

        Kind(final Op intOp, final Op longOp, final Op floatOp, final Op doubleOp) {
            this.intOp = intOp;
            this.longOp = longOp;
            this.floatOp = floatOp;
            this.doubleOp = doubleOp;
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
                default -> null;
            };
        }

        boolean isValidFor(TypeKind tk) {
            return null != opFor(tk);
        }
    }

    interface Op {
        void apply(CodeBuilder cb);
    }
}
