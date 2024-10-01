package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Short;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_Throwable;
import static java.lang.constant.ConstantDescs.CD_Void;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;
import static java.util.Collections.nCopies;

import java.io.PrintStream;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.Opcode;
import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.LValueExpr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.creator.SwitchExprCreator;
import io.quarkus.gizmo2.creator.TryCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.IntConstant;
import io.quarkus.gizmo2.impl.constant.NullConstant;

/**
 * The block builder implementation. Internal only.
 */
sealed public class BlockCreatorImpl extends Item implements BlockCreator, Scoped<BlockCreatorImpl> permits SwitchCreatorImpl.Case {
    private static final int ST_ACTIVE = 0;
    private static final int ST_NESTED = 1;
    private static final int ST_EXIT_BLOCK = 2;
    private static final int ST_EXIT_ALL = 3;

    private final TypeCreatorImpl owner;
    /**
     * The outermost code builder.
     * This should only be used for creating new labels and other context-independent things.
     */
    private final CodeBuilder outerCodeBuilder;
    private final BlockCreatorImpl parent;
    private final int depth;
    /**
     * All the items to emit, in order.
     */
    private final List<Item> items = new ArrayList<>();
    private Cleanup blockCleanup = null;
    private int state;
    private boolean fallsOut;
    private final Label startLabel;
    private final Label endLabel;
    private final ExprImpl input;
    private final ClassDesc outputType;
    private final BlockHeaderExpr header;

    BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder) {
        this(owner, outerCodeBuilder, null, CD_void, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent) {
        this(parent, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc headerType) {
        this(parent.owner, parent.outerCodeBuilder, parent, headerType, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ExprImpl input, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, input.type(), input, outputType);
    }

    private BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final BlockCreatorImpl parent, final ClassDesc headerType, final ExprImpl input, final ClassDesc outputType) {
        this.outerCodeBuilder = outerCodeBuilder;
        this.parent = parent;
        this.owner = owner;
        depth = parent == null ? 0 : parent.depth + 1;
        startLabel = newLabel();
        endLabel = newLabel();
        this.input = input;
        add_(header = new BlockHeaderExpr(this, headerType));
        this.outputType = outputType;
    }

    Label newLabel() {
        return outerCodeBuilder.newLabel();
    }

    public ClassDesc type() {
        return outputType;
    }

    public boolean active() {
        return state == ST_ACTIVE;
    }

    public boolean done() {
        return state > ST_NESTED;
    }

    public boolean exitsAll() {
        return state == ST_EXIT_ALL;
    }

    public boolean exitsBlock() {
        return state == ST_EXIT_BLOCK;
    }

    boolean fallsOut() {
        return fallsOut;
    }

    private void markExited() {
        state = ST_EXIT_BLOCK;
    }

    private void markExitedAll() {
        state = ST_EXIT_ALL;
    }

    public boolean isContainedBy(final BlockCreator other) {
        return this == other || parent != null && parent.isContainedBy(other);
    }

    public LocalVar declare(final String name, final ClassDesc type) {
        LocalVarImpl lv = new LocalVarImpl(this, name, type);
        add_(lv.allocator());
        return lv;
    }

    public Expr get(final LValueExpr var, final AccessMode mode) {
        return add_(((LValueExprImpl) var).emitGet(this, mode));
    }

    public LValueExpr set(final LValueExpr var, final Expr value, final AccessMode mode) {
        add_(((LValueExprImpl) var).emitSet(this, (ExprImpl) value, mode));
        return var;
    }

    public LValueExpr andAssign(final LValueExpr var, final Expr arg) {
        return set(var, and(var, arg));
    }

    public LValueExpr orAssign(final LValueExpr var, final Expr arg) {
        return set(var, or(var, arg));
    }

    public LValueExpr xorAssign(final LValueExpr var, final Expr arg) {
        return set(var, xor(var, arg));
    }

    public LValueExpr shlAssign(final LValueExpr var, final Expr arg) {
        return set(var, shl(var, arg));
    }

    public LValueExpr shrAssign(final LValueExpr var, final Expr arg) {
        return set(var, shr(var, arg));
    }

    public LValueExpr ushrAssign(final LValueExpr var, final Expr arg) {
        return set(var, ushr(var, arg));
    }

    public LValueExpr addAssign(final LValueExpr var, final Expr arg) {
        if (arg instanceof Constant c) {
            inc(var, c);
            return var;
        } else {
            return set(var, add(var, arg));
        }
    }

    public LValueExpr subAssign(final LValueExpr var, final Expr arg) {
        if (arg instanceof Constant c) {
            dec(var, c);
            return var;
        } else {
            return set(var, sub(var, arg));
        }
    }

    public LValueExpr mulAssign(final LValueExpr var, final Expr arg) {
        return set(var, mul(var, arg));
    }

    public LValueExpr divAssign(final LValueExpr var, final Expr arg) {
        return set(var, div(var, arg));
    }

    public LValueExpr remAssign(final LValueExpr var, final Expr arg) {
        return set(var, rem(var, arg));
    }

    private ClassDesc boxType(TypeKind typeKind) {
        return switch (typeKind) {
            case BOOLEAN -> CD_Boolean;
            case BYTE -> CD_Byte;
            case CHAR -> CD_Character;
            case SHORT -> CD_Short;
            case INT -> CD_Integer;
            case LONG -> CD_Long;
            case FLOAT -> CD_Float;
            case DOUBLE -> CD_Double;
            case VOID -> CD_Void;
            default -> throw new IllegalArgumentException("No box type for " + typeKind);
        };
    }

    private static final Map<ClassDesc, ClassDesc> unboxTypes = Map.of(
        CD_Boolean, CD_boolean,
        CD_Byte, CD_byte,
        CD_Character, CD_char,
        CD_Short, CD_short,
        CD_Integer, CD_int,
        CD_Long, CD_long,
        CD_Float, CD_float,
        CD_Double, CD_double,
        CD_Void, CD_void
    );

    public Expr box(final Expr a) {
        if (unboxTypes.containsKey(a.type())) {
            return a;
        }
        TypeKind typeKind = a.typeKind();
        ClassDesc boxType = boxType(typeKind);
        return invokeStatic(ClassMethodDesc.of(boxType, "valueOf", MethodTypeDesc.of(boxType, typeKind.upperBound())), a);
    }

    public Expr unbox(final Expr a) {
        if (a.typeKind().getDeclaringClass().isPrimitive()) {
            return a;
        }
        ClassDesc boxType = a.type();
        ClassDesc unboxType = unboxTypes.get(boxType);
        if (unboxType == null) {
            throw new IllegalArgumentException("No unbox type for " + boxType);
        }
        return invokeVirtual(ClassMethodDesc.of(boxType, switch (TypeKind.from(boxType)) {
            case BOOLEAN -> "booleanValue";
            case BYTE -> "byteValue";
            case CHAR -> "charValue";
            case SHORT -> "shortValue";
            case INT -> "intValue";
            case LONG -> "longValue";
            case FLOAT -> "floatValue";
            case DOUBLE -> "doubleValue";
            default -> throw new IllegalStateException();
        }, MethodTypeDesc.of(unboxType)), a);
    }

    public void switch_(final Expr val, final Consumer<SwitchCreator> builder) {
        add_(switch (val.typeKind().asLoadable()) {
            case INT -> new IntSwitch(this, val);
            case REFERENCE -> {
                if (val.type().equals(CD_String)) {
                    // yield new HashingSwitch(this, val);
                    throw new UnsupportedOperationException();
                } else {
                    throw new UnsupportedOperationException("Switch type " + val.type() + " not supported");
                }
            }
            default -> throw new UnsupportedOperationException("Switch type " + val.type() + " not supported");
        }).accept(builder);
    }

    public void redo(final SwitchCreator switch_, final Constant case_) {
        add_(new Item() {
            protected void insert(final BlockCreatorImpl block, final ListIterator<Item> iter) {
                super.insert(block, iter);
                block.cleanStack(iter);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                SwitchCreatorImpl<?> cast = (SwitchCreatorImpl<?>) switch_;
                BlockCreatorImpl matched = cast.findCase(case_);
                if (matched == null) {
                    matched = cast.findDefault();
                }
                exitTo(cb, matched.parent);
                cb.goto_(matched.startLabel());
            }
        });
    }

    public void redoDefault(final SwitchCreator switch_) {
        add_(new Item() {
            protected void insert(final BlockCreatorImpl block, final ListIterator<Item> iter) {
                super.insert(block, iter);
                block.cleanStack(iter);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                SwitchCreatorImpl<?> cast = (SwitchCreatorImpl<?>) switch_;
                BlockCreatorImpl default_ = cast.findDefault();
                exitTo(cb, default_.parent);
                cb.goto_(default_.startLabel());
            }
        });
    }

    public Expr iterate(final Expr items) {
        return invokeInterface(MethodDesc.of(Iterable.class, "iterator", Iterator.class), items);
    }

    public Expr currentThread() {
        return invokeStatic(MethodDesc.of(Thread.class, "currentThread", void.class));
    }

    public Expr iterHasNext(final Expr iterator) {
        return invokeInterface(MethodDesc.of(Iterator.class, "hasNext", boolean.class), iterator);
    }

    public Expr iterNext(final Expr iterator) {
        return invokeInterface(MethodDesc.of(Iterator.class, "next", Object.class), iterator);
    }

    public void close(final Expr closeable) {
        invokeInterface(MethodDesc.of(AutoCloseable.class, "close", void.class), closeable);
    }

    public void addSuppressed(final Expr throwable, final Expr suppressed) {
        invokeVirtual(MethodDesc.of(Throwable.class, "addSuppressed", void.class, Throwable.class), throwable, suppressed);
    }

    public Expr postInc(final LValueExpr var) {
        ExprImpl res = (ExprImpl) get(var);
        inc(var);
        return res;
    }

    public Expr preInc(final LValueExpr var) {
        inc(var);
        return get(var);
    }

    public void inc(final LValueExpr var, Constant amount) {
        ((LValueExprImpl) var).emitInc(this, amount);
    }

    public Expr postDec(final LValueExpr var) {
        ExprImpl res = (ExprImpl) get(var);
        dec(var);
        return res;
    }

    public Expr preDec(final LValueExpr var) {
        dec(var);
        return get(var);
    }

    public void dec(final LValueExpr var, Constant amount) {
        ((LValueExprImpl) var).emitDec(this, amount);
    }

    public Expr newArray(final ClassDesc elemType, final Expr size) {
        return add_(new NewEmptyArray(elemType, (ExprImpl) size));
    }

    public Expr newArray(final ClassDesc elementType, final List<Expr> values) {
        if (values.isEmpty()) {
            return newArray(elementType, ConstantImpl.of(0));
        }
        return add_(new NewArray(elementType, values));
    }

    private Expr relZero(final Expr a, final If.Kind kind) {
        if (a instanceof IntConstant ac) {
            return Constant.of(ac.intValue() == 0);
        } else if (a instanceof NullConstant) {
            return Constant.of(true);
        }
        switch (a.typeKind().asLoadable()) {
            case INT, REFERENCE -> {
                // normal relZero
                return new RelZero(a, kind);
            }
            case LONG -> {
                // wrap with cmp
                return relZero(cmp(a, Constant.of(0, a.typeKind())), kind);
            }
            case FLOAT, DOUBLE -> {
                // wrap with cmpg
                return relZero(cmpg(a, Constant.of(0, a.typeKind())), kind);
            }
            default -> throw new IllegalStateException();
        }
    }

    private Expr rel(final Expr a, final Expr b, final If.Kind kind) {
        switch (a.typeKind().asLoadable()) {
            case INT -> {
                // normal rel
                if (a instanceof IntConstant ac && ac.intValue() == 0) {
                    return relZero(b, kind.invert());
                } else if (b instanceof IntConstant bc && bc.intValue() == 0) {
                    return relZero(a, kind);
                } else {
                    return new Rel(a, b, kind);
                }
            }
            case LONG -> {
                // wrap with cmp
                return relZero(cmp(a, b), kind);
            }
            case FLOAT, DOUBLE -> {
                // wrap with cmpg
                return relZero(cmpg(a, b), kind);
            }
            case REFERENCE -> {
                if (a instanceof NullConstant) {
                    return relZero(b, kind);
                } else if (b instanceof NullConstant) {
                    return relZero(a, kind);
                } else {
                    return new Rel(a, b, kind);
                }
            }
            default -> throw new IllegalStateException();
        }
    }

    public Expr eq(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.EQ);
    }

    public Expr ne(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.NE);
    }

    public Expr lt(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.LT);
    }

    public Expr gt(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.GT);
    }

    public Expr le(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.LE);
    }

    public Expr ge(final Expr a, final Expr b) {
        return rel(a, b, If.Kind.GE);
    }

    public Expr cmp(final Expr a, final Expr b) {
        return add_(new Cmp(a, b, Cmp.Kind.CMP));
    }

    public Expr cmpl(final Expr a, final Expr b) {
        return add_(new Cmp(a, b, Cmp.Kind.CMPL));
    }

    public Expr cmpg(final Expr a, final Expr b) {
        return add_(new Cmp(a, b, Cmp.Kind.CMPG));
    }

    public Expr and(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.AND));
    }

    public Expr or(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.OR));
    }

    public Expr xor(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.XOR));
    }

    public Expr complement(final Expr a) {
        return xor(a, Constant.of(-1, a.typeKind()));
    }

    public Expr shl(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.SHL));
    }

    public Expr shr(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.SHR));
    }

    public Expr ushr(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.USHR));
    }

    public Expr add(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.ADD));
    }

    public Expr sub(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.SUB));
    }

    public Expr mul(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.MUL));
    }

    public Expr div(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.DIV));
    }

    public Expr rem(final Expr a, final Expr b) {
        return add_(new BinOp(a, b, BinOp.Kind.REM));
    }

    public Expr neg(final Expr a) {
        return add_(new Neg(a));
    }

    public Expr switchExpr(final Expr val, final Consumer<SwitchExprCreator> builder) {
        throw new UnsupportedOperationException();
    }

    public Expr lambda(final ClassDesc type, final Consumer<LambdaCreator> builder) {
        throw new UnsupportedOperationException();
    }

    public Expr cast(final Expr a, final ClassDesc toType) {
        if (a.type().isPrimitive()) {
            if (toType.isPrimitive()) {
                return add_(new PrimitiveCast(a, toType));
            } else if (toType.equals(boxType(a.typeKind()))) {
                return box(a);
            } else {
                throw new IllegalArgumentException("Cannot cast primitive value to object type");
            }
        } else {
            if (toType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot cast object value to primitive type");
            } else if (unboxTypes.containsKey(a.type()) && toType.equals(unboxTypes.get(a.type()))) {
                return unbox(a);
            } else {
                return add_(new CheckCast(a, toType));
            }
        }
    }

    public Expr instanceOf(final Expr obj, final ClassDesc type) {
        return add_(new InstanceOf(obj, type));
    }

    public Expr new_(final ConstructorDesc ctor, final List<Expr> args) {
        return add_(new Invoke(ctor, new New(ctor.owner()), true, args));
    }

    public Expr invokeStatic(final MethodDesc method, final List<Expr> args) {
        return add_(new Invoke(Opcode.INVOKESTATIC, method, null, args));
    }

    public Expr invokeVirtual(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return add_(new Invoke(Opcode.INVOKEVIRTUAL, method, instance, args));
    }

    public Expr invokeSpecial(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return add_(new Invoke(Opcode.INVOKESPECIAL, method, instance, args));
    }

    public Expr invokeSpecial(final ConstructorDesc ctor, final Expr instance, final List<Expr> args) {
        return add_(new Invoke(ctor, instance, false, args));
    }

    public Expr invokeInterface(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return add_(new Invoke(Opcode.INVOKEINTERFACE, method, instance, args));
    }

    public void forEach(final Expr fn, final BiConsumer<BlockCreator, Expr> builder) {
        block(fn, (b0, fn0) -> {
            LocalVar items = b0.define("$$items" + depth, fn0);
            if (items.type().isArray()) {
                // iterate array
                Expr lv = items.length();
                Expr length = lv instanceof Constant ? lv : b0.define("$$length" + depth, lv);
                LocalVar idx = b0.define("$$idx" + depth, Constant.of(0));
                b0.block(b1 -> {
                    b1.if_(b1.lt(idx, length), b2 -> {
                        LocalVar val = b2.define("$$val" + depth, items.elem(idx));
                        builder.accept(b2, val);
                        b2.inc(idx);
                        b2.redo();
                    });
                });
            } else {
                // use iterable
                LocalVar itr = b0.define("$$itr" + depth, b0.iterate(items));
                b0.block(b1 -> {
                    b1.if_(b1.iterHasNext(itr), b2 -> {
                        LocalVar val = b2.define("$$val" + depth, b2.iterNext(itr));
                        builder.accept(b2, val);
                        b2.redo(b1);
                    });
                });
            }
        });
    }

    public void block(final Expr arg, BiConsumer<BlockCreator, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, (ExprImpl) arg, CD_void);
        add_(block);
        block.accept(nested);
        return;
    }

    public void block(final Consumer<BlockCreator> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this);
        add_(block);
        block.accept(nested);
        return;
    }

    public Expr blockExpr(final ClassDesc type, final Function<BlockCreator, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, ConstantImpl.ofVoid(), type);
        add_(block);
        return block.accept(nested);
    }

    public Expr blockExpr(final Expr arg, final ClassDesc type, final BiFunction<BlockCreator, Expr, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, (ExprImpl) arg, type);
        add_(block);
        return block.accept(nested);
    }

    public void accept(final BiConsumer<? super BlockCreatorImpl, ? super ExprImpl> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (! type().equals(CD_void)) {
            throw new IllegalStateException("Void accept on block which returns " + type());
        }
        handler.accept(this, header);
        if (state == ST_ACTIVE) {
            cleanStack(items.listIterator(items.size()));
            fallsOut = true;
        }
    }

    public void accept(final Consumer<? super BlockCreatorImpl> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (! type().equals(CD_void)) {
            throw new IllegalStateException("Void accept on block which returns " + type());
        }
        handler.accept(this);
        if (state == ST_ACTIVE) {
            cleanStack(items.listIterator(items.size()));
            fallsOut = true;
        }
    }

    public Expr accept(final Function<? super BlockCreatorImpl, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (type().equals(CD_void)) {
            throw new IllegalStateException("Function accept on void-typed block");
        }
        ExprImpl res = (ExprImpl) handler.apply(this);
        if (state == ST_ACTIVE) {
            // expect the apply result
            ListIterator<Item> iter = items.listIterator(items.size());
            res.process(this, iter, true);
            // and clean the rest
            cleanStack(iter);
            fallsOut = true;
        }
        return res;
    }

    public Expr accept(final BiFunction<? super BlockCreatorImpl, Expr, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (type().equals(CD_void)) {
            throw new IllegalStateException("Function accept on void-typed block");
        }
        ExprImpl res = (ExprImpl) handler.apply(this, header);
        if (state == ST_ACTIVE) {
            // expect the apply result
            ListIterator<Item> iter = items.listIterator(items.size());
            res.process(this, iter, true);
            // and clean the rest
            cleanStack(iter);
            fallsOut = true;
        }
        return res;
    }

    public void ifInstanceOf(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, Expr> ifTrue) {
        if_(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.cast(obj, type)));
    }

    public void ifInstanceOfElse(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, Expr> ifTrue, final Consumer<BlockCreator> ifFalse) {
        ifElse(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.cast(obj, type)), ifFalse);
    }

    private If doIfInsn(final ClassDesc type, final Expr cond, final BlockCreatorImpl wt, final BlockCreatorImpl wf) {
        // try to combine the condition into the `if`
        if (cond.bound()) {
            if (cond instanceof Rel rel && lastWas(rel)) {
                return replace_(rel, new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right()));
            }
            if (cond instanceof RelZero rz && lastWas(rz)) {
                return replace_(rz, new IfZero(type, rz.kind(), wt, wf, rz.input()));
            }
            // failed
        } else {
            if (cond instanceof Rel rel) {
                return add_(new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right()));
            } else if (cond instanceof RelZero rz) {
                return add_(new IfZero(type, rz.kind(), wt, wf, rz.input()));
            }
            // failed
        }
        return add_(new IfZero(type, If.Kind.NE, wt, null, (ExprImpl) cond));
    }

    private void doIf(final Expr cond, final Consumer<BlockCreator> whenTrue, final Consumer<BlockCreator> whenFalse) {
        BlockCreatorImpl wt = whenTrue == null ? null : new BlockCreatorImpl(this);
        BlockCreatorImpl wf = whenFalse == null ? null : new BlockCreatorImpl(this);
        if (wt != null) {
            wt.accept(whenTrue);
        }
        if (wf != null) {
            wf.accept(whenFalse);
        }
        doIfInsn(CD_void, cond, wt, wf);
    }

    public Expr selectExpr(final ClassDesc type, final Expr cond, final Function<BlockCreator, Expr> whenTrue, final Function<BlockCreator, Expr> whenFalse) {
        BlockCreatorImpl wt = new BlockCreatorImpl(this, type);
        BlockCreatorImpl wf = new BlockCreatorImpl(this, type);
        wt.accept(whenTrue);
        wf.accept(whenFalse);
        return doIfInsn(type, cond, wt, wf);
    }

    public void if_(final Expr cond, final Consumer<BlockCreator> whenTrue) {
        doIf(cond, whenTrue, null);
    }

    public void unless(final Expr cond, final Consumer<BlockCreator> whenFalse) {
        doIf(cond, null, whenFalse);
    }

    public void ifElse(final Expr cond, final Consumer<BlockCreator> whenTrue, final Consumer<BlockCreator> whenFalse) {
        doIf(cond, whenTrue, whenFalse);
    }

    public void break_(final BlockCreator outer) {
        ((BlockCreatorImpl) outer).fallsOut = true;
        if (outer != this) {
            add_(new Item() {
                protected void insert(final BlockCreatorImpl block, final ListIterator<Item> iter) {
                    super.insert(block, iter);
                    block.cleanStack(iter);
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    block.exitTo(cb, (BlockCreatorImpl) outer);
                    cb.goto_(((BlockCreatorImpl) outer).endLabel());
                }

                public boolean exitsBlock() {
                    return true;
                }
            });
        }
        markExited();
    }

    public void redo(final BlockCreator outer) {
        if (! outer.contains(this)) {
            throw new IllegalStateException("Invalid block nesting");
        }
        add_(new Redo(outer));
        markExited();
    }

    public void loop(final Consumer<BlockCreator> body) {
        block(b0 -> {
            body.accept(b0);
            b0.redo();
        });
    }

    public void while_(final Function<BlockCreator, Expr> cond, final Consumer<BlockCreator> body) {
        block(b0 -> if_(b0.blockExpr(CD_boolean, cond), b1 -> {
            body.accept(b1);
            b1.redo(b0);
        }));
    }

    public void doWhile(final Consumer<BlockCreator> body, final Function<BlockCreator, Expr> cond) {
        block(b0 -> {
            body.accept(b0);
            if_(cond.apply(b0), b1 -> b1.redo(b0));
        });
    }

    public void try_(final Consumer<TryCreator> body) {
        add_(new TryImpl()).accept(body);
    }

    public void autoClose(final Expr resource, final BiConsumer<BlockCreator, Expr> body) {
        block(resource, (b0, opened) -> {
            LocalVar rsrc = b0.define("$$resource" + depth, opened);
            b0.try_(t1 -> {
                t1.body(b2 -> body.accept(b2, rsrc));
                t1.catch_(CD_Throwable, (b2, e2) -> {
                    b2.try_(t3 -> {
                        t3.body(b4 -> {
                            b4.close(rsrc);
                            b4.throw_(e2);
                        });
                        t3.catch_(CD_Throwable, (b4, e4) -> {
                            b4.addSuppressed(e2, e4);
                            b4.throw_(e2);
                        });
                    });
                });
            });
            b0.close(rsrc);
        });
    }

    void monitorEnter(final ExprImpl monitor) {
        add_(new Item() {
            protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
                monitor.process(block, iter, verifyOnly);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.monitorenter();
            }
        });
    }

    void monitorExit(final ExprImpl monitor) {
        add_(new Item() {
            protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
                monitor.process(block, iter, verifyOnly);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.monitorexit();
            }
        });
    }

    public void synchronized_(final Expr monitor, final Consumer<BlockCreator> body) {
        block(monitor, (b0, mon) -> {
            LocalVar mv = define("$$monitor" + depth, mon);
            monitorEnter((ExprImpl) mv);
            try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> ((BlockCreatorImpl)b2).monitorExit((ExprImpl) mv));
            });
        });
    }

    public void locked(final Expr jucLock, final Consumer<BlockCreator> body) {
        block(jucLock, (b0, lock) -> {
            LocalVar lv = define("$$lock" + depth, lock);
            invokeInterface(MethodDesc.of(Lock.class, "lock", void.class), lv);
            try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> b2.invokeInterface(MethodDesc.of(Lock.class, "unlock", void.class), lv));
            });
        });
    }

    public void return_() {
        add_(new Return());
    }

    public void return_(final Expr val) {
        add_(new Return(val));
    }

    public void throw_(final Expr val) {
        add_(new Throw(val));
    }

    public Expr objHashCode(final Expr obj) {
        return switch (obj.typeKind()) {
            case BOOLEAN -> invokeStatic(MethodDesc.of(Boolean.class, "hashCode", int.class, boolean.class), obj);
            case BYTE -> invokeStatic(MethodDesc.of(Byte.class, "hashCode", int.class, byte.class), obj);
            case SHORT -> invokeStatic(MethodDesc.of(Short.class, "hashCode", int.class, short.class), obj);
            case CHAR -> invokeStatic(MethodDesc.of(Character.class, "hashCode", int.class, char.class), obj);
            case INT -> invokeStatic(MethodDesc.of(Integer.class, "hashCode", int.class, int.class), obj);
            case LONG -> invokeStatic(MethodDesc.of(Long.class, "hashCode", int.class, long.class), obj);
            case FLOAT -> invokeStatic(MethodDesc.of(Float.class, "hashCode", int.class, float.class), obj);
            case DOUBLE -> invokeStatic(MethodDesc.of(Double.class, "hashCode", int.class, double.class), obj);
            case REFERENCE -> invokeVirtual(MethodDesc.of(Object.class, "hashCode", int.class), obj);
            case VOID -> Constant.of(0); // null constant
        };
    }

    public Expr objEquals(final Expr a, final Expr b) {
        return invokeStatic(MethodDesc.of(Objects.class, "equals", boolean.class, Object.class, Object.class), a, b);
    }

    public Expr objToString(final Expr obj) {
        return invokeStatic(MethodDesc.of(String.class, "valueOf", String.class, switch (obj.typeKind()) {
            case BOOLEAN -> boolean.class;
            case BYTE, SHORT, INT -> int.class;
            case CHAR -> char.class;
            case LONG -> long.class;
            case FLOAT -> float.class;
            case DOUBLE -> double.class;
            case REFERENCE -> obj.type().isArray() ? switch (TypeKind.from(obj.type().componentType())) {
                case CHAR -> char[].class;
                default -> Object.class;
            } : Object.class;
            default -> throw new IllegalArgumentException("Invalid type for `toString`: " + obj);
        }), obj);
    }

    public Expr arrayEquals(final Expr a, final Expr b) {
        ClassDesc type = switch (TypeKind.from(a.type().componentType())) {
            case REFERENCE -> CD_Object.arrayType();
            default -> a.type();
        };
        return invokeStatic(MethodDesc.of(Arrays.class, "equals", MethodTypeDesc.of(CD_boolean, type, type)), a, b);
    }

    public Expr loadClass(final Expr className) {
        return invokeStatic(MethodDesc.of(Class.class, "forName", Class.class, String.class), className);
    }

    public Expr listOf(final List<Expr> items) {
        int size = items.size();
        if (size <= 10) {
            return invokeStatic(MethodDesc.of(List.class, "of", List.class, nCopies(size, Object.class)), items);
        } else {
            return invokeStatic(MethodDesc.of(List.class, "of", List.class, Object[].class), newArray(Object.class, items));
        }
    }

    public Expr setOf(final List<Expr> items) {
        int size = items.size();
        if (size <= 10) {
            return invokeStatic(MethodDesc.of(Set.class, "of", Set.class, nCopies(size, Object.class)), items);
        } else {
            return invokeStatic(MethodDesc.of(Set.class, "of", Set.class, Object[].class), newArray(Object.class, items));
        }
    }

    public void line(final int lineNumber) {
        add_(new Item() {
            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.lineNumber(lineNumber);
            }
        });
    }

    public void printf(final String format, final List<Expr> values) {
        invokeVirtual(
            MethodDesc.of(PrintStream.class, "printf", PrintStream.class, String.class, Object[].class), Expr.staticField(FieldDesc.of(System.class, "out")),
            Constant.of(format),
            newArray(CD_Object, values)
        );
    }

    public void assert_(final Function<BlockCreator, Expr> assertion, final String message) {
        if_(logicalAnd(
            Constant.ofInvoke(
                Constant.ofMethodHandle(InvokeKind.VIRTUAL, MethodDesc.of(Class.class, "desiredAssertionStatus", boolean.class)
            )
        ), assertion), __ -> {
            throw_(AssertionError.class, message);
        });
    }

    protected void processDependencies(final BlockCreatorImpl block, final ListIterator<Item> iter, final boolean verifyOnly) {
        input.process(block, iter, verifyOnly);
    }

    public void writeCode(CodeBuilder cb, final BlockCreatorImpl block) {
        cb.block(bcb -> {
            bcb.labelBinding(startLabel);
            for (final Item item : items) {
                item.writeCode(bcb, this);
            }
            if (fallsOut()) {
                exit(bcb);
            }
            bcb.labelBinding(endLabel);
        });
    }

    // non-public

    boolean lastWas(Item item) {
        if (items.isEmpty()) {
            return false;
        }
        int last = items.size() - 1;
        return items.get(last) == item;
    }

    <I extends Item> I replace_(Item item, I replacement) {
        if (! active()) {
            throw new IllegalStateException("This block is not active");
        }
        if (! lastWas(item)) {
            throw new IllegalStateException("Item mismatch");
        }
        int last = items.size() - 1;
        items.set(last, replacement);
        return replacement;
    }

    <I extends Item> I add_(I item) {
        if (! active()) {
            throw new IllegalStateException("This block is not active");
        }
        item.insert(this, items.listIterator(items.size()));
        if (item.exitsAll()) {
            markExitedAll();
        } else if (item.exitsBlock()) {
            markExited();
        }
        return item;
    }

    <C extends Cleanup> C cleanup(C cleanup) {
        if (! active()) {
            throw new IllegalStateException("This block is not active");
        }
        if (blockCleanup != null) {
            throw new IllegalStateException("Block cleanup was already set");
        }
        blockCleanup = cleanup;
        return cleanup;
    }

    Label startLabel() {
        return startLabel;
    }

    Label endLabel() {
        return endLabel;
    }

    void exitAll(final CodeBuilder cb) {
        exit(cb);
        if (parent != null) {
            parent.exit(cb);
        }
    }

    void exitTo(final CodeBuilder cb, BlockCreatorImpl block) {
        if (this == block) {
            return;
        }
        exit(cb);
        if (parent == null) {
            throw new IllegalStateException("Invalid block nesting");
        }
        parent.exitTo(cb, block);
    }

    void exit(final CodeBuilder cb) {
        if (blockCleanup != null) {
            blockCleanup.writeCleanup(cb, this);
        }
    }

    void cleanStack(final ListIterator<Item> iter) {
        // clean the block stack
        while (iter.hasPrevious()) {
            Item previous = iter.previous();
            if (previous.type().equals(CD_void)) {
                // skip it
                previous.processDependencies(this, iter, true);
            } else {
                iter.next();
                // pop it
                Pop pop = new Pop((ExprImpl) previous);
                pop.insert(this, iter);
            }
        }
    }
}
