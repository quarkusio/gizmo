package io.quarkus.gizmo2.creator;

import static java.lang.constant.ConstantDescs.CD_boolean;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.ConstructorDesc;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LValueExpr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A code block.
 */
public sealed interface BlockCreator permits BlockCreatorImpl {

    // general state

    /**
     * {@return true if the current block is activated (in scope), or false if it is not}
     * Blocks which are not active may not have operations added to them.
     * Blocks are inactive when a nested scope is active or when the block is terminated.
     */
    boolean active();

    /**
     * {@return true if the block is done, or false if it is not}
     */
    boolean done();

    boolean isContainedBy(BlockCreator other);

    default boolean contains(BlockCreator other) {
        return other.isContainedBy(this);
    }

    default boolean contains(LocalVar var) {
        return contains(var.block());
    }

    // lexical variables

    LocalVar declare(String name, ClassDesc type);

    default LocalVar declare(String name, Class<?> type) {
        return declare(name, Util.classDesc(type));
    }

    default LocalVar define(String name, Expr value) {
        LocalVar var = declare(name, value.type());
        set(var, value);
        return var;
    }

    default LocalVar define(Var original) {
        return define(original.name(), original);
    }


    // reading memory

    Expr get(LValueExpr var, AccessMode mode);

    default Expr get(LValueExpr var) {
        return get(var, AccessMode.AsDeclared);
    }


    // writing memory

    LValueExpr set(LValueExpr var, Expr value, AccessMode mode);

    default LValueExpr set(LValueExpr var, Expr value) {
        return set(var, value, AccessMode.AsDeclared);
    }

    default LValueExpr set(LValueExpr var, Constant value) {
        return set(var, (Expr) value);
    }

    default LValueExpr set(LValueExpr var, ConstantDesc value) {
        return set(var, Constant.of(value));
    }

    default LValueExpr set(LValueExpr var, Constable value) {
        return set(var, Constant.of(value));
    }

    default LValueExpr set(LValueExpr var, String value) {
        return set(var, Constant.of(value));
    }

    default LValueExpr set(LValueExpr var, int value) {
        return set(var, Constant.of(value));
    }

    default LValueExpr set(LValueExpr var, long value) {
        return set(var, Constant.of(value));
    }


    // increment/decrement

    /**
     * Post-increment the given value non-atomically by one.
     *
     * @param var the variable to modify (must not be {@code null})
     * @return the value before incrementing (not {@code null})
     */
    Expr postInc(LValueExpr var);

    Expr preInc(LValueExpr var);

    default void inc(LValueExpr var) {
        inc(var, Constant.of(1, var.typeKind()));
    }

    void inc(LValueExpr var, Constant amount);

    Expr postDec(LValueExpr var);

    Expr preDec(LValueExpr var);

    default void dec(LValueExpr var) {
        dec(var, Constant.of(1, var.typeKind()));
    }

    void dec(LValueExpr var, Constant amount);

    // arrays

    Expr newArray(ClassDesc elemType, Expr size);

    default Expr newArray(Class<?> elemType, Expr size) {
        return newArray(Util.classDesc(elemType), size);
    }

    Expr newArray(ClassDesc elementType, List<Expr> values);

    default Expr newArray(Class<?> elementType, List<Expr> values) {
        Expr array = newArray(elementType, Constant.of(values.size()));
        for (int i = 0; i < values.size(); i++) {
            set(array.elem(i), values.get(i));
        }
        return array;
    }


    // relational ops

    /**
     * The {@code ==} operator.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument (must not be {@code null})
     * @return the boolean result expression
     */
    Expr eq(Expr a, Expr b);

    default Expr eq(Expr a, int b) {
        return eq(a, Constant.of(b, a.typeKind()));
    }

    default Expr eq(Expr a, long b) {
        return eq(a, Constant.of(b, a.typeKind()));
    }

    default Expr eq(Expr a, float b) {
        return eq(a, Constant.of(b, a.typeKind()));
    }

    default Expr eq(Expr a, double b) {
        return eq(a, Constant.of(b, a.typeKind()));
    }

    Expr ne(Expr a, Expr b);

    default Expr ne(Expr a, int b) {
        return ne(a, Constant.of(b, a.typeKind()));
    }

