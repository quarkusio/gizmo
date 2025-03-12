package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;
import static java.util.Collections.nCopies;

import java.io.PrintStream;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.creator.SwitchExprCreator;
import io.quarkus.gizmo2.creator.TryCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ClassConstant;
import io.quarkus.gizmo2.impl.constant.ConstantImpl;
import io.quarkus.gizmo2.impl.constant.EnumConstant;
import io.quarkus.gizmo2.impl.constant.IntConstant;
import io.quarkus.gizmo2.impl.constant.NullConstant;
import io.quarkus.gizmo2.impl.constant.StringConstant;

/**
 * The block builder implementation. Internal only.
 */
sealed public class BlockCreatorImpl extends Item implements BlockCreator, Scoped<BlockCreatorImpl> permits SwitchCreatorImpl.Case {
    private static final int ST_ACTIVE = 0;
    private static final int ST_NESTED = 1;
    private static final int ST_DONE = 2;

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
    private final Node head;
    private final Node tail;
    private boolean breakTarget;
    private Cleanup blockCleanup = null;
    private int state;
    private final Label startLabel;
    private final Label endLabel;
    private final Item input;
    private final ClassDesc outputType;

    BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder) {
        this(owner, outerCodeBuilder, null, CD_void, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent) {
        this(parent, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc headerType) {
        this(parent.owner, parent.outerCodeBuilder, parent, headerType, ConstantImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final Item input, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, input.type(), input, outputType);
    }

    private BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final BlockCreatorImpl parent, final ClassDesc inputType, final Item input, final ClassDesc outputType) {
        this.outerCodeBuilder = outerCodeBuilder;
        this.parent = parent;
        this.owner = owner;
        depth = parent == null ? 0 : parent.depth + 1;
        startLabel = newLabel();
        endLabel = newLabel();
        this.input = input;
        head = Node.newList(BlockHeader.INSTANCE, Yield.YIELD_VOID);
        tail = head.next();
        if (! inputType.equals(CD_void)) {
            head.insertNext(new BlockExpr(inputType));
        }
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
        return state == ST_DONE;
    }

    public boolean mayFallThrough() {
        if (active()) {
            //throw new IllegalStateException();
        }
        return breakTarget || tail.item().mayFallThrough();
    }

    public boolean mayThrow() {
        if (active()) {
            throw new IllegalStateException();
        }
        // todo: compute once & cache
        Node node = tail;
        while (node != null) {
            if (node.item().mayThrow()) {
                return true;
            }
            node = node.prev();
        }
        return false;
    }

    public boolean mayBreak() {
        if (active()) {
            throw new IllegalStateException();
        }
        // todo: compute once & cache
        Node node = tail;
        while (node != null) {
            if (node.item().mayBreak()) {
                return true;
            }
            node = node.prev();
        }
        return false;
    }

    public boolean mayReturn() {
        if (active()) {
            throw new IllegalStateException();
        }
        // todo: compute once & cache
        Node node = tail;
        while (node != null) {
            if (node.item().mayReturn()) {
                return true;
            }
            node = node.prev();
        }
        return false;
    }

    public Node pop(final Node node) {
        assert this == node.item();
        if (isVoid()) {
            return super.pop(node);
        }
        assert mayFallThrough();
        // else, pop *our* result
        if (breakTarget) {
            // need an explicit pop node
            return super.pop(node);
        }
        Item tailItem = tail.item();
        if (tailItem instanceof Yield yield && ! yield.value().isVoid()) {
            tail.set(Yield.YIELD_VOID);
            cleanStack(tail);
            return node.prev();
        } else {
            return super.pop(node);
        }
    }

    private void markDone() {
        state = ST_DONE;
    }

    public boolean isContainedBy(final BlockCreator other) {
        return this == other || parent != null && parent.isContainedBy(other);
    }

    public LocalVar declare(final String name, final ClassDesc type) {
        LocalVarImpl lv = new LocalVarImpl(this, name, type);
        addItem(lv.allocator());
        return lv;
    }

    public Expr get(final LValueExpr var, final AccessMode mode) {
        return addItem(((LValueExprImpl) var).emitGet(this, mode));
    }

    public void set(final LValueExpr var, final Expr value, final AccessMode mode) {
        addItem(((LValueExprImpl) var).emitSet(this, (Item) value, mode));
    }

    public void andAssign(final LValueExpr var, final Expr arg) {
        set(var, and(var, arg));
    }

    public void orAssign(final LValueExpr var, final Expr arg) {
        set(var, or(var, arg));
    }

    public void xorAssign(final LValueExpr var, final Expr arg) {
        set(var, xor(var, arg));
    }

    public void shlAssign(final LValueExpr var, final Expr arg) {
        set(var, shl(var, arg));
    }

    public void shrAssign(final LValueExpr var, final Expr arg) {
        set(var, shr(var, arg));
    }

    public void ushrAssign(final LValueExpr var, final Expr arg) {
        set(var, ushr(var, arg));
    }

    public void addAssign(final LValueExpr var, final Expr arg) {
        if (arg instanceof Constant c) {
            inc(var, c);
        } else {
            set(var, add(var, arg));
        }
    }

    public void subAssign(final LValueExpr var, final Expr arg) {
        if (arg instanceof Constant c) {
            dec(var, c);
        } else {
            set(var, sub(var, arg));
        }
    }

    public void mulAssign(final LValueExpr var, final Expr arg) {
        set(var, mul(var, arg));
    }

    public void divAssign(final LValueExpr var, final Expr arg) {
        set(var, div(var, arg));
    }

    public void remAssign(final LValueExpr var, final Expr arg) {
        set(var, rem(var, arg));
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

    public void switchEnum(final Expr enumExpr, final Consumer<SwitchCreator> builder) {
        SwitchCreatorImpl<? extends ConstantImpl> sci = new HashingSwitch<>(this, enumExpr, EnumConstant.class,
            cb -> {
                cb.invokevirtual(CD_Enum, "name", MethodTypeDesc.of(CD_String));
                cb.invokevirtual(CD_String, "hashCode", MethodTypeDesc.of(CD_int));
            },
            cc -> cc.name().hashCode()
        );
        addItem(sci);
    }

    public void switch_(final Expr expr, final Consumer<SwitchCreator> builder) {
        SwitchCreatorImpl<? extends ConstantImpl> sci = switch (expr.typeKind().asLoadable()) {
            case INT -> new IntSwitch(this, expr);
            case REFERENCE -> {
                if (expr.type().equals(CD_String)) {
                    yield new HashingSwitch<>(this, expr, StringConstant.class);
                } else if (expr.type().equals(CD_Class)) {
                    yield new HashingSwitch<>(this, expr, ClassConstant.class,
                        cb -> {
                            cb.invokevirtual(CD_Class, "descriptorString", MethodTypeDesc.of(CD_String));
                            cb.invokevirtual(CD_String, "hashCode", MethodTypeDesc.of(CD_int));
                        },
                        cc -> cc.desc().descriptorString().hashCode()
                    );
                } else {
                    throw new UnsupportedOperationException("Switch type " + expr.type() + " not supported");
                }
            }
            default -> throw new UnsupportedOperationException("Switch type " + expr.type() + " not supported");
        };
        addItem(sci);
    }

    public void redo(final SwitchCreator switch_, final Constant case_) {
        addItem(new Item() {
            protected Node insert(final Node node) {
                Node res = super.insert(node);
                cleanStack(res);
                return res;
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                SwitchCreatorImpl<?> cast = (SwitchCreatorImpl<?>) switch_;
                BlockCreatorImpl matched = cast.findCase(case_);
                if (matched == null) {
                    matched = cast.findDefault();
                }
                cb.goto_(matched.startLabel());
            }

            public boolean mayFallThrough() {
                return false;
            }
        });
    }

    public void redoDefault(final SwitchCreator switch_) {
        addItem(new Item() {
            protected Node insert(final Node node) {
                Node res = super.insert(node);
                cleanStack(node);
                return res;
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                SwitchCreatorImpl<?> cast = (SwitchCreatorImpl<?>) switch_;
                BlockCreatorImpl default_ = cast.findDefault();
                cb.goto_(default_.startLabel());
            }

            public boolean mayFallThrough() {
                return false;
            }
        });
    }

    public Expr iterate(final Expr items) {
        return invokeInterface(MethodDesc.of(Iterable.class, "iterator", Iterator.class), items);
    }

    public Expr currentThread() {
        return invokeStatic(MethodDesc.of(Thread.class, "currentThread", void.class));
    }

    public void close(final Expr closeable) {
        invokeInterface(MethodDesc.of(AutoCloseable.class, "close", void.class), closeable);
    }

    public void addSuppressed(final Expr throwable, final Expr suppressed) {
        invokeVirtual(MethodDesc.of(Throwable.class, "addSuppressed", void.class, Throwable.class), throwable, suppressed);
    }

    public void inc(final LValueExpr var, Constant amount) {
        ((LValueExprImpl) var).emitInc(this, amount);
    }

    public void dec(final LValueExpr var, Constant amount) {
        ((LValueExprImpl) var).emitDec(this, amount);
    }

    public Expr newEmptyArray(final ClassDesc elemType, final Expr size) {
        return addItem(new NewEmptyArray(elemType, (Item) size));
    }

    public Expr newArray(final ClassDesc elementType, final List<Expr> values) {
        if (values.isEmpty()) {
            return newEmptyArray(elementType, ConstantImpl.of(0));
        }
        return addItem(new NewArray(elementType, values));
    }

    private Expr relZero(final Expr a, final If.Kind kind) {
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
        return addItem(new Cmp(a, b, Cmp.Kind.CMP));
    }

    public Expr cmpl(final Expr a, final Expr b) {
        return addItem(new Cmp(a, b, Cmp.Kind.CMPL));
    }

    public Expr cmpg(final Expr a, final Expr b) {
        return addItem(new Cmp(a, b, Cmp.Kind.CMPG));
    }

    public Expr and(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.AND));
    }

    public Expr or(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.OR));
    }

    public Expr xor(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.XOR));
    }

    public Expr complement(final Expr a) {
        return xor(a, Constant.of(-1, a.typeKind()));
    }

    public Expr shl(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.SHL));
    }

    public Expr shr(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.SHR));
    }

    public Expr ushr(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.USHR));
    }

    public Expr add(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.ADD));
    }

    public Expr sub(final Expr a, final Expr b) {
        if (a instanceof ConstantImpl c && c.isZero()) {
            return neg(b);
        }
        return addItem(new BinOp(a, b, BinOp.Kind.SUB));
    }

    public Expr mul(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.MUL));
    }

    public Expr div(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.DIV));
    }

    public Expr rem(final Expr a, final Expr b) {
        return addItem(new BinOp(a, b, BinOp.Kind.REM));
    }

    public Expr neg(final Expr a) {
        return addItem(new Neg(a));
    }

    public Expr switchExpr(final Expr val, final Consumer<SwitchExprCreator> builder) {
        throw new UnsupportedOperationException();
    }

    public Expr lambda(final MethodDesc sam, final Consumer<LambdaCreator> builder) {
        throw new UnsupportedOperationException();
    }

    public Expr cast(final Expr a, final ClassDesc toType) {
        if (a.type().isPrimitive()) {
            if (toType.isPrimitive()) {
                return addItem(new PrimitiveCast(a, toType));
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
                return addItem(new CheckCast(a, toType));
            }
        }
    }

    public Expr instanceOf(final Expr obj, final ClassDesc type) {
        return addItem(new InstanceOf(obj, type));
    }

    public Expr new_(final ConstructorDesc ctor, final List<Expr> args) {
        return addItem(new Invoke(ctor, new New(ctor.owner()), true, args));
    }

    public Expr invokeStatic(final MethodDesc method, final List<Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESTATIC, method, null, args));
    }

    public Expr invokeVirtual(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return addItem(new Invoke(Opcode.INVOKEVIRTUAL, method, instance, args));
    }

    public Expr invokeSpecial(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESPECIAL, method, instance, args));
    }

    public Expr invokeSpecial(final ConstructorDesc ctor, final Expr instance, final List<Expr> args) {
        return addItem(new Invoke(ctor, instance, false, args));
    }

    public Expr invokeInterface(final MethodDesc method, final Expr instance, final List<Expr> args) {
        return addItem(new Invoke(Opcode.INVOKEINTERFACE, method, instance, args));
    }

    public void forEach(final Expr fn, final BiConsumer<BlockCreator, Expr> builder) {
        block(fn, (b0, fn0) -> {
            Var items = b0.define("$$items" + depth, fn0);
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
                    b1.if_(b1.withIterator(itr).hasNext(), b2 -> {
                        LocalVar val = b2.define("$$val" + depth, b2.withIterator(itr).next());
                        builder.accept(b2, val);
                        b2.redo(b1);
                    });
                });
            }
        });
    }

    public void block(final Expr arg, BiConsumer<BlockCreator, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, (Item) arg, CD_void);
        block.accept(nested);
        addItem(block);
        return;
    }

    public void block(final Consumer<BlockCreator> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this);
        state = ST_NESTED;
        block.accept(nested);
        state = ST_ACTIVE;
        addItem(block);
        return;
    }

    public Expr blockExpr(final ClassDesc type, final Function<BlockCreator, Expr> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this, ConstantImpl.ofVoid(), type);
        state = ST_NESTED;
        block.accept(nested);
        state = ST_ACTIVE;
        addItem(block);
        return block;
    }

    public Expr blockExpr(final Expr arg, final ClassDesc type, final BiFunction<BlockCreator, Expr, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, (Item) arg, type);
        addItem(block);
        return block.accept(nested);
    }

    public void accept(final BiConsumer<? super BlockCreatorImpl, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (! type().equals(CD_void)) {
            throw new IllegalStateException("Void accept on block which returns " + type());
        }
        if (! (head.next().item() instanceof BlockExpr be)) {
            throw new IllegalStateException("Expected block expression");
        }
        handler.accept(this, be);
        if (mayFallThrough()) {
            cleanStack(tail.apply(Item::verify));
        }
        markDone();
    }

    public void accept(final Consumer<? super BlockCreatorImpl> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (! type().equals(CD_void)) {
            throw new IllegalStateException("Void accept on block which returns " + type());
        }
        handler.accept(this);
        if (mayFallThrough()) {
            cleanStack(tail.apply(Item::verify));
        }
        markDone();
    }

    public Expr accept(final Function<? super BlockCreatorImpl, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (type().equals(CD_void)) {
            throw new IllegalStateException("Function accept on void-typed block");
        }
        Item res = (Item) handler.apply(this);
        if (mayFallThrough()) {
            Node tail = this.tail;
            tail.set(new Yield(res));
            cleanStack(tail.apply(Item::verify));
        }
        markDone();
        return this;
    }

    public Expr accept(final BiFunction<? super BlockCreatorImpl, Expr, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        if (type().equals(CD_void)) {
            throw new IllegalStateException("Function accept on void-typed block");
        }
        if (! (head.next().item() instanceof BlockExpr be)) {
            throw new IllegalStateException("Expected block expression");
        }
        Item res = (Item) handler.apply(this, be);
        if (mayFallThrough()) {
            Node tail = this.tail;
            tail.set(new Yield(res));
            cleanStack(tail.apply(Item::verify));
        }
        markDone();
        return this;
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
            Item prevItem = tail.prev().item();
            if (prevItem == cond) {
                if (cond instanceof Rel rel) {
                    IfRel ifRel = new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right());
                    rel.replace(tail.prev(), ifRel);
                    return ifRel;
                } else if (cond instanceof RelZero rz) {
                    IfZero ifZero = new IfZero(type, rz.kind(), wt, wf, rz.input());
                    rz.replace(tail.prev(), ifZero);
                    return ifZero;
                }
            }
            // failed
        } else {
            if (cond instanceof Rel rel) {
                return addItem(new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right()));
            } else if (cond instanceof RelZero rz) {
                return addItem(new IfZero(type, rz.kind(), wt, wf, rz.input()));
            }
            // failed
        }
        return addItem(new IfZero(type, If.Kind.NE, wt, null, (Item) cond));
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
        ((BlockCreatorImpl) outer).breakTarget = true;
        if (outer != this) {
            addItem(new Item() {
                public boolean mayBreak() {
                    return true;
                }

                public boolean mayFallThrough() {
                    return false;
                }

                public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                    cb.goto_(((BlockCreatorImpl) outer).endLabel());
                }
            });
        }
        markDone();
    }

    public void redo(final BlockCreator outer) {
        if (! outer.contains(this)) {
            throw new IllegalStateException("Invalid block nesting");
        }
        addItem(new Redo(outer));
        markDone();
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
        addItem(new TryImpl(this)).accept(body);
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

    void monitorEnter(final Item monitor) {
        addItem(new Item() {
            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return monitor.process(node.prev(), op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.monitorenter();
            }
        });
    }

    void monitorExit(final Item monitor) {
        addItem(new Item() {
            protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
                return monitor.process(node.prev(), op);
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.monitorexit();
            }
        });
    }

    public void synchronized_(final Expr monitor, final Consumer<BlockCreator> body) {
        block(monitor, (b0, mon) -> {
            LocalVar mv = define("$$monitor" + depth, mon);
            monitorEnter((Item) mv);
            try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> ((BlockCreatorImpl)b2).monitorExit((Item) mv));
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
        replaceLastItem(new Return());
    }

    public void return_(final Expr val) {
        replaceLastItem(new Return(val));
    }

    public void throw_(final Expr val) {
        replaceLastItem(new Throw(val));
    }

    public Expr exprHashCode(final Expr expr) {
        return switch (expr.typeKind()) {
            case BOOLEAN -> invokeStatic(MethodDesc.of(Boolean.class, "hashCode", int.class, boolean.class), expr);
            case BYTE -> invokeStatic(MethodDesc.of(Byte.class, "hashCode", int.class, byte.class), expr);
            case SHORT -> invokeStatic(MethodDesc.of(Short.class, "hashCode", int.class, short.class), expr);
            case CHAR -> invokeStatic(MethodDesc.of(Character.class, "hashCode", int.class, char.class), expr);
            case INT -> invokeStatic(MethodDesc.of(Integer.class, "hashCode", int.class, int.class), expr);
            case LONG -> invokeStatic(MethodDesc.of(Long.class, "hashCode", int.class, long.class), expr);
            case FLOAT -> invokeStatic(MethodDesc.of(Float.class, "hashCode", int.class, float.class), expr);
            case DOUBLE -> invokeStatic(MethodDesc.of(Double.class, "hashCode", int.class, double.class), expr);
            case REFERENCE -> invokeVirtual(MethodDesc.of(Object.class, "hashCode", int.class), expr);
            case VOID -> Constant.of(0); // null constant
        };
    }

    public Expr exprEquals(final Expr a, final Expr b) {
        return switch (a.typeKind()) {
            case REFERENCE -> switch (b.typeKind()) {
                case REFERENCE -> invokeStatic(MethodDesc.of(Objects.class, "equals", boolean.class, Object.class, Object.class), a, b);
                default -> exprEquals(a, box(b));
            };
            default -> switch (b.typeKind()) {
                case REFERENCE -> exprEquals(box(a), b);
                default -> eq(a, b);
            };
        };
    }

    public Expr exprToString(final Expr expr) {
        return invokeStatic(MethodDesc.of(String.class, "valueOf", String.class, switch (expr.typeKind()) {
            case BOOLEAN -> boolean.class;
            case BYTE, SHORT, INT -> int.class;
            case CHAR -> char.class;
            case LONG -> long.class;
            case FLOAT -> float.class;
            case DOUBLE -> double.class;
            case REFERENCE -> expr.type().isArray() ? switch (TypeKind.from(expr.type().componentType())) {
                case CHAR -> char[].class;
                default -> Object.class;
            } : Object.class;
            default -> throw new IllegalArgumentException("Invalid type for `toString`: " + expr);
        }), expr);
    }

    public Expr arrayEquals(final Expr a, final Expr b) {
        ClassDesc type = switch (TypeKind.from(a.type().componentType())) {
            case REFERENCE -> CD_Object.arrayType();
            default -> a.type();
        };
        return invokeStatic(MethodDesc.of(Arrays.class, "equals", MethodTypeDesc.of(CD_boolean, type, type)), a, b);
    }

    public Expr classForName(final Expr className) {
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
        addItem(new Item() {
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

    protected Node forEachDependency(final Node node, final BiFunction<Item, Node, Node> op) {
        return input.process(node.prev(), op);
    }

    public void writeCode(CodeBuilder cb, final BlockCreatorImpl block) {
        cb.block(bcb -> {
            bcb.labelBinding(startLabel);
            Node node = head;
            while (node != null) {
                node.item().writeCode(bcb, this);
                node = node.next();
            }
            bcb.labelBinding(endLabel);
        });
    }

    // non-public

    <I extends Item> I addItem(I item) {
        checkActive();
        Node node = item.insert(tail);
        item.forEachDependency(node, Item::insertIfUnbound);
        if (! item.mayFallThrough()) {
            assert tail.item() instanceof Yield;
            tail.set(item);
            node.remove();
            markDone();
        }
        return item;
    }

    <I extends Item> I replaceLastItem(I item) {
        checkActive();
        assert tail.item() instanceof Yield;
        tail.set(item);
        item.forEachDependency(tail, Item::insertIfUnbound);
        markDone();
        return item;
    }

    <C extends Cleanup> C cleanup(C cleanup) {
        checkActive();
        if (blockCleanup != null) {
            throw new IllegalStateException("Block cleanup was already set");
        }
        blockCleanup = cleanup;
        return cleanup;
    }

    private void checkActive() {
        if (! active()) {
            throw new IllegalStateException("This block is not active");
        }
    }

    Label startLabel() {
        return startLabel;
    }

    Label endLabel() {
        return endLabel;
    }

    static Node cleanStack(Node node) {
        if (node == null) {
            throw new IllegalStateException();
        }
        // clean the block stack before node
        while (node.hasPrev()) {
            // pop every unused item in the list, skipping void nodes
            node = node.item().pop(node);
            if (node == null) {
                throw new IllegalStateException();
            }
        }
        return node;
    }
}
