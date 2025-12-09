package io.quarkus.gizmo2.impl;

import static io.quarkus.gizmo2.desc.Descs.*;
import static io.quarkus.gizmo2.impl.Conversions.*;
import static io.quarkus.gizmo2.impl.Preconditions.*;
import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodHandleDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.GenericTypes;
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
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstImpl;
import io.quarkus.gizmo2.impl.constant.IntConst;
import io.quarkus.gizmo2.impl.constant.NullConst;
import io.smallrye.classfile.ClassFile;
import io.smallrye.classfile.ClassModel;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;
import io.smallrye.classfile.MethodModel;
import io.smallrye.classfile.Opcode;
import io.smallrye.classfile.TypeAnnotation;
import io.smallrye.classfile.attribute.InnerClassInfo;
import io.smallrye.classfile.attribute.InnerClassesAttribute;
import io.smallrye.classfile.attribute.NestHostAttribute;
import io.smallrye.common.constraint.Assert;

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
     * Name of the method to which this block belongs. This is used to create better names
     * of lambda methods in debug mode, so it follows the javac convention: lambdas created
     * in a static initializer use the name {@code static} and lambdas created in a constructor
     * use the name {@code new}.
     */
    private final String methodNameForLambdas;
    /**
     * All the items to emit, in order.
     */
    private final ArrayList<Item> items = new ArrayList<Item>(40);
    private boolean breakTarget;
    private boolean branchTarget;
    /**
     * Set if this block is an outermost body block of a {@code try} that also has a {@code finally}.
     * Note that this is only set when the {@code finally} block is added, so when generating the
     * {@code try} body, this is still {@code null}. Call {@link #tryFinally()} to find the enclosing
     * {@code TryFinally} inside {@link Item#writeCode(CodeBuilder, BlockCreatorImpl, StackMapBuilder)}.
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

    private List<Consumer<BlockCreator>> postInits;

    BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final ClassDesc returnType,
            final String methodNameForLambdas) {
        this(owner, outerCodeBuilder, null, CD_void, ConstImpl.ofVoid(), CD_void, returnType, methodNameForLambdas);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent) {
        this(parent, ConstImpl.ofVoid(), CD_void);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc inputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, inputType, ConstImpl.ofVoid(), CD_void, parent.returnType,
                parent.methodNameForLambdas);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final Item input, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, input.type(), input, outputType, parent.returnType,
                parent.methodNameForLambdas);
    }

    BlockCreatorImpl(final BlockCreatorImpl parent, final ClassDesc inputType, final ClassDesc outputType) {
        this(parent.owner, parent.outerCodeBuilder, parent, inputType, ConstImpl.ofVoid(), outputType, parent.returnType,
                parent.methodNameForLambdas);
    }

    private BlockCreatorImpl(final TypeCreatorImpl owner, final CodeBuilder outerCodeBuilder, final BlockCreatorImpl parent,
            final ClassDesc inputType, final Item input, final ClassDesc outputType, final ClassDesc returnType,
            final String methodNameForLambdas) {
        this.outerCodeBuilder = outerCodeBuilder;
        this.parent = parent;
        this.owner = owner;
        depth = parent == null ? 0 : parent.depth + 1;
        this.methodNameForLambdas = methodNameForLambdas;
        postInits = parent == null ? List.of() : parent.postInits;
        startLabel = newLabel();
        endLabel = newLabel();
        this.input = input;
        if (Util.isVoid(inputType)) {
            items.add(BlockHeader.VOID);
        } else {
            items.add(new BlockHeader(inputType));
        }
        this.outputType = outputType;
        this.returnType = returnType;
    }

    BlockCreatorImpl parent() {
        return parent;
    }

    /**
     * This method should be used to look up the enclosing {@code TryFinally} inside
     * {@link Item#writeCode(CodeBuilder, BlockCreatorImpl, StackMapBuilder)}. The {@code tryFinally} field is set late
     * (when the {@code finally} block is being added), so all blocks within the {@code try} body,
     * except of the outermost one, do <em>not</em> have it set correctly.
     */
    TryFinally tryFinally() {
        BlockCreatorImpl current = this;
        while (current != null) {
            if (current.tryFinally != null) {
                return current.tryFinally;
            }
            current = current.parent;
        }
        return null;
    }

    Label newLabel() {
        return outerCodeBuilder.newLabel();
    }

    ClassDesc returnType() {
        return returnType;
    }

    public void breakTarget() {
        breakTarget = true;
    }

    public void branchTarget() {
        branchTarget = true;
    }

    protected void computeType() {
        initType(outputType);
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
        return breakTarget || getLast().mayFallThrough();
    }

    public void pop(final ListIterator<Item> itr) {
        if (isVoid()) {
            super.pop(itr);
            return;
        }
        assert mayFallThrough();
        // else, pop *our* result
        if (breakTarget) {
            // need an explicit pop node
            super.pop(itr);
            return;
        }
        Item tailItem = getLast();
        if (tailItem instanceof Yield yield && !yield.value().isVoid()) {
            ListIterator<Item> subItr = iterator();
            tailItem.revoke(subItr); // remove the old yield
            addItemUnchecked(Yield.YIELD_VOID, subItr); // add a new one
        } else {
            super.pop(itr);
        }
    }

    private void markDone() {
        state = ST_DONE;
        finishSite = Util.trackCaller();
    }

    public boolean isContainedBy(final BlockCreator other) {
        return this == other || parent != null && parent.isContainedBy(other);
    }

    public LocalVar localVar(final String name, final ClassDesc type, final Expr value) {
        return localVar0(new LocalVarImpl(this, name, type, null), value);
    }

    public LocalVar localVar(final String name, final GenericType type, final Expr value) {
        return localVar0(new LocalVarImpl(this, name, null, type), value);
    }

    private LocalVar localVar0(LocalVarImpl lv, Expr value) {
        addItem(lv.allocator());
        set(lv, value);
        return lv;
    }

    public Expr get(final Assignable var, final MemoryOrder mode) {
        return addItem(((AssignableImpl) var).emitGet(this, mode));
    }

    public void set(final Assignable var, final Expr value, final MemoryOrder mode) {
        Item newValue = convert(value, var.type());
        addItem(((AssignableImpl) var).emitSet(this, newValue, mode));
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

    public Expr box(final Expr a) {
        if (Conversions.isPrimitiveWrapper(a.type())) {
            return a;
        }
        return addItem(new Box(a));
    }

    public Expr unbox(final Expr a) {
        if (Conversions.isPrimitive(a.type())) {
            return a;
        }
        return addItem(new Unbox(a));
    }

    public Expr switchEnum(final ClassDesc outputType, final Expr enumExpr, final Consumer<SwitchCreator> builder) {
        EnumSwitchCreatorImpl sci = new EnumSwitchCreatorImpl(this, enumExpr, outputType);
        sci.accept(builder);
        addItem(sci);
        return sci;
    }

    public Expr switch_(final ClassDesc outputType, final Expr expr, final Consumer<SwitchCreator> builder) {
        SwitchCreatorImpl<? extends ConstImpl> sci = switch (expr.type().descriptorString()) {
            case "I", "S", "B", "Z", "C" -> new IntSwitchCreatorImpl(this, expr, outputType);
            case "J" -> new LongSwitchCreatorImpl(this, expr, outputType);
            case "Ljava/lang/String;" -> new StringSwitchCreatorImpl(this, expr, outputType);
            case "Ljava/lang/Class;" -> new ClassSwitchCreatorImpl(this, expr, outputType);
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
        return invokeInterface(MD_Iterable.iterator, items);
    }

    public Expr currentThread() {
        return invokeStatic(MD_Thread.currentThread);
    }

    public void close(final Expr closeable) {
        invokeInterface(MD_AutoCloseable.close, closeable);
    }

    public void inc(final Assignable var, Const amount) {
        ((AssignableImpl) var).emitInc(this, amount);
    }

    public void dec(final Assignable var, Const amount) {
        ((AssignableImpl) var).emitDec(this, amount);
    }

    public Expr compareAndExchange(final Assignable var, final Expr expected, final Expr update, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitCompareAndExchange(this, (Item) expected, (Item) update, order));
    }

    public Expr getAndSet(final Assignable var, final Expr newValue, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitReadModifyWrite(this, "Set", (Item) newValue, order));
    }

    public Expr getAndAdd(final Assignable var, final Expr amount, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitReadModifyWrite(this, "Add", (Item) amount, order));
    }

    public Expr getAndBitwiseOr(final Assignable var, final Expr other, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitReadModifyWrite(this, "BitwiseOr", (Item) other, order));
    }

    public Expr getAndBitwiseAnd(final Assignable var, final Expr other, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitReadModifyWrite(this, "BitwiseAnd", (Item) other, order));
    }

    public Expr getAndBitwiseXor(final Assignable var, final Expr other, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitReadModifyWrite(this, "BitwiseXor", (Item) other, order));
    }

    public Expr compareAndSet(final Assignable var, final Expr expected, final Expr update) {
        return addItem(
                ((AssignableImpl) var).emitCompareAndSet(this, (Item) expected, (Item) update, false, MemoryOrder.Volatile));
    }

    public Expr weakCompareAndSet(final Assignable var, final Expr expected, final Expr update, final MemoryOrder order) {
        return addItem(((AssignableImpl) var).emitCompareAndSet(this, (Item) expected, (Item) update, true, order));
    }

    public Expr newEmptyArray(final ClassDesc componentType, final Expr size) {
        return addItem(new NewEmptyArray(componentType, (Item) size));
    }

    @Override
    public <T> Expr newArray(final ClassDesc componentType, final List<T> values, final Function<T, ? extends Expr> mapper) {
        checkActive();
        // build the object graph
        int size = values.size();
        NewEmptyArray nea = new NewEmptyArray(componentType, ConstImpl.of(size));
        if (size == 0) {
            return addItem(nea);
        }
        // make the stores list
        List<ArrayStore> stores = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Expr mapped = mapper.apply(values.get(i));
            stores.add(new ArrayStore(new Dup(nea), ConstImpl.of(i), (Item) mapped, componentType));
        }
        // stitch the object graph into our list
        ListIterator<Item> itr = iterator();
        // start at end
        NewArrayResult result = new NewArrayResult(nea, Util.reinterpretCast(stores));
        result.insert(itr);
        // reverse order stores and dups
        for (int i = size - 1; i >= 0; i--) {
            ArrayStore store = stores.get(i);
            store.insert(itr);
            // now the value to be stored
            store.value().insertIfUnbound(itr);
            // now the index
            store.index().insert(itr);
            // now the Dup
            store.arrayExpr().insert(itr);
        }
        // last, add the empty array
        nea.insert(itr);
        ((Item) nea.length()).insert(itr);
        return result;
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
            default -> throw impossibleSwitchCase(a.typeKind().asLoadable());
        }
    }

    private Expr rel(Expr a, Expr b, final If.Kind kind) {
        ClassDesc operandType = a.type();
        Optional<ClassDesc> promotedType = numericPromotionRequired(kind, a.type(), b.type())
                ? numericPromotion(a.type(), b.type())
                : Optional.empty();
        if (promotedType.isPresent()) {
            operandType = promotedType.get();
            a = convert(a, operandType);
            b = convert(b, operandType);
        }
        TypeKind typeKind = TypeKind.from(operandType).asLoadable();

        switch (typeKind) {
            case INT -> {
                // normal rel
                if (a instanceof IntConst ac && ac.intValue() == 0) {
                    boolean shouldNotInvert = kind == If.Kind.EQ || kind == If.Kind.NE;
                    return relZero(b, shouldNotInvert ? kind : kind.invert());
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
            default -> throw impossibleSwitchCase(typeKind);
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
        if (owner.gizmo.lambdasAsAnonymousClasses()) {
            return newAnonymousClass(samOwner, acc -> {
                acc.method(sam, imc -> {
                    builder.accept(new LambdaAsAnonClassCreatorImpl(
                            (AnonymousClassCreatorImpl) acc, (InstanceMethodCreatorImpl) imc));
                });
            });
        }

        // certain versions of GraalVM native image cannot handle our custom translation strategy of lambdas
        // see: https://github.com/quarkusio/quarkus/issues/49346
        // we'll need to handle it better, but for now, let's just use the "classic" translation strategy always
        // this code block shall be removed in the future
        if (true) {
            return lambdaDebug(sam, samOwner, builder);
        }

        if (Util.debug) {
            return lambdaDebug(sam, samOwner, builder);
        }

        ClassDesc ownerDesc = owner.type();
        String ds = ownerDesc.descriptorString();
        ClassDesc desc = ClassDesc.ofDescriptor(ds.substring(0, ds.length() - 1) + "$lambda;");
        ClassFile cf = owner.gizmo.createClassFile();
        final ArrayList<Expr> captureExprs = new ArrayList<>();
        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(owner.version().major(), 0);
            AnonymousClassCreatorImpl tc = new AnonymousClassCreatorImpl(owner.gizmo, desc, owner.output(), zb,
                    ConstructorDesc.of(Object.class), captureExprs);
            if (sam instanceof InterfaceMethodDesc) {
                // implement the interface too
                tc.implements_(sam.owner());
            }
            tc.method(sam, imc -> {
                imc.public_();
                LambdaAsAnonClassCreatorImpl lc = new LambdaAsAnonClassCreatorImpl(tc, (InstanceMethodCreatorImpl) imc);
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
        String encoded = Base64.getUrlEncoder().encodeToString(bytes);
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

    private Expr lambdaDebug(MethodDesc sam, ClassDesc samOwner, Consumer<LambdaCreator> builder) {
        // TODO serializable lambdas not (yet) supported
        MethodTypeDesc samType = sam.type();
        String name = "lambda$" + methodNameForLambdas + "$" + owner.lambdaAndAnonClassCounter++;
        List<Expr> captures = new ArrayList<>();
        // always generating `static` methods is fine, because users have to capture `this` explicitly
        MethodDesc lambdaMethod = owner.staticMethod(name, mc -> {
            mc.private_();
            mc.synthetic();
            mc.returning(samType.returnType());
            builder.accept(new LambdaAsMethodCreatorImpl(samOwner, samType, (MethodCreatorImpl) mc, captures));
        });
        return invokeDynamic(DynamicCallSiteDesc.of(
                ConstantDescs.ofCallsiteBootstrap(CD_LambdaMetafactory, "metafactory",
                        CD_CallSite, CD_MethodType, CD_MethodHandle, CD_MethodType),
                sam.name(),
                MethodTypeDesc.of(samOwner, captures.stream().map(Expr::type).toArray(ClassDesc[]::new)),
                samType,
                MethodHandleDesc.ofMethod(DirectMethodHandleDesc.Kind.STATIC, lambdaMethod.owner(), lambdaMethod.name(),
                        lambdaMethod.type()),
                samType), captures);
    }

    public Expr newAnonymousClass(final ConstructorDesc superCtor, final List<? extends Expr> args,
            final Consumer<AnonymousClassCreator> builder) {
        ClassDesc ownerDesc = owner.type();
        int idx = owner.lambdaAndAnonClassCounter++;
        String ds = ownerDesc.descriptorString();
        ClassDesc desc = ClassDesc.ofDescriptor(ds.substring(0, ds.length() - 1) + "$" + idx + ";");
        ClassFile cf = owner.gizmo.createClassFile();
        final ArrayList<Expr> captureExprs = new ArrayList<>();

        byte[] bytes = cf.build(desc, zb -> {
            zb.withVersion(owner.version().major(), 0);
            zb.with(NestHostAttribute.of(ownerDesc));
            zb.with(InnerClassesAttribute.of(
                    InnerClassInfo.of(desc, Optional.of(ownerDesc), Optional.empty(), 0)));
            AnonymousClassCreatorImpl tc = new AnonymousClassCreatorImpl(owner.gizmo, desc, owner.output(), zb, superCtor,
                    captureExprs);
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
        owner.addNestMember(desc);
        return new_(ConstructorDesc.of(desc, ourCtor.methodTypeSymbol()),
                Stream.concat(args.stream(), captureExprs.stream()).toList());
    }

    public Expr cast(final Expr a, final GenericType toGenType) {
        ClassDesc toType = toGenType.desc();
        if (a.type().isPrimitive()) {
            if (toType.isPrimitive()) {
                return addItem(new PrimitiveCast(a, toGenType.desc()));
            } else if (Util.equals(toType, boxingConversion(a.type()).orElse(null))) {
                return box(a);
            } else {
                throw new IllegalArgumentException("Cannot cast primitive value of type '" + a.type().displayName()
                        + "' to object type '" + toType.displayName() + "'");
            }
        } else {
            if (Util.equals(toType, unboxingConversion(a.type()).orElse(null))) {
                return unbox(a);
            } else if (toType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot cast object value of type '" + a.type().displayName()
                        + "' to primitive type '" + toType.displayName() + "'");
            } else {
                return addItem(new CheckCast(a, null, toGenType));
            }
        }
    }

    public Expr cast(final Expr a, final ClassDesc toType) {
        if (Util.equals(a.type(), toType)) {
            return a;
        } else if (a.type().isPrimitive()) {
            if (toType.isPrimitive()) {
                return addItem(new PrimitiveCast(a, toType));
            } else if (Util.equals(toType, boxingConversion(a.type()).orElse(null))) {
                return box(a);
            } else {
                throw new IllegalArgumentException("Cannot cast primitive value of type '" + a.type().displayName()
                        + "' to object type '" + toType.displayName() + "'");
            }
        } else {
            if (Util.equals(toType, unboxingConversion(a.type()).orElse(null))) {
                return unbox(a);
            } else if (toType.isPrimitive()) {
                throw new IllegalArgumentException("Cannot cast object value of type '" + a.type().displayName()
                        + "' to primitive type '" + toType.displayName() + "'");
            } else {
                return addItem(new CheckCast(a, toType, null));
            }
        }
    }

    public Expr uncheckedCast(final Expr a, final ClassDesc toType) {
        if (Util.equals(a.type(), toType)) {
            return a;
        } else if (a.type().isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive value: " + a.type().displayName());
        }
        if (toType.isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive type: " + toType.displayName());
        }
        return addItem(new UncheckedCast(a, toType, null));
    }

    public Expr uncheckedCast(final Expr a, final GenericType toType) {
        if (a.type().isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive value: " + a.type().displayName());
        }
        if (toType.desc().isPrimitive()) {
            throw new IllegalArgumentException("Cannot apply unchecked cast to primitive type: " + toType.desc().displayName());
        }
        return addItem(new UncheckedCast(a, null, toType));
    }

    public Expr instanceOf(final Expr obj, final ClassDesc type) {
        Assert.checkNotNullParam("type", type);
        return addItem(new InstanceOf(obj, type, null));
    }

    public Expr instanceOf(final Expr obj, final GenericType type) {
        Assert.checkNotNullParam("type", type);
        return addItem(new InstanceOf(obj, null, type));
    }

    public Expr new_(final GenericType genericType, final ConstructorDesc ctor, final List<? extends Expr> args) {
        Assert.checkNotNullParam("genericType", genericType);
        Assert.checkNotNullParam("ctor", ctor);
        Assert.checkNotNullParam("args", args);
        checkActive();
        if (!Util.equals(ctor.owner(), genericType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match constructor type %s".formatted(genericType, ctor.owner()));
        }
        return new0(genericType, ctor, args);
    }

    public Expr new_(final ConstructorDesc ctor, final List<? extends Expr> args) {
        Assert.checkNotNullParam("ctor", ctor);
        Assert.checkNotNullParam("args", args);
        return new0(null, ctor, args);
    }

    private NewResult new0(final GenericType genericType, final ConstructorDesc ctor, final List<? extends Expr> args) {
        New new_ = new New(ctor.owner(), genericType);
        Dup dup_ = new Dup(new_);
        ListIterator<Item> itr = iterator();
        // insert New & Dup *before* the arguments
        for (int i = args.size() - 1; i >= 0; i--) {
            Item arg = (Item) args.get(i);
            if (arg.bound()) {
                arg.verify(itr);
            }
        }
        dup_.insert(itr);
        new_.insert(itr);
        // add the invoke at tail
        Invoke invoke = new Invoke(ctor, dup_, args, genericType);
        addItem(invoke);
        // finally, add the result
        return addItem(new NewResult(new_, invoke));
    }

    public Expr invokeStatic(final GenericType genericReturnType, final MethodDesc method, final List<? extends Expr> args) {
        if (!Util.equals(method.returnType(), genericReturnType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match method return type %s".formatted(genericReturnType, method.returnType()));
        }
        return addItem(new Invoke(Opcode.INVOKESTATIC, method, null, args, genericReturnType));
    }

    public Expr invokeStatic(final MethodDesc method, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESTATIC, method, null, args, null));
    }

    public Expr invokeVirtual(final GenericType genericReturnType, final MethodDesc method, final Expr instance,
            final List<? extends Expr> args) {
        if (!Util.equals(method.returnType(), genericReturnType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match method return type %s".formatted(genericReturnType, method.returnType()));
        }
        return addItem(new Invoke(Opcode.INVOKEVIRTUAL, method, instance, args, genericReturnType));
    }

    public Expr invokeVirtual(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKEVIRTUAL, method, instance, args, null));
    }

    public Expr invokeSpecial(final GenericType genericReturnType, final MethodDesc method, final Expr instance,
            final List<? extends Expr> args) {
        if (!Util.equals(method.returnType(), genericReturnType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match method return type %s".formatted(genericReturnType, method.returnType()));
        }
        return addItem(new Invoke(Opcode.INVOKESPECIAL, method, instance, args, genericReturnType));
    }

    public Expr invokeSpecial(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        return addItem(new Invoke(Opcode.INVOKESPECIAL, method, instance, args, null));
    }

    public Expr invokeSpecial(final ConstructorDesc ctor, final Expr instance, final List<? extends Expr> args) {
        Invoke invoke = new Invoke(ctor, instance, args, GenericTypes.GT_void);
        addItem(invoke);
        if (instance instanceof ThisExpr) {
            // self-init
            for (Consumer<BlockCreator> postInit : postInits) {
                postInit.accept(this);
            }
        }
        return invoke;
    }

    public Expr invokeInterface(final GenericType genericReturnType, final MethodDesc method, final Expr instance,
            final List<? extends Expr> args) {
        if (!(method instanceof InterfaceMethodDesc)) {
            throw new IllegalArgumentException("Cannot emit `invokeinterface` for " + method + "; must be InterfaceMethodDesc");
        }
        if (!Util.equals(method.returnType(), genericReturnType.desc())) {
            throw new IllegalArgumentException(
                    "Generic type %s does not match method return type %s".formatted(genericReturnType, method.returnType()));
        }
        return addItem(new Invoke(Opcode.INVOKEINTERFACE, method, instance, args, genericReturnType));
    }

    public Expr invokeInterface(final MethodDesc method, final Expr instance, final List<? extends Expr> args) {
        if (!(method instanceof InterfaceMethodDesc)) {
            throw new IllegalArgumentException("Cannot emit `invokeinterface` for " + method + "; must be InterfaceMethodDesc");
        }
        return addItem(new Invoke(Opcode.INVOKEINTERFACE, method, instance, args, null));
    }

    public Expr invokeDynamic(final DynamicCallSiteDesc callSiteDesc, final List<? extends Expr> args) {
        return addItem(new InvokeDynamic(args, callSiteDesc));
    }

    public void forEach(final Expr fn, final BiConsumer<BlockCreator, ? super LocalVar> builder) {
        block(fn, (b0, fn0) -> {
            Var items = b0.localVar("$$items" + depth, fn0);
            if (items.type().isArray()) {
                // iterate array
                Expr lv = items.length();
                Expr length = lv instanceof Const ? lv : b0.localVar("$$length" + depth, lv);
                LocalVar idx = b0.localVar("$$idx" + depth, Const.of(0));
                b0.block(b1 -> {
                    b1.if_(b1.lt(idx, length), b2 -> {
                        LocalVar val = b2.localVar("$$val" + depth, items.elem(idx));
                        builder.accept(b2, val);
                        if (b2.active()) {
                            b2.inc(idx);
                            b2.goto_(b1);
                        }
                    });
                });
            } else {
                // use iterable
                LocalVar itr = b0.localVar("$$itr" + depth, b0.iterate(items));
                b0.block(b1 -> {
                    b1.if_(b1.withIterator(itr).hasNext(), b2 -> {
                        LocalVar val = b2.localVar("$$val" + depth, b2.withIterator(itr).next());
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
        nesting(() -> {
            block.accept(nested);
        });
        addItem(block);
    }

    public void block(final Consumer<BlockCreator> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this);
        nesting(() -> {
            block.accept(nested);
        });
        addItem(block);
    }

    public Expr blockExpr(final ClassDesc type, final Consumer<BlockCreator> nested) {
        checkActive();
        BlockCreatorImpl block = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        nesting(() -> {
            block.accept(nested);
        });
        addItem(block);
        return block;
    }

    public void accept(final BiConsumer<? super BlockCreatorImpl, Expr> handler) {
        checkActive();
        Expr input = getFirst();
        handler.accept(this, input);
        finish();
    }

    public void accept(final Consumer<? super BlockCreatorImpl> handler) {
        checkActive();
        handler.accept(this);
        finish();
    }

    private void finish() {
        if (!done()) {
            addItem(Yield.YIELD_VOID);
        }
        ListIterator<Item> itr = iterator();
        Item last = Util.peekPrevious(itr);
        if (last instanceof Yield yield) {
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
        last.verify(itr);
        // clean stack with fresh iterator
        cleanStack(iterator());
        markDone();
    }

    public void ifInstanceOf(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, ? super LocalVar> ifTrue) {
        doIf(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.localVar("$$instance" + depth, bc.cast(obj, type))), null);
    }

    public void ifNotInstanceOf(Expr obj, ClassDesc type, Consumer<BlockCreator> ifFalse) {
        doIf(instanceOf(obj, type), null, ifFalse);
    }

    public void ifInstanceOfElse(final Expr obj, final ClassDesc type, final BiConsumer<BlockCreator, ? super LocalVar> ifTrue,
            final Consumer<BlockCreator> ifFalse) {
        doIf(instanceOf(obj, type), bc -> ifTrue.accept(bc, bc.localVar("$$instance" + depth, bc.cast(obj, type))), ifFalse);
    }

    private If doIfInsn(final ClassDesc type, final Expr cond, final BlockCreatorImpl wt, final BlockCreatorImpl wf) {
        // try to combine the condition into the `if`
        if (((Item) cond).bound()) {
            ListIterator<Item> itr = iterator();
            Item prevItem = Util.peekPrevious(itr);
            if (prevItem == cond) {
                if (cond instanceof Rel rel) {
                    IfRel ifRel = new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right());
                    itr.set(ifRel);
                    if (!ifRel.mayFallThrough()) {
                        markDone();
                    }
                    return ifRel;
                } else if (cond instanceof RelZero rz) {
                    IfZero ifZero = new IfZero(type, rz.kind(), wt, wf, rz.input(), false);
                    itr.set(ifZero);
                    if (!ifZero.mayFallThrough()) {
                        markDone();
                    }
                    return ifZero;
                }
            }
            // failed
        } else {
            if (cond instanceof Rel rel) {
                return addItem(new IfRel(type, rel.kind(), wt, wf, rel.left(), rel.right()));
            } else if (cond instanceof RelZero rz) {
                return addItem(new IfZero(type, rz.kind(), wt, wf, rz.input(), false));
            }
            // failed
        }
        return addItem(new IfZero(type, If.Kind.NE, wt, wf, (Item) cond, true));
    }

    private void doIf(final Expr cond, final Consumer<BlockCreator> whenTrue, final Consumer<BlockCreator> whenFalse) {
        BlockCreatorImpl wt = whenTrue == null ? null : new BlockCreatorImpl(this);
        BlockCreatorImpl wf = whenFalse == null ? null : new BlockCreatorImpl(this);
        nesting(() -> {
            if (wt != null) {
                wt.accept(whenTrue);
            }
            if (wf != null) {
                wf.accept(whenFalse);
            }
        });
        doIfInsn(CD_void, cond, wt, wf);
    }

    public Expr cond(final ClassDesc type, final Expr cond, final Consumer<BlockCreator> whenTrue,
            final Consumer<BlockCreator> whenFalse) {
        BlockCreatorImpl wt = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        BlockCreatorImpl wf = new BlockCreatorImpl(this, ConstImpl.ofVoid(), type);
        nesting(() -> {
            wt.accept(whenTrue);
            wf.accept(whenFalse);
        });
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
        ((BlockCreatorImpl) outer).branchTarget = true;
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
                LocalVar rsrc = b0.localVar("$$resource" + depth, opened);
                autoClose(rsrc, b1 -> {
                    body.accept(b1, rsrc);
                });
            });
        }
    }

    public void autoClose(final Var resource, final Consumer<BlockCreator> body) {
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
        addItem(new MonitorEnter(monitor));
    }

    void monitorExit(final Item monitor) {
        addItem(new MonitorExit(monitor));
    }

    public void synchronized_(final Expr monitor, final Consumer<BlockCreator> body) {
        block(monitor, (b0, mon) -> {
            LocalVar mv = b0.localVar("$$monitor" + depth, mon);
            ((BlockCreatorImpl) b0).monitorEnter((Item) mv);
            b0.try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> ((BlockCreatorImpl) b2).monitorExit((Item) mv));
            });
        });
    }

    public void locked(final Expr jucLock, final Consumer<BlockCreator> body) {
        block(jucLock, (b0, lock) -> {
            LocalVar lv = b0.localVar("$$lock" + depth, lock);
            b0.invokeInterface(MD_Lock.lock, lv);
            b0.try_(t1 -> {
                t1.body(body);
                t1.finally_(b2 -> b2.invokeInterface(MD_Lock.unlock, lv));
            });
        });
    }

    public void returnNull() {
        return_(ConstImpl.ofNull(returnType));
    }

    public void return_() {
        addItem(Return.RETURN_VOID);
    }

    public void return_(Expr val) {
        if (Util.isVoid(returnType) && !val.isVoid()) {
            // Gizmo 1 automatically dropped the provided value in this case
            // let's at least provide a proper error message
            throw new IllegalArgumentException("Attempted to return a value from a `void`-returning method");
        }

        val = convert(val, returnType);
        addItem(val.equals(Const.ofVoid()) ? Return.RETURN_VOID : new Return(val));
    }

    public void throw_(final Expr val) {
        addItem(new Throw(val));
    }

    public void yield(Expr val) {
        val = convert(val, outputType);
        addItem(val.equals(Const.ofVoid()) ? Yield.YIELD_VOID : new Yield(val));
    }

    public Expr exprHashCode(final Expr expr) {
        return switch (expr.typeKind()) {
            case BOOLEAN -> invokeStatic(MD_Boolean.hashCode, expr);
            case BYTE -> invokeStatic(MD_Byte.hashCode, expr);
            case SHORT -> invokeStatic(MD_Short.hashCode, expr);
            case CHAR -> invokeStatic(MD_Character.hashCode, expr);
            case INT -> invokeStatic(MD_Integer.hashCode, expr);
            case LONG -> invokeStatic(MD_Long.hashCode, expr);
            case FLOAT -> invokeStatic(MD_Float.hashCode, expr);
            case DOUBLE -> invokeStatic(MD_Double.hashCode, expr);
            case REFERENCE -> {
                if (expr instanceof ConstImpl c && c.isNonZero()) {
                    yield invokeVirtual(MD_Object.hashCode, c);
                }
                yield invokeStatic(MD_Objects.hashCode, expr);
            }
            case VOID -> Const.of(0); // null constant
        };
    }

    public Expr exprEquals(final Expr a, final Expr b) {
        return switch (a.typeKind()) {
            case REFERENCE -> switch (b.typeKind()) {
                case REFERENCE -> {
                    if (a instanceof ConstImpl c && c.isNonZero()) {
                        yield invokeVirtual(MD_Object.equals, c, b);
                    }
                    if (b instanceof ConstImpl c && c.isNonZero()) {
                        yield invokeVirtual(MD_Object.equals, c, a);
                    }
                    yield invokeStatic(MD_Objects.equals, a, b);
                }
                default -> exprEquals(a, box(b));
            };
            default -> switch (b.typeKind()) {
                case REFERENCE -> exprEquals(box(a), b);
                default -> eq(a, b);
            };
        };
    }

    public Expr exprToString(final Expr expr) {
        if (expr.typeKind() == TypeKind.REFERENCE && expr instanceof ConstImpl c && c.isNonZero()) {
            return invokeVirtual(MD_Object.toString, c);
        }
        return invokeStatic(MD_String.valueOf(expr.type()), expr);
    }

    public Expr arrayHashCode(final Expr expr) {
        requireArray(expr);

        ClassDesc componentType = expr.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MD_Arrays.deepHashCode, expr);
        } else {
            return invokeStatic(MD_Arrays.hashCode(componentType), expr);
        }
    }

    public Expr arrayEquals(final Expr a, final Expr b) {
        requireArray(a);
        requireArray(b);
        requireSameTypeKind(a.type().componentType(), b.type().componentType());

        ClassDesc componentType = a.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MD_Arrays.deepEquals, a, b);
        } else {
            return invokeStatic(MD_Arrays.equals(componentType), a, b);
        }
    }

    public Expr arrayToString(final Expr expr) {
        requireArray(expr);

        ClassDesc componentType = expr.type().componentType();
        if (componentType.isArray()) {
            return invokeStatic(MD_Arrays.deepToString, expr);
        } else {
            return invokeStatic(MD_Arrays.toString(componentType), expr);
        }
    }

    public Expr classForName(final Expr className) {
        return invokeStatic(MD_Class.forName, className);
    }

    @Override
    public <T> Expr listOf(final List<T> items, final Function<T, ? extends Expr> mapper) {
        List<Expr> exprs = new ArrayList<>();
        for (T item : items) {
            exprs.add(mapper.apply(item));
        }

        int size = exprs.size();
        if (size <= 10) {
            return invokeStatic(MD_List.of_n(size), exprs);
        } else {
            return invokeStatic(MD_List.of_array, newArray(Object.class, exprs));
        }
    }

    @Override
    public <T> Expr setOf(final List<T> items, final Function<T, ? extends Expr> mapper) {
        List<Expr> exprs = new ArrayList<>();
        for (T item : items) {
            exprs.add(mapper.apply(item));
        }

        int size = exprs.size();
        if (size <= 10) {
            return invokeStatic(MD_Set.of_n(size), exprs);
        } else {
            return invokeStatic(MD_Set.of_array, newArray(Object.class, exprs));
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
            return invokeStatic(MD_Map.of_n(size >> 1), items);
        } else {
            throw new UnsupportedOperationException("Maps with more than 10 entries are not supported");
        }
    }

    public Expr mapEntry(final Expr key, final Expr value) {
        return invokeStatic(MD_Map.entry, key, value);
    }

    @Override
    public Expr optionalOf(Expr value) {
        return invokeStatic(MD_Optional.of, value);
    }

    @Override
    public Expr optionalOfNullable(Expr value) {
        return invokeStatic(MD_Optional.ofNullable, value);
    }

    @Override
    public Expr optionalEmpty() {
        return invokeStatic(MD_Optional.empty);
    }

    public void line(final int lineNumber) {
        addItem(new LineNumber(lineNumber));
    }

    public void printf(final String format, final List<? extends Expr> values) {
        invokeVirtual(
                MD_PrintStream.printf,
                Expr.staticField(FD_System.out),
                Const.of(format),
                newArray(CD_Object, values));
    }

    public void assert_(final Consumer<BlockCreator> assertion, final String message) {
        if_(Const.ofInvoke(Const.ofMethodHandle(InvokeKind.VIRTUAL, MD_Class.desiredAssertionStatus),
                Const.of(owner.type())),
                b0 -> b0.ifNot(b0.blockExpr(CD_boolean, assertion),
                        b1 -> b1.throw_(b1.new_(ConstructorDesc.of(AssertionError.class, Object.class), Const.of(message)))));
    }

    protected void forEachDependency(final ListIterator<Item> itr, final BiConsumer<Item, ListIterator<Item>> op) {
        input.process(itr, op);
    }

    public void writeCode(CodeBuilder cb, final BlockCreatorImpl block, final StackMapBuilder smb) {
        StackMapBuilder.Saved saved = smb.save();
        if (branchTarget) {
            smb.addFrameInfo(cb);
        }
        cb.block(bcb -> {
            bcb.labelBinding(startLabel);
            List<Item> items = this.items;
            int sz = items.size();
            for (int i = 0; i < sz; i++) {
                items.get(i).writeCode(bcb, this, smb);
            }
            bcb.labelBinding(endLabel);
        });
        smb.restore(saved);
        if (!Util.isVoid(input.type())) {
            // consume argument
            smb.pop();
        }
        if (!Util.isVoid(outputType)) {
            // produce output type
            smb.push(outputType);
        }
        if (breakTarget) {
            smb.addFrameInfo(cb);
        }
    }

    public void writeAnnotations(final RetentionPolicy retention, final ArrayList<TypeAnnotation> annotations) {
        List<Item> items = this.items;
        int sz = items.size();
        for (int i = 0; i < sz; i++) {
            items.get(i).writeAnnotations(retention, annotations);
        }
    }

    // non-public

    void postInit(final List<Consumer<BlockCreator>> postInits) {
        this.postInits = postInits;
    }

    <I extends Item> I addItem(I item) {
        checkActive();
        return addItemUnchecked(item, iterator());
    }

    private <I extends Item> I addItemUnchecked(final I item, final ListIterator<Item> itr) {
        item.insert(itr);
        item.forEachDependency(itr, Item::insertIfUnbound);
        if (!item.mayFallThrough() || item instanceof Yield) {
            markDone();
        }
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

    static void cleanStack(ListIterator<Item> itr) {
        // clean the block stack before the current iterator position
        while (itr.hasPrevious()) {
            // pop every unused item in the list, skipping void nodes
            Util.peekPrevious(itr).pop(itr);
        }
    }

    void nesting(Runnable action) {
        checkActive();
        state = ST_NESTED;
        nestSite = Util.trackCaller();
        try {
            action.run();
        } finally {
            state = ST_ACTIVE;
            nestSite = null;
        }
    }

    ListIterator<Item> iterator() {
        return items.listIterator(items.size());
    }

    protected void insert(final ListIterator<Item> itr) {
        if (items.size() == 2) {
            Item last = items.get(1);
            if (Util.equals(last.type(), input.type())) {
                last.insert(itr);
                return;
            }
        }
        super.insert(itr);
    }

    Item getFirst() {
        return items.get(0);
    }

    Item getLast() {
        return items.get(items.size() - 1);
    }
}