    default Expr ne(Expr a, long b) {
        return ne(a, Constant.of(b, a.typeKind()));
    }

    default Expr ne(Expr a, float b) {
        return ne(a, Constant.of(b, a.typeKind()));
    }

    default Expr ne(Expr a, double b) {
        return ne(a, Constant.of(b, a.typeKind()));
    }

    Expr lt(Expr a, Expr b);

    default Expr lt(Expr a, int b) {
        return lt(a, Constant.of(b, a.typeKind()));
    }

    default Expr lt(Expr a, long b) {
        return lt(a, Constant.of(b, a.typeKind()));
    }

    default Expr lt(Expr a, float b) {
        return lt(a, Constant.of(b, a.typeKind()));
    }

    default Expr lt(Expr a, double b) {
        return lt(a, Constant.of(b, a.typeKind()));
    }

    Expr gt(Expr a, Expr b);

    default Expr gt(Expr a, int b) {
        return gt(a, Constant.of(b, a.typeKind()));
    }

    default Expr gt(Expr a, long b) {
        return gt(a, Constant.of(b, a.typeKind()));
    }

    default Expr gt(Expr a, float b) {
        return gt(a, Constant.of(b, a.typeKind()));
    }

    default Expr gt(Expr a, double b) {
        return gt(a, Constant.of(b, a.typeKind()));
    }

    Expr le(Expr a, Expr b);

    default Expr le(Expr a, int b) {
        return le(a, Constant.of(b, a.typeKind()));
    }

    default Expr le(Expr a, long b) {
        return le(a, Constant.of(b, a.typeKind()));
    }

    default Expr le(Expr a, float b) {
        return le(a, Constant.of(b, a.typeKind()));
    }

    default Expr le(Expr a, double b) {
        return le(a, Constant.of(b, a.typeKind()));
    }

    Expr ge(Expr a, Expr b);

    default Expr ge(Expr a, int b) {
        return ge(a, Constant.of(b, a.typeKind()));
    }

    default Expr ge(Expr a, long b) {
        return ge(a, Constant.of(b, a.typeKind()));
    }

    default Expr ge(Expr a, float b) {
        return ge(a, Constant.of(b, a.typeKind()));
    }

    default Expr ge(Expr a, double b) {
        return ge(a, Constant.of(b, a.typeKind()));
    }

    Expr cmp(Expr a, Expr b);

    Expr cmpl(Expr a, Expr b);

    Expr cmpg(Expr a, Expr b);


    // bitwise

    Expr and(Expr a, Expr b);

    Expr or(Expr a, Expr b);

    Expr xor(Expr a, Expr b);

    Expr complement(Expr a);

    Expr shl(Expr a, Expr b);

    Expr shr(Expr a, Expr b);

    Expr ushr(Expr a, Expr b);


    // bitwise-assign

    LValueExpr andAssign(LValueExpr var, Expr arg);

    LValueExpr orAssign(LValueExpr var, Expr arg);

    LValueExpr xorAssign(LValueExpr var, Expr arg);

    LValueExpr shlAssign(LValueExpr var, Expr arg);

    LValueExpr shrAssign(LValueExpr var, Expr arg);

    LValueExpr ushrAssign(LValueExpr var, Expr arg);


    // arithmetic

    Expr add(Expr a, Expr b);

    default Expr add(Expr a, int b) {
        return add(a, Constant.of(b, a.typeKind()));
    }

    default Expr add(Expr a, long b) {
        return add(a, Constant.of(b, a.typeKind()));
    }

    default Expr add(Expr a, float b) {
        return add(a, Constant.of(b, a.typeKind()));
    }

    default Expr add(Expr a, double b) {
        return add(a, Constant.of(b, a.typeKind()));
    }

    Expr sub(Expr a, Expr b);

    default Expr sub(Expr a, int b) {
        return sub(a, Constant.of(b, a.typeKind()));
    }

    default Expr sub(Expr a, long b) {
        return sub(a, Constant.of(b, a.typeKind()));
    }

    default Expr sub(Expr a, float b) {
        return sub(a, Constant.of(b, a.typeKind()));
    }

    default Expr sub(Expr a, double b) {
        return sub(a, Constant.of(b, a.typeKind()));
    }

