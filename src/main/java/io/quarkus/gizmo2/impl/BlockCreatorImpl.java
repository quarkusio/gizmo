package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.impl.Preconditions.requireArray;
import static io.quarkus.gizmo2.impl.Preconditions.requireSameLoadableTypeKind;
import static io.quarkus.gizmo2.impl.Preconditions.requireSameTypeKind;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.*;
import static java.util.Collections.*;

import java.io.PrintStream;
import java.lang.constant.ClassDesc;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.ClassFile;
import io.github.dmlloyd.classfile.ClassModel;
import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.github.dmlloyd.classfile.MethodModel;
import io.github.dmlloyd.classfile.Opcode;
import io.github.dmlloyd.classfile.attribute.InnerClassInfo;
import io.github.dmlloyd.classfile.attribute.InnerClassesAttribute;
import io.github.dmlloyd.classfile.attribute.NestHostAttribute;
import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.InvokeKind;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.AnonymousClassCreator;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.creator.TryCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.quarkus.gizmo2.impl.constant.IntConst;
import io.quarkus.gizmo2.impl.constant.NullConst;
import io.quarkus.gizmo2.impl.constant.VoidConst;

/**
 * The block builder implementation. Internal only.
 */
public final class BlockCreatorImpl extends Item implements BlockCreator {
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
    /**
     * Set if this block is within a try block.
     */
    TryFinally tryFinally;
    private int state;
    private final Label startLabel;
    private final Label endLabel;
    private final Item input;
    private final ClassDesc outputType;
    private final ClassDesc returnType;
    private Consumer<BlockCreator> loopAction;

    private String nestSite;
    private String finishSite;

    private int anonClassCount;
    private List<Consumer<BlockCreator>> postInits;

    BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final ClassDesc returnType) {
        this(owner, outerCodeBuilder, null, CD_void, ConstImpl.ofVoid(), CD_void, returnType);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent) {
        this(parent, ConstImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc inputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, inputType, ConstImpl.ofVoid(), CD_void, parent.returnType);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final Item input, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, input.type(), input, outputType, parent.returnType);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc inputType, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, inputType, ConstImpl.ofVoid(), outputType, parent.returnType);
    }

    private BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final BlockCreatorImpl parent,
            final ClassDesc inputType, final Item input, final ClassDesc outputType, final ClassDesc returnType) {
        this.outerCodeBuilder = outerCodeBuilder;
        this.parent = parent;
        this.owner = owner;
        depth = parent == null ? 0 : parent.depth + 1;
        tryFinally = parent == null ? null : parent.tryFinally;
        postInits = parent == null ? List.of() : parent.postInits;
        startLabel = newLabel();
        endLabel = newLabel();
        this.input = input;
        head = Node.newList(BlockHeader.INSTANCE, Yield.YIELD_VOID);
        tail = head.next();
        if (!inputType.equals(CD_void)) {
            head.insertNext(new BlockExpr(inputType));
        }
        this.outputType = outputType;
        this.returnType = returnType;
    }

    Node head() {
        return head;
    }

    Node tail() {
        return tail;
    }

    BlockCreatorImpl parent() {
        return parent;
    }

    Label newLabel() {
        return outerCodeBuilder.newLabel();
    }

    ClassDesc returnType() {
        return returnType;
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
        if (tailItem instanceof Yield yield && !yield.value().isVoid()) {
            tail.set(Yield.YIELD_VOID);
            cleanStack(tail);
            return node.prev();
        } else {
            return super.pop(node);
        }
    }

    private void markDone() {
        state = ST_DONE;
        finishSite = Util.trackCaller();
    }

    public boolean isContainedBy(final BlockCreator other) {
        return this == other || parent != null && parent.isContainedBy(other);
    }

    public LocalVar declare(final String name, final ClassDesc type) {
        LocalVarImpl lv = new LocalVarImpl(this, name, type);
        addItem(lv.allocator());
        return lv;
    }

    public Expr get(final Assignable var, final MemoryOrder mode) {
        return addItem(((AssignableImpl) var).emitGet(this, mode));
    }

    public void set(final Assignable var, final Expr value, final MemoryOrder mode) {
        addItem(((AssignableImpl) var).emitSet(this, (Item) value, mode));
    }

    public void andAssign(final Assignable var, final Expr arg) {
        set(var, and(var, arg));
    }

    public void orAssign(final Assignable var, final Expr arg) {
        set(var, or(var, arg));
    }

    public void xorAssign(final Assignable var, final Expr arg) {
        set(var, xor(var, arg));
    }

    public void shlAssign(final Assignable var, final Expr arg) {
        set(var, shl(var, arg));
    }

    public void shrAssign(final Assignable var, final Expr arg) {
        set(var, shr(var, arg));
    }

    public void ushrAssign(final Assignable var, final Expr arg) {
        set(var, ushr(var, arg));
    }

    public void addAssign(final Assignable var, final Expr arg) {
        if (arg instanceof Const c) {
            inc(var, c);
        } else {
            set(var, add(var, arg));
        }
    }

    public void subAssign(final Assignable var, final Expr arg) {
        if (arg instanceof Const c) {
            dec(var, c);
        } else {
            set(var, sub(var, arg));
        }
    }

    public void mulAssign(final Assignable var, final Expr arg) {
        set(var, mul(var, arg));
    }

    public void divAssign(final Assignable var, final Expr arg) {
        set(var, div(var, arg));
    }

    public void remAssign(final Assignable var, final Expr arg) {
        set(var, rem(var, arg));
    }

    private static final Map<ClassDesc, ClassDesc> boxTypes = Map.of(
            CD_boolean, CD_Boolean,
            CD_byte, CD_Byte,
            CD_char, CD_Character,
            CD_short, CD_Short,
            CD_int, CD_Integer,
            CD_long, CD_Long,
            CD_float, CD_Float,
            CD_double, CD_Double,
            CD_void, CD_Void);

    private static final Map<ClassDesc, ClassDesc> unboxTypes = Util.reverseMap(boxTypes);

    public Expr box(final Expr a) {
        if (unboxTypes.containsKey(a.type())) {
            return a;
        }
        ClassDesc unboxType = a.type();
        ClassDesc boxType = boxTypes.get(unboxType);
        if (boxType == null) {
            throw new IllegalArgumentException("No box type for " + unboxType.displayName());
        }
        if (boxType.equals(CD_Void)) {
            throw new IllegalArgumentException("Cannot box void");
        }
        return invokeStatic(ClassMethodDesc.of(boxType, "valueOf", MethodTypeDesc.of(boxType, unboxType)), a);
    }

    public Expr unbox(final Expr a) {
        if (boxTypes.containsKey(a.type())) {
            return a;
        }
        ClassDesc boxType = a.type();
        ClassDesc unboxType = unboxTypes.get(boxType);
        if (unboxType == null) {
            throw new IllegalArgumentException("No unbox type for " + boxType.displayName());
        }
        if (unboxType.equals(CD_void)) {
            throw new IllegalArgumentException("Cannot unbox void");
        }
        return invokeVirtual(ClassMethodDesc.of(boxType, switch (TypeKind.from(unboxType)) {
            case BOOLEAN -> "booleanValue";
            case BYTE -> "byteValue";
            case CHAR -> "charValue";
            case SHORT -> "shortValue";
            case INT -> "intValue";
            case LONG -> "longValue";
            case FLOAT -> "floatValue";
            case DOUBLE -> "doubleValue";
            default -> throw impossibleSwitchCase(TypeKind.from(unboxType));
        }, MethodTypeDesc.of(unboxType, Util.NO_DESCS)), a);
    }

    public Expr switchEnum(final ClassDesc outputType, final Expr enumExpr, final Consumer<SwitchCreator> builder) {
        EnumSwitchCreatorImpl sci = new EnumSwitchCreatorImpl(this, enumExpr, outputType);
        sci.accept(builder);
        addItem(sci);
        return sci;
    }

    public Expr switch_(final ClassDesc outputType, final Expr expr, final Consumer<SwitchCreator> builder) {
        SwitchCreatorImpl<? extends ConstImpl> sci = switch (expr.typeKind().asLoadable()) {
            case INT -> new IntSwitchCreatorImpl(this, expr, outputType);
            case LONG -> new LongSwitchCreatorImpl(this, expr, outputType);
            case REFERENCE -> {
                if (expr.type().equals(CD_String)) {
                    yield new StringSwitchCreatorImpl(this, expr, outputType);
                } else if (expr.type().equals(CD_Class)) {
                    yield new ClassSwitchCreatorImpl(this, expr, outputType);
                } else {
                    throw new UnsupportedOperationException("Switch type " + expr.type() + " not supported");
                }
            }
            default -> throw new UnsupportedOperationException("Switch type " + expr.type() + " not supported");
        };
        sci.accept(builder);
        addItem(sci);
        return sci;
    }

    public void gotoCase(final SwitchCreator switch_, final Const case_) {
        SwitchCreatorImpl<?> sci = (SwitchCreatorImpl<?>) switch_;
        if (!sci.contains(this)) {
            throw new IllegalArgumentException("The given switch statement does not enclose this block");
        }
        addItem(new GotoCase(switch_, case_));
    }

    public void gotoDefault(final SwitchCreator switch_) {
        addItem(new GotoDefault(switch_));
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

    public void inc(final Assignable var, Const amount) {
        ((AssignableImpl) var).emitInc(this, amount);
    }

    public void dec(final Assignable var, Const amount) {
        ((AssignableImpl) var).emitDec(this, amount);
    }

    public Expr newEmptyArray(final ClassDesc componentType, final Expr size) {
        return addItem(new NewEmptyArray(componentType, (Item) size));
    }

    private void insertNewArrayDup(final NewEmptyArray nea, final List<ArrayStore> stores, Node node, List<Item> values,
            int idx) {
        Dup dup = (Dup) stores.get(idx).arrayExpr();
        Node prev = dup.insert(node);
        insertNewArrayNextStore(nea, stores, prev, values, idx);
        // post-process
        // (not needed)
    }

    private void insertNewArrayStore(NewEmptyArray nea, List<ArrayStore> stores, Node node, List<Item> values, int idx) {
        ArrayStore store = stores.get(idx);
        Node storeNode = store.insert(node);
        // skip predecessor (value)
        Node beforeValNode = storeNode.prev();
        Item value = values.get(idx);
        if (value.bound()) {
            beforeValNode = value.verify(beforeValNode);
        }
        // (skip index for now)
        // insert dup before value
        insertNewArrayDup(nea, stores, beforeValNode.next(), values, idx);
        // post-process (inserts index)
        store.forEachDependency(storeNode, Item::insertIfUnbound);
    }

    private void insertNewArrayNextStore(NewEmptyArray nea, List<ArrayStore> stores, Node node, List<Item> values, int idx) {
        if (idx == 0) {
            // all elements processed
            nea.forEachDependency(nea.insert(node), Item::insertIfUnbound);
        } else {
            insertNewArrayStore(nea, stores, node, values, idx - 1);
        }
    }

    public Expr newArray(final ClassDesc componentType, final List<? extends Expr> values) {
        checkActive();
        // build the object graph
        int size = values.size();
        List<ArrayStore> stores = new ArrayList<>(size);
        NewEmptyArray nea = new NewEmptyArray(componentType, ConstImpl.of(size));
        for (int i = 0; i < size; i++) {
            stores.add(new ArrayStore(new Dup(nea), ConstImpl.of(i), (Item) values.get(i), componentType));
        }
        // stitch the object graph into our list
        insertNewArrayNextStore(nea, stores, tail, Util.reinterpretCast(values), values.size());
        return nea;
    }

    private Expr relZero(final Expr a, final If.Kind kind) {
        switch (a.typeKind().asLoadable()) {
            case INT, REFERENCE -> {
                // normal relZero
                return addItem(new RelZero(a, kind));
            }
            case LONG -> {
                // wrap with cmp
                return relZero(cmp(a, Const.of(0, a.typeKind())), kind);
            }
            case FLOAT, DOUBLE -> {
                // wrap with cmpg
                return relZero(cmpg(a, Const.of(0, a.typeKind())), kind);
            }
            default -> throw new IllegalStateException();
        }
    }

    private Expr rel(final Expr a, final Expr b, final If.Kind kind) {
        switch (a.typeKind().asLoadable()) {
            case INT -> {
                // normal rel
                if (a instanceof IntConst ac && ac.intValue() == 0) {
                    return relZero(b, kind.invert());
                } else if (b instanceof IntConst bc && bc.intValue() == 0) {
                    return relZero(a, kind);
                } else {
                    return addItem(new Rel(a, b, kind));
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
                if (a instanceof NullConst) {
                    return relZero(b, kind);
                } else if (b instanceof NullConst) {
                    return relZero(a, kind);
                } else {
                    return addItem(new Rel(a, b, kind));
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
        return xor(a, Const.of(-1, a.typeKind()));
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
        if (a instanceof ConstImpl c && c.isZero()) {
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

    public Expr lambda(final MethodDesc sam, final ClassDesc samOwner, final Consumer<LambdaCreator> builder) {
        ClassDesc ownerDesc = owner.type();
        String ds = ownerDesc.descriptorString();
        ClassDesc desc = ClassDesc.ofDescriptor(ds.substring(0, ds.length() - 1) + "$lambda;");
        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        final ArrayList<Expr> captureExprs = new ArrayList<>();
        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(owner.version().major(), 0);
            AnonymousClassCreatorImpl tc = new AnonymousClassCreatorImpl(desc, owner.output(), zb,
                    ConstructorDesc.of(Object.class), captureExprs);
            if (sam instanceof InterfaceMethodDesc imd) {
                // implement the interface too
                tc.implements_(imd.owner());
            }
            tc.final_();
            tc.method(sam, imc -> {
                imc.public_();
                LambdaCreatorImpl lc = new LambdaCreatorImpl(tc, (InstanceMethodCreatorImpl) imc);
                tc.preAccept();
                builder.accept(lc);
                tc.freezeCaptures();
                tc.constructor(cc -> {
                    tc.ctorSetups().forEach(action -> action.accept(cc));
                });
                tc.postAccept();
            });
        });
        owner.buildLambdaBootstrap();
        String encoded = Base64.getEncoder().encodeToString(bytes);
        MethodTypeDesc ctorType = MethodTypeDesc.of(
                samOwner,
                captureExprs.stream().map(Expr::type).toArray(ClassDesc[]::new));
        return invokeDynamic(DynamicCallSiteDesc.of(
                MethodHandleDesc.ofMethod(
                        DirectMethodHandleDesc.Kind.STATIC,
                        ownerDesc,
                        "defineLambdaCallSite",
                        MethodTypeDesc.of(
                                CD_CallSite,
                                CD_MethodHandles_Lookup,
                                CD_String,
                                CD_MethodType)),
                encoded,
                ctorType), captureExprs);
    }

    public Expr newAnonymousClass(final ConstructorDesc superCtor, final List<? extends Expr> args,
            final Consumer<AnonymousClassCreator> builder) {
        ClassDesc ownerDesc = owner.type();
        int idx = ++anonClassCount;
        String ds = ownerDesc.descriptorString();
        ClassDesc desc = ClassDesc.ofDescriptor(ds.substring(0, ds.length() - 1) + "$" + idx + ";");
        ClassFile cf = ClassFile.of(ClassFile.StackMapsOption.GENERATE_STACK_MAPS);
        final ArrayList<Expr> captureExprs = new ArrayList<>();

        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(owner.version().major(), 0);
            zb.with(NestHostAttribute.of(ownerDesc));
            zb.with(InnerClassesAttribute.of(
                    InnerClassInfo.of(desc, Optional.of(ownerDesc), Optional.empty(), 0)));
            AnonymousClassCreatorImpl tc = new AnonymousClassCreatorImpl(desc, owner.output(), zb, superCtor, captureExprs);
            tc.preAccept();
            builder.accept(tc);
            tc.freezeCaptures();
            tc.constructor(cc -> {
                tc.ctorSetups().forEach(action -> action.accept(cc));
            });
            tc.postAccept();
        });
        ClassModel cm = cf.parse(bytes);
        List<MethodModel> methods = cm.methods();
        MethodModel ourCtor = methods.get(methods.size() - 1);
        owner.output().write(desc, bytes);
        return new_(ConstructorDesc.of(desc, ourCtor.methodTypeSymbol()),
                Stream.concat(args.stream(), captureExprs.stream()).toList());
    }

    public Expr cast(final Expr a, final ClassDesc toType) {
        if (a.type().isPrimitive()) {
            if (toType.isPrimitive()) {
                return addItem(new PrimitiveCast(a, toType));
            } else if (boxTypes.containsKey(a.type()) && toType.equals(boxTypes.get(a.type()))) {
                return box(a);
            } else {
                throw new IllegalArgumentException("Cannot cast primitive value of type '" + a.type().displayName()
                        + "' to object type '" + toType.displayName() + "'");
            }
        } else {
            if (unboxTypes.containsKey(a.type()) && toType.equals(unboxTypes.get(a.type()))) {
                return unbox(a);
            } else if (toType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot cast object value of type '" + a.type().displayName()
                        + "' to primitive type '" + toType.displayName() + "'");
            } else {
                return addItem(new CheckCast(a, toType));
            }
        }
    }

    public Expr uncheckedCast(final Expr a, final ClassDesc toType) {
        if (a.type().isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive value: " + a.type().displayName());
        }
        if (toType.isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive type: " + toType.displayName());
        }
        return addItem(new UncheckedCast(a, toType));
    }

    public Expr instanceOf(final Expr obj, final ClassDesc type) {
        return addItem(new InstanceOf(obj, type));
    }

    public Expr new_(final ConstructorDesc ctor, final List<? extends Expr> args) {
        checkActive();
        New new_ = new New(ctor.owner());
        Dup dup_ = new Dup(new_);
        Node node = tail.prev();
        // insert New & Dup *before* the arguments
        for (int i = args.size() - 1; i >= 0; i--) {
            Item arg = (Item) args.get(i);
            if (arg.bound()) {
                node = arg.verify(node);
            }
        }
        Node dupNode = dup_.insert(node.next());
        new_.insert(dupNode);
        // finally, add the invoke at tail
        addItem(new Invoke(ctor, dup_, args));
        // the New is all that is left on the stack now
        return new_;
    }

    public Expr invokeStatic(final MethodDesc method, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESTATIC, method, null, args));
    }

    public Expr invokeVirtual(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKEVIRTUAL, method, instance, args));
    }

    public Expr invokeSpecial(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESPECIAL, method, instance, args));
    }

    public Expr invokeSpecial(final ConstructorDesc ctor, final Expr instance, final List<? extends Expr> args) {
        Invoke invoke = new Invoke(ctor, instance, args);
        addItem(invoke);
        if (instance instanceof ThisExpr) {
            // self-init
            for (Consumer<BlockCreator> postInit : postInits) {
                postInit.accept(this);
            }
        }
        return invoke;
    }

    public Expr invokeInterface(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        if (!(method instanceof InterfaceMethodDesc)) {
            throw new IllegalArgumentException("Cannot emit `invokeinterface` for " + method + "; must be InterfaceMethodDesc");
        }
        return addItem(new Invoke(Opcode.INVOKEINTERFACE, method, instance, args));
    }

    public Expr invokeDynamic(final DynamicCallSiteDesc callSiteDesc, final List<? extends Expr> args) {
        return addItem(new Item() {
            protected Node forEachDependency(Node node, final BiFunction<Item, Node, Node> op) {
                node = node.prev();
                for (int i = args.size() - 1; i >= 0; i--) {
                    final Item arg = (Item) args.get(i);
                    node = arg.process(node, op);
                }
                return node;
            }

            public ClassDesc type() {
                return callSiteDesc.invocationType().returnType();
            }

            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.invokedynamic(callSiteDesc);
            }
        });
    }

    public void forEach(final Expr fn, final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        block(fn, (b0, fn0) -> {
            Var items = b0.define("$$items" + depth, fn0);
            if (items.type().isArray()) {
                // iterate array
                Expr lv = items.length();
                Expr length = lv instanceof Const ? lv : b0.define("$$length" + depth, lv);
                LocalVar idx = b0.define("$$idx" + depth, Const.of(0));
                b0.block(b1 -> {
                    b1.if_(b1.lt(idx, length), b2 -> {
                        LocalVar val = b2.define("$$val" + depth, items.elem(idx));
                        builder.accept(b2, val);
                        if (b2.active()) {
                            b2.inc(idx);
                            b2.gotoStart();
                        }
                    });
                });
            } else {
                // use iterable
                LocalVar itr = b0.define("$$itr" + depth, b0.iterate(items));
                b0.block(b1 -> {
                    b1.if_(b1.withIterator(itr).hasNext(), b2 -> {
                        LocalVar val = b2.define("$$val" + depth, b2.withIterator(itr).next());
                        ((BlockCreatorImpl) b2).loopAction = bb -> bb.goto_(b1);
                        builder.accept(b2, val);
                        if (b2.active()) {
                            b2.goto_(b1);
                        }
                    });
                });
            }
        });
    }

    void block(final Expr arg, BiConsumer<BlockCreator, Expr> nested) {
        BlockCreatorImpl block = new BlockCreatorImpl(this, (Item) arg, CD_void);
        block.accept(nested);
        addItem(block);
    }

    public void block(final Consumer<BlockCreator> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this);
        state = ST_NESTED;
        nestSite = Util.trackCaller();
        block.accept(nested);
        state = ST_ACTIVE;
        nestSite = null;
        addItem(block);
    }

    public Expr blockExpr(final ClassDesc type, final Consumer<BlockCreator> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        state = ST_NESTED;
        nestSite = Util.trackCaller();
        block.accept(nested);
        state = ST_ACTIVE;
        nestSite = null;
        // inline it
        if (block.tail.item() instanceof Yield yield && yield.value().isVoid()) {
            // block should be safe to inline
            Node node = block.head.next();
            while (node.item() != yield) {
                tail.insertPrev(node.item());
                node = node.next();
            }
            return tail.prev().item();
        } else {
            addItem(block);
            return block;
        }
    }

    public void accept(final BiConsumer<? super BlockCreatorImpl, Expr> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        Expr input;
        if (head.next().item() instanceof BlockExpr be) {
            input = be;
        } else {
            // void-input-typed block
            input = VoidConst.INSTANCE;
        }
        handler.accept(this, input);
        cleanStack(tail.item().process(tail, Item::verify));
        markDone();
    }

    public void accept(final Consumer<? super BlockCreatorImpl> handler) {
        if (state != ST_ACTIVE) {
            throw new IllegalStateException("Block already processed");
        }
        handler.accept(this);
        if (tail.item() instanceof Yield yield) {
            Expr val = yield.value();
            if (val.typeKind() != typeKind()) {
                if (val.typeKind() == TypeKind.VOID) {
                    throw new IllegalStateException(
                            "Block did not yield a value of type " + typeKind() + " (did you forget to call `yield(val)`?)");
                } else {
                    throw new IllegalStateException(
                            "Block yielded value of wrong type (expected a " + typeKind() + " but got " + val.type() + ")");
                }
            }
        }
        cleanStack(tail.item().process(tail, Item::verify));
        markDone();
    }

    public void ifInstanceOf(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, ? super LocalVar> ifTrue) {
        doIf(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.define("$$instance" + depth, bc.cast(obj, type))), null);
    }

    public void ifNotInstanceOf(Expr obj, ClassDesc type, Consumer<BlockCreator> ifFalse) {
        doIf(instanceOf(obj, type), null, ifFalse);
    }

    public void ifInstanceOfElse(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, ? super LocalVar> ifTrue,
            final Consumer<BlockCreator> ifFalse) {
        doIf(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.define("$$instance" + depth, bc.cast(obj, type))), ifFalse);
    }

    private If doIfInsn(final ClassDesc type, final Expr cond, final BlockCreatorImpl wt, final BlockCreatorImpl wf) {
        // try to combine the condition into the `if`
        if (cond.bound()) {
            Item prevItem = tail.prev().item();
            if (prevItem == cond) {
                if (cond instanceof Rel rel) {
                    IfRel ifRel = new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right());
                    if (ifRel.mayFallThrough()) {
                        rel.replace(tail.prev(), ifRel);
                    } else {
                        rel.remove(tail.prev());
                        replaceLastItem(ifRel);
                    }
                    return ifRel;
                } else if (cond instanceof RelZero rz) {
                    IfZero ifZero = new IfZero(type, rz.kind(), wt, wf, rz.input());
                    if (ifZero.mayFallThrough()) {
                        rz.replace(tail.prev(), ifZero);
                    } else {
                        rz.remove(tail.prev());
                        replaceLastItem(ifZero);
                    }
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
        return addItem(new IfZero(type, If.Kind.NE, wt, wf, (Item) cond));
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

    public Expr cond(final ClassDesc type, final Expr cond, final Consumer<BlockCreator> whenTrue,
            final Consumer<BlockCreator> whenFalse) {
        BlockCreatorImpl wt = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        BlockCreatorImpl wf = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        wt.accept(whenTrue);
        wf.accept(whenFalse);
        return doIfInsn(type, cond, wt, wf);
    }

    public void if_(final Expr cond, final Consumer<BlockCreator> whenTrue) {
        doIf(cond, whenTrue, null);
    }

    public void ifNot(final Expr cond, final Consumer<BlockCreator> whenFalse) {
        doIf(cond, null, whenFalse);
    }

    public void ifElse(final Expr cond, final Consumer<BlockCreator> whenTrue, final Consumer<BlockCreator> whenFalse) {
        doIf(cond, whenTrue, whenFalse);
    }

    public void break_(final BlockCreator outer) {
        ((BlockCreatorImpl) outer).breakTarget = true;
        if (outer != this) {
            addItem(new Break(outer));
        }
        markDone();
    }

    public void continue_(final BlockCreator loop) {
        BlockCreatorImpl bci = (BlockCreatorImpl) loop;
        Consumer<BlockCreator> action = bci.loopAction;
        if (action == null) {
            throw new IllegalArgumentException("Can only continue a loop");
        }
        action.accept(this);
    }

    public void goto_(final BlockCreator outer) {
        if (!outer.contains(this)) {
            throw new IllegalStateException("Invalid block nesting");
        }
        addItem(new GotoStart(outer));
        markDone();
    }

    public void loop(final Consumer<BlockCreator> body) {
        block(b0 -> {
            ((BlockCreatorImpl) b0).loopAction = bb -> bb.goto_(b0);
            body.accept(b0);
            if (b0.active()) {
                b0.gotoStart();
            }
        });
    }

    public void while_(final Consumer<BlockCreator> cond, final Consumer<BlockCreator> body) {
        block(b0 -> b0.if_(b0.blockExpr(CD_boolean, cond), b1 -> {
            ((BlockCreatorImpl) b1).loopAction = bb -> bb.goto_(b0);
            body.accept(b1);
            if (b1.active()) {
                b1.goto_(b0);
            }
        }));
    }

    public void doWhile(final Consumer<BlockCreator> body, final Consumer<BlockCreator> cond) {
        block(b0 -> {
            b0.block(b1 -> {
                ((BlockCreatorImpl) b1).loopAction = bb -> bb.break_(b1);
                body.accept(b1);
            });
            if (b0.active()) {
                b0.if_(b0.blockExpr(CD_boolean, cond), b1 -> b1.goto_(b0));
            }
        });
    }

    public void try_(final Consumer<TryCreator> body) {
        TryCreatorImpl tci = new TryCreatorImpl(this);
        tci.accept(body);
        tci.addTo(this);
    }

    public void autoClose(final Expr resource, final BiConsumer<BlockCreator, ? super LocalVar> body) {
        if (resource instanceof LocalVar lv) {
            autoClose(lv, b0 -> {
                body.accept(b0, lv);
            });
        } else {
            block(resource, (b0, opened) -> {
                LocalVar rsrc = b0.define("$$resource" + depth, opened);
                autoClose(rsrc, b1 -> {
                    body.accept(b1, rsrc);
                });
            });
        }
    }

    public void autoClose(final LocalVar resource, final Consumer<BlockCreator> body) {
        try_(t1 -> {
            t1.body(body);
            t1.catch_(CD_Throwable, "e2", (b2, e2) -> {
                b2.try_(t3 -> {
                    t3.body(b4 -> {
                        b4.close(resource);
                    });
                    t3.catch_(CD_Throwable, "e4", (b4, e4) -> {
                        b4.withThrowable(e2).addSuppressed(e4);
                    });
                });
                b2.throw_(e2);
            });
        });
        if (active()) {
            close(resource);
        }
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
            LocalVar mv = b0.define("$$monitor" + depth, mon);
            ((BlockCreatorImpl) b0).monitorEnter((Item) mv);
            b0.try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> ((BlockCreatorImpl) b2).monitorExit((Item) mv));
            });
        });
    }

    public void locked(final Expr jucLock, final Consumer<BlockCreator> body) {
        block(jucLock, (b0, lock) -> {
            LocalVar lv = b0.define("$$lock" + depth, lock);
            b0.invokeInterface(MethodDesc.of(Lock.class, "lock", void.class), lv);
            b0.try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> b2.invokeInterface(MethodDesc.of(Lock.class, "unlock", void.class), lv));
            });
        });
    }

    public void returnNull() {
        return_(ConstImpl.ofNull(returnType));
    }

    public void return_() {
        replaceLastItem(Return.RETURN_VOID);
    }

    public void return_(final Expr val) {
        requireSameLoadableTypeKind(returnType, val.type());
        replaceLastItem(val.equals(Const.ofVoid()) ? Return.RETURN_VOID : new Return(val));
    }

    public void throw_(final Expr val) {
        replaceLastItem(new Throw(val));
    }

    public void yield(final Expr val) {
        requireSameLoadableTypeKind(this, val);
        replaceLastItem(val.equals(Const.ofVoid()) ? Yield.YIELD_VOID : new Yield(val));
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
            case REFERENCE -> invokeStatic(MethodDesc.of(Objects.class, "hashCode", int.class, Object.class), expr);
            case VOID -> Const.of(0); // null constant
        };
    }

    public Expr exprEquals(final Expr a, final Expr b) {
        return switch (a.typeKind()) {
            case REFERENCE -> switch (b.typeKind()) {
                case REFERENCE ->
                    invokeStatic(MethodDesc.of(Objects.class, "equals", boolean.class, Object.class, Object.class), a, b);
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
            case REFERENCE -> expr.type().equals(CD_char.arrayType()) ? char[].class : Object.class;
            default -> throw new IllegalArgumentException("Invalid type for `toString`: " + expr);
        }), expr);
    }

    public Expr arrayHashCode(final Expr expr) {
        requireArray(expr);

        ClassDesc componentType = expr.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MethodDesc.of(Arrays.class, "deepHashCode", int.class, Object[].class), expr);
        } else {
            ClassDesc type = TypeKind.from(componentType) == TypeKind.REFERENCE ? CD_Object.arrayType() : expr.type();
            return invokeStatic(MethodDesc.of(Arrays.class, "hashCode", MethodTypeDesc.of(CD_int, type)), expr);
        }
    }

    public Expr arrayEquals(final Expr a, final Expr b) {
        requireArray(a);
        requireArray(b);
        requireSameTypeKind(a.type().componentType(), b.type().componentType());

        ClassDesc componentType = a.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MethodDesc.of(Arrays.class, "deepEquals", boolean.class, Object[].class, Object[].class), a, b);
        } else {
            ClassDesc type = TypeKind.from(componentType) == TypeKind.REFERENCE ? CD_Object.arrayType() : a.type();
            return invokeStatic(MethodDesc.of(Arrays.class, "equals", MethodTypeDesc.of(CD_boolean, type, type)), a, b);
        }
    }

    public Expr arrayToString(final Expr expr) {
        requireArray(expr);

        ClassDesc componentType = expr.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MethodDesc.of(Arrays.class, "deepToString", String.class, Object[].class), expr);
        } else {
            ClassDesc type = TypeKind.from(componentType) == TypeKind.REFERENCE ? CD_Object.arrayType() : expr.type();
            return invokeStatic(MethodDesc.of(Arrays.class, "toString", MethodTypeDesc.of(CD_String, type)), expr);
        }
    }

    public Expr classForName(final Expr className) {
        return invokeStatic(MethodDesc.of(Class.class, "forName", Class.class, String.class), className);
    }

    public Expr listOf(final List<? extends Expr> items) {
        int size = items.size();
        if (size <= 10) {
            return invokeStatic(MethodDesc.of(List.class, "of", List.class, nCopies(size, Object.class)), items);
        } else {
            return invokeStatic(MethodDesc.of(List.class, "of", List.class, Object[].class), newArray(Object.class, items));
        }
    }

    public Expr setOf(final List<? extends Expr> items) {
        int size = items.size();
        if (size <= 10) {
            return invokeStatic(MethodDesc.of(Set.class, "of", Set.class, nCopies(size, Object.class)), items);
        } else {
            return invokeStatic(MethodDesc.of(Set.class, "of", Set.class, Object[].class), newArray(Object.class, items));
        }
    }

    @Override
    public Expr mapOf(List<? extends Expr> items) {
        items = List.copyOf(items);
        int size = items.size();
        if (size % 2 != 0) {
            throw new IllegalArgumentException("Invalid number of items: " + items);
        }
        if (size <= 20) {
            return invokeStatic(MethodDesc.of(Map.class, "of", Map.class, nCopies(items.size(), Object.class)), items);
        } else {
            throw new UnsupportedOperationException("Maps with more than 10 entries are not supported");
        }
    }

    public Expr mapEntry(final Expr key, final Expr value) {
        return invokeStatic(MethodDesc.of(Map.class, "entry", Map.Entry.class, Object.class, Object.class), key, value);
    }

    @Override
    public Expr optionalOf(Expr value) {
        return invokeStatic(MethodDesc.of(Optional.class, "of", Optional.class, Object.class), value);
    }

    @Override
    public Expr optionalOfNullable(Expr value) {
        return invokeStatic(MethodDesc.of(Optional.class, "ofNullable", Optional.class, Object.class), value);
    }

    public void line(final int lineNumber) {
        addItem(new Item() {
            public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
                cb.lineNumber(lineNumber);
            }
        });
    }

    public void printf(final String format, final List<? extends Expr> values) {
        invokeVirtual(
                MethodDesc.of(PrintStream.class, "printf", PrintStream.class, String.class, Object[].class),
                Expr.staticField(FieldDesc.of(System.class, "out")),
                Const.of(format),
                newArray(CD_Object, values));
    }

    public void assert_(final Consumer<BlockCreator> assertion, final String message) {
        if_(logicalAnd(Const.ofInvoke(Const.ofMethodHandle(InvokeKind.VIRTUAL,
                MethodDesc.of(Class.class, "desiredAssertionStatus", boolean.class))), assertion),
                __ -> throw_(AssertionError.class, message));
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

    void postInit(final List<Consumer<BlockCreator>> postInits) {
        this.postInits = postInits;
    }

    <I extends Item> I addItem(I item) {
        checkActive();
        Node node = item.insert(tail);
        item.forEachDependency(node, Item::insertIfUnbound);
        if (!item.mayFallThrough()) {
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

    private void checkActive() {
        if (state == ST_DONE) {
            if (finishSite == null) {
                throw new IllegalStateException("This block has already been finished" + Util.trackingMessage);
            } else {
                throw new IllegalStateException("This block has already been finished at " + finishSite);
            }
        }
        if (state == ST_NESTED) {
            if (nestSite == null) {
                throw new IllegalStateException("This block is currently not active,"
                        + " because a nested block is being created" + Util.trackingMessage);
            } else {
                throw new IllegalStateException("This block is currently not active,"
                        + " because a nested block is being created, starting at " + nestSite);
            }
        }
        // leaving this just for future-proofing
        if (!active()) {
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