    default Expr sub(int a, Expr b) {
        return sub(Constant.of(a, b.typeKind()), b);
    }

    default Expr sub(long a, Expr b) {
        return sub(Constant.of(a, b.typeKind()), b);
    }

    default Expr sub(float a, Expr b) {
        return sub(Constant.of(a, b.typeKind()), b);
    }

    default Expr sub(double a, Expr b) {
        return sub(Constant.of(a, b.typeKind()), b);
    }

    Expr mul(Expr a, Expr b);

    default Expr mul(Expr a, int b) {
        return mul(a, Constant.of(b, a.typeKind()));
    }

    default Expr mul(Expr a, long b) {
        return mul(a, Constant.of(b, a.typeKind()));
    }

    default Expr mul(Expr a, float b) {
        return mul(a, Constant.of(b, a.typeKind()));
    }

    default Expr mul(Expr a, double b) {
        return mul(a, Constant.of(b, a.typeKind()));
    }

    Expr div(Expr a, Expr b);

    default Expr div(Expr a, int b) {
        return div(a, Constant.of(b, a.typeKind()));
    }

    default Expr div(Expr a, long b) {
        return div(a, Constant.of(b, a.typeKind()));
    }

    default Expr div(Expr a, float b) {
        return div(a, Constant.of(b, a.typeKind()));
    }

    default Expr div(Expr a, double b) {
        return div(a, Constant.of(b, a.typeKind()));
    }

    default Expr div(int a, Expr b) {
        return div(Constant.of(a, b.typeKind()), b);
    }

    default Expr div(long a, Expr b) {
        return div(Constant.of(a, b.typeKind()), b);
    }

    default Expr div(float a, Expr b) {
        return div(Constant.of(a, b.typeKind()), b);
    }

    default Expr div(double a, Expr b) {
        return div(Constant.of(a, b.typeKind()), b);
    }

    Expr rem(Expr a, Expr b);

    default Expr rem(Expr a, int b) {
        return rem(a, Constant.of(b, a.typeKind()));
    }

    default Expr rem(Expr a, long b) {
        return rem(a, Constant.of(b, a.typeKind()));
    }

    default Expr rem(Expr a, float b) {
        return rem(a, Constant.of(b, a.typeKind()));
    }

    default Expr rem(Expr a, double b) {
        return rem(a, Constant.of(b, a.typeKind()));
    }

    default Expr rem(int a, Expr b) {
        return rem(Constant.of(a, b.typeKind()), b);
    }

    default Expr rem(long a, Expr b) {
        return rem(Constant.of(a, b.typeKind()), b);
    }

    default Expr rem(float a, Expr b) {
        return rem(Constant.of(a, b.typeKind()), b);
    }

    default Expr rem(double a, Expr b) {
        return rem(Constant.of(a, b.typeKind()), b);
    }

    Expr neg(Expr a);


    // arithmetic-assign

    LValueExpr addAssign(LValueExpr var, Expr arg);

    LValueExpr subAssign(LValueExpr var, Expr arg);

    LValueExpr mulAssign(LValueExpr var, Expr arg);

    LValueExpr divAssign(LValueExpr var, Expr arg);

    LValueExpr remAssign(LValueExpr var, Expr arg);


    // logical

    default Expr logicalNot(Expr a) {
        return eq(a, Constant.of(0, a.typeKind()));
    }

    default Expr logicalOr(Expr cond, Function<BlockCreator, Expr> other) {
        return selectExpr(CD_boolean, cond, __ -> Constant.of(true), other);
    }

    default Expr logicalAnd(Expr cond, Function<BlockCreator, Expr> other) {
        return selectExpr(CD_boolean, cond, other, __ -> Constant.of(false));
    }


    // conditional

    default Expr selectExpr(Class<?> type, Expr cond, Function<BlockCreator, Expr> ifTrue, Function<BlockCreator, Expr> ifFalse) {
        return selectExpr(Util.classDesc(type), cond, ifTrue, ifFalse);
    }

    Expr selectExpr(ClassDesc type, Expr cond, Function<BlockCreator, Expr> ifTrue, Function<BlockCreator, Expr> ifFalse);

    Expr switchExpr(Expr val, Consumer<SwitchExprCreator> builder);


    // lambda

    default Expr lambda(Class<?> type, Consumer<LambdaCreator> builder) {
        return lambda(Util.classDesc(type), builder);
    }

    Expr lambda(ClassDesc type, Consumer<LambdaCreator> builder);


    // conversion

    /**
     * Cast a value to the given type.
     * For primitives, the appropriate conversion is applied.
     * For objects, a class cast is performed.
     *
     * @param a the value to cast (must not be {@code null})
     * @param toType the type to cast to (must not be {@code null})
     * @return the cast value (not {@code null})
     * @see #instanceOf(Expr, ClassDesc)
     * @see #ifInstanceOf(Expr, ClassDesc, BiConsumer)
     * @see #ifInstanceOfElse(Expr, ClassDesc, BiConsumer, Consumer)
     */
    Expr cast(Expr a, ClassDesc toType);

    default Expr cast(Expr a, Class<?> toType) {
        return cast(a, Util.classDesc(toType));
    }

    Expr box(Expr a);

    Expr unbox(Expr a);


    // object

    Expr instanceOf(Expr obj, ClassDesc type);

    Expr new_(ConstructorDesc ctor, List<Expr> args);

    default Expr new_(ClassDesc type, List<Expr> args) {
        return new_(ConstructorDesc.of(type, args.stream().map(Expr::type).toList()), args);
    }

    default Expr new_(Class<?> type, List<Expr> args) {
        return new_(Util.classDesc(type), args);
    }


    // invocation

    Expr invokeStatic(MethodDesc method, List<Expr> args);

    Expr invokeVirtual(Expr instance, MethodDesc method, List<Expr> args);

    Expr invokeSpecial(Expr instance, MethodDesc method, List<Expr> args);

    Expr invokeSpecial(Expr instance, ConstructorDesc method, List<Expr> args);

    Expr invokeInterface(Expr instance, MethodDesc method, List<Expr> args);


    // control flow

    /**
     * Build a for-each loop over an array or collection.
     *
     * @param items the array or collection (must not be {@code null})
     * @param builder the builder for the loop body (must not be {@code null})
     */
    void forEach(Expr items, BiConsumer<BlockCreator, Expr> builder);

    /**
     * Create a nested block.
     *
     * @param nested the builder for the block body (must not be {@code null})
     */
    void block(Consumer<BlockCreator> nested);

    /**
     * Create a block which takes an argument.
     *
     * @param arg the block argument (must not be {@code null})
     * @param nested the builder for the block body (must not be {@code null})
     */
    void block(Expr arg, BiConsumer<BlockCreator, Expr> nested);

    /**
     * Create a block expression.
     *
     * @param type the output type (must not be {@code null})
     * @param nested the builder for the block body (must not be {@code null})
     * @return the returned value (not {@code null})
     */
    Expr blockExpr(ClassDesc type, Function<BlockCreator, Expr> nested);

    /**
     * Create a block expression which takes an argument.
     *
     * @param arg the block argument (must not be {@code null})
     * @param type the output type (must not be {@code null})
     * @param nested the builder for the block body (must not be {@code null})
     * @return the returned value (not {@code null})
     */
    Expr blockExpr(Expr arg, ClassDesc type, BiFunction<BlockCreator, Expr, Expr> nested);

    void ifInstanceOf(Expr obj, ClassDesc type, BiConsumer<BlockCreator, Expr> ifTrue);

    void ifInstanceOfElse(Expr obj, ClassDesc type, BiConsumer<BlockCreator, Expr> ifTrue, Consumer<BlockCreator> ifFalse);

    void if_(Expr cond, Consumer<BlockCreator> whenTrue);

    void unless(Expr cond, Consumer<BlockCreator> whenFalse);

    void ifElse(Expr cond, Consumer<BlockCreator> whenTrue, Consumer<BlockCreator> whenFalse);

    /**
     * Construct a {@code switch} statement.
     * The switch value must be of a supported type.
     *
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     */
    void switch_(Expr val, Consumer<SwitchCreator> builder);

    /**
     * Exit an enclosing block.
     * Blocks which are part of an expression building operation (i.e. a {@code Function<BlockCreator, Expr>}) may
     * not be the target of a {@code break}.
     *
     * @param outer the block to break (must not be {@code null})
     */
    void break_(BlockCreator outer);

    /**
     * Restart an enclosing block.
     * Blocks which are part of an expression-accepting operation (i.e. a {@code BiConsumer<BlockCreator, Expr>}) may
     * not be the target of a {@code redo}.
     *
     * @param outer the block to restart (must not be {@code null})
     */
    void redo(BlockCreator outer);

    /**
     * Jump to a specific case in an enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #redoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    void redo(SwitchCreator switch_, Constant case_);

    default void redo(SwitchCreator switch_, int case_) {
        redo(switch_, Constant.of(case_));
    }

    default void redo(SwitchCreator switch_, String case_) {
        redo(switch_, Constant.of(case_));
    }

    default void redo(SwitchCreator switch_, Enum<?> case_) {
        redo(switch_, Constant.of(case_));
    }

    default void redo(SwitchCreator switch_, Class<?> case_) {
        redo(switch_, Constant.of(case_));
    }

    /**
     * Jump to the default case in an enclosing {@code switch}.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     */
    void redoDefault(SwitchCreator switch_);

    /**
     * Restart this block from the top.
     */
    default void redo() {
        redo(this);
    }

    /**
     * Enter a loop.
     * The loop may be exited by calling {@link #break_(BlockCreator)} on the loop's block.
     *
     * @param body the loop body (must not be {@code null})
     */
    void loop(Consumer<BlockCreator> body);

    void while_(Function<BlockCreator, Expr> cond, Consumer<BlockCreator> body);

    void doWhile(Consumer<BlockCreator> body, Function<BlockCreator, Expr> cond);

    void try_(Consumer<TryCreator> body);

    /**
     * Open a resource and run the given body with the resource, automatically closing it at the end.
     *
     * @param resource the resource to automatically close (must not be {@code null})
     * @param body the body of the resource operation (must not be {@code null})
     */
    void autoClose(Expr resource, BiConsumer<BlockCreator, Expr> body);

    void synchronized_(Expr monitor, Consumer<BlockCreator> body);

    void locked(Expr jucLock, Consumer<BlockCreator> body);


    // exiting

    void return_();

    void return_(Expr val);

    default void return_(String val) {
        return_(Constant.of(val));
    }

    default void return_(Class<?> val) {
        return_(Constant.of(val));
    }

    default void return_(boolean val) {
        return_(Constant.of(val));
    }

    default void return_(int val) {
        return_(Constant.of(val));
    }

    default void returnTrue() {
        return_(true);
    }

    default void returnFalse() {
        return_(false);
    }

    default void returnIntZero() {
        return_(Constant.of(0));
    }

    default void returnNull(ClassDesc type) {
        return_(Constant.ofNull(type));
    }

    default void returnNull(Class<?> type) {
        return_(Constant.ofNull(type));
    }

    //xxx more returns

    void throw_(Expr val);

    default void throw_(ClassDesc type) {
        throw_(new_(type, List.of()));
    }

    default void throw_(ClassDesc type, String message) {
        throw_(new_(type, List.of(Constant.of(message))));
    }

    default void throw_(Class<? extends Throwable> type) {
        throw_(Util.classDesc(type));
    }

    default void throw_(Class<? extends Throwable> type, String message) {
        if (message == null) {
            throw_(type);
        } else {
            throw_(Util.classDesc(type), message);
        }
    }

    // useful helpers/utilities

    Expr objHashCode(Expr obj);

    Expr objEquals(Expr a, Expr b);

    Expr arrayEquals(Expr a, Expr b);

    Expr loadClass(Expr className);

    Expr listOf(List<Expr> items);

    Expr setOf(List<Expr> items);

    Expr iterate(Expr items);

    Expr currentThread();

    /**
     * A convenience method to call the {@code hasNext} method on an {@code Iterator}.
     *
     * @param iterator the iterator (must not be {@code null})
     * @return the boolean result (not {@code null})
     */
    Expr iterHasNext(Expr iterator);

    Expr iterNext(Expr iterator);

    void close(Expr closeable);

    void addSuppressed(Expr throwable, Expr suppressed);

    // debug stuff

    void line(int lineNumber);

    void printf(String format, List<Expr> values);

    void assert_(Function<BlockCreator, Expr> assertion, String message);
}

