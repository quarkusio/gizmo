package io.quarkus.gizmo2.creator;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.Constable;
import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicCallSiteDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.VarHandle;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import io.quarkus.gizmo2.Assignable;
import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.MemoryOrder;
import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.ops.ClassOps;
import io.quarkus.gizmo2.creator.ops.CollectionOps;
import io.quarkus.gizmo2.creator.ops.IteratorOps;
import io.quarkus.gizmo2.creator.ops.ListOps;
import io.quarkus.gizmo2.creator.ops.MapOps;
import io.quarkus.gizmo2.creator.ops.ObjectOps;
import io.quarkus.gizmo2.creator.ops.OptionalOps;
import io.quarkus.gizmo2.creator.ops.SetOps;
import io.quarkus.gizmo2.creator.ops.StringBuilderOps;
import io.quarkus.gizmo2.creator.ops.StringOps;
import io.quarkus.gizmo2.creator.ops.ThrowableOps;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.ArrayDeref;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.FieldDeref;
import io.quarkus.gizmo2.impl.StaticFieldVarImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A code block. All blocks have a {@linkplain #type() type} and if that type is not {@code void},
 * the block must {@linkplain #yield(Expr) yield} a value (or exit some other way).
 */
public sealed interface BlockCreator extends SimpleTyped permits BlockCreatorImpl {
    /**
     * {@return the type of this block (may be {@code void})}
     * If the type is non-{@code void}, then the block must {@linkplain #yield(Expr) yield} a value
     * if it does not exit explicitly some other way.
     */
    ClassDesc type();

    /**
     * Yield a value from this block.
     * If control falls out of a block, an implicit {@code yield(Constant.ofVoid())}
     * is added to terminate it.
     *
     * @param value the value to yield (must not be {@code null})
     * @throws IllegalArgumentException if the value type does not match the {@linkplain #type() type of the block}
     */
    void yield(Expr value);

    /**
     * Yield a {@code null} value from this block.
     *
     * @throws IllegalArgumentException if this block's type is primitive or {@code void}
     */
    default void yieldNull() {
        this.yield(Const.ofNull(type()));
    }

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

    /**
     * {@return true if this block is contained by the given block, or false if it is not}
     *
     * @param other the containing block (must not be {@code null})
     */
    boolean isContainedBy(BlockCreator other);

    /**
     * {@return true if this block contains the given block, or false if it does not}
     *
     * @param other the contained block (must not be {@code null})
     */
    default boolean contains(BlockCreator other) {
        return other.isContainedBy(this);
    }

    /**
     * {@return true if this block contains the block that owns the given variable, or false if it does not}
     *
     * @param var the variable (must not be {@code null})
     */
    default boolean contains(LocalVar var) {
        return contains(var.block());
    }

    // local variables

    /**
     * Declare a local variable with given {@code name} and {@code type}, which is initialized
     * to the given {@code value}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param type the variable type (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    LocalVar localVar(String name, GenericType type, Expr value);

    /**
     * Declare a local variable with given {@code name} and {@code type}, which is initialized
     * to the given {@code value}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param type the variable type (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    LocalVar localVar(String name, ClassDesc type, Expr value);

    /**
     * Declare a local variable with given {@code name} and {@code type}, which is initialized
     * to the given {@code value}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param type the variable type (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, Class<?> type, Expr value) {
        return localVar(name, Util.classDesc(type), value);
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is the type of the {@code value}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, Expr value) {
        if (value.hasGenericType()) {
            return localVar(name, value.genericType(), value);
        } else {
            return localVar(name, value.type(), value);
        }
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code boolean}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, boolean value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code int}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, int value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code long}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, long value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code float}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, float value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code double}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, double value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable with given {@code name}, which is initialized to the given {@code value}.
     * The type of the new variable is {@code String}.
     * <p>
     * Variable names are not strictly required to be unique, but it is a good practice.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    default LocalVar localVar(String name, String value) {
        return localVar(name, Const.of(value));
    }

    /**
     * Declare a local variable, which is initialized to the given variable's current value.
     * The name and type of the new variable are the same as the name and type of the {@code original}.
     *
     * @param original the original variable (must not be {@code null})
     * @return the new local variable (not {@code null})
     */
    default LocalVar localVar(Var original) {
        return localVar(original.name(), original);
    }

    // reading memory

    /**
     * Read a value from memory with the given atomicity mode.
     *
     * @param var the assignable (must not be {@code null})
     * @param mode the atomicity mode for the access (must not be {@code null})
     * @return the memory value (not {@code null})
     */
    Expr get(Assignable var, MemoryOrder mode);

    /**
     * Read a value from memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @return the memory value (not {@code null})
     */
    default Expr get(Assignable var) {
        return get(var, MemoryOrder.AsDeclared);
    }

    /**
     * Read the value from a static field.
     *
     * @param desc the field descriptor (must not be {@code null})
     * @return the memory value (not {@code null})
     */
    default Expr getStaticField(FieldDesc desc) {
        return get(Expr.staticField(desc));
    }

    // writing memory

    /**
     * Write a value to memory with the given atomicity mode.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     * @param mode the atomicity mode for the access (must not be {@code null})
     */
    void set(Assignable var, Expr value, MemoryOrder mode);

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(Assignable var, Expr value) {
        set(var, value, MemoryOrder.AsDeclared);
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(Assignable var, Const value) {
        set(var, (Expr) value);
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(Assignable var, ConstantDesc value) {
        set(var, Const.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(Assignable var, Constable value) {
        set(var, Const.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(Assignable var, String value) {
        set(var, Const.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write
     */
    default void set(Assignable var, int value) {
        set(var, Const.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the assignable.
     *
     * @param var the assignable (must not be {@code null})
     * @param value the value to write
     */
    default void set(Assignable var, long value) {
        set(var, Const.of(value));
    }

    /**
     * Write the value to a static field.
     *
     * @param desc the field descriptor (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void setStaticField(FieldDesc desc, Expr value) {
        set(Expr.staticField(desc), value);
    }

    /**
     * Swap the values of two variables
     * without requiring an intermediate temporary variable.
     *
     * @param var1 the first variable (must not be {@code null})
     * @param var2 the second variable (must not be {@code null})
     */
    default void swap(Assignable var1, Assignable var2) {
        Expr get1 = get(var1);
        set(var1, get(var2));
        set(var2, get1);
    }

    /**
     * Rotate the values of three variables one position to the right
     * without requiring an intermediate temporary variable.
     * The rightmost value is moved to the leftmost variable.
     *
     * @param var1 the first variable (must not be {@code null})
     * @param var2 the second variable (must not be {@code null})
     * @param var3 the third variable (must not be {@code null})
     */
    default void rotate(Assignable var1, Assignable var2, Assignable var3) {
        Expr get1 = get(var1);
        set(var1, get(var3));
        set(var3, get(var2));
        set(var2, get1);
    }

    /**
     * Rotate the values of four variables one position to the right
     * without requiring an intermediate temporary variable.
     * The rightmost value is moved to the leftmost variable.
     *
     * @param var1 the first variable (must not be {@code null})
     * @param var2 the second variable (must not be {@code null})
     * @param var3 the third variable (must not be {@code null})
     * @param var4 the fourth variable (must not be {@code null})
     */
    default void rotate(Assignable var1, Assignable var2, Assignable var3, Assignable var4) {
        Expr get1 = get(var1);
        set(var1, get(var4));
        set(var4, get(var3));
        set(var3, get(var2));
        set(var2, get1);
    }

    // increment/decrement

    /**
     * Increment some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to increase by (must not be {@code null})
     */
    void inc(Assignable var, Const amount);

    /**
     * Increment some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to increase by
     */
    default void inc(Assignable var, int amount) {
        inc(var, Const.of(amount, var.typeKind()));
    }

    /**
     * Increment some variable by one.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     */
    default void inc(Assignable var) {
        inc(var, 1);
    }

    /**
     * Decrement some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to decrease by (must not be {@code null})
     */
    void dec(Assignable var, Const amount);

    /**
     * Decrement some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to decrease by
     */
    default void dec(Assignable var, int amount) {
        dec(var, Const.of(amount, var.typeKind()));
    }

    /**
     * Decrement some variable by one.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     */
    default void dec(Assignable var) {
        dec(var, 1);
    }

    // atomic

    private Expr compareAndSet(Assignable var, Expr expected, Expr update, String name) {
        final Const varHandle;
        final List<Expr> args;
        final MethodTypeDesc typeDesc;
        if (var instanceof StaticFieldVarImpl sfv) {
            varHandle = Const.ofStaticFieldVarHandle(sfv.desc());
            args = List.of(expected, update);
            typeDesc = MethodTypeDesc.of(CD_boolean, var.type(), var.type());
        } else if (var instanceof FieldDeref fd) {
            varHandle = Const.ofFieldVarHandle(fd.desc());
            args = List.of(fd.instance(), expected, update);
            typeDesc = MethodTypeDesc.of(CD_boolean, fd.instance().type(), var.type(), var.type());
        } else if (var instanceof ArrayDeref ad) {
            varHandle = Const.ofArrayVarHandle(ad.type().arrayType());
            args = List.of(ad.array(), ad.index(), expected, update);
            typeDesc = MethodTypeDesc.of(CD_boolean, ad.array().type(), CD_int, var.type(), var.type());
        } else {
            throw new IllegalArgumentException("Unsupported target for atomic operations: " + var);
        }
        return invokeVirtual(ClassMethodDesc.of(
                CD_VarHandle,
                name,
                typeDesc), varHandle, args);
    }

    /**
     * Atomically sets the value of {@code var} to {@code update} if its current value
     * is equal to {@code expected}.
     *
     * @param var the variable to update (must not be {@code null})
     * @param expected the expected comparison value (must not be {@code null})
     * @param update the new value to set (must not be {@code null})
     * @return a {@code boolean}-typed expression that is {@code true} if the update succeeded or {@code false} if it did not
     */
    default Expr compareAndSet(Assignable var, Expr expected, Expr update) {
        return compareAndSet(var, expected, update, "compareAndSet");
    }

    /**
     * Atomically sets the value of {@code var} to {@code update} if its current value
     * is equal to {@code expected}.
     * The comparison is "weak", meaning that it may fail sporadically.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Plain}</li>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the variable to update (must not be {@code null})
     * @param expected the expected comparison value (must not be {@code null})
     * @param update the new value to set (must not be {@code null})
     * @param order the memory order which is used for the operation (must not be {@code null})
     * @return a {@code boolean}-typed expression that is {@code true} if the update succeeded or {@code false} if it did not
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr weakCompareAndSet(Assignable var, Expr expected, Expr update, MemoryOrder order) {
        return compareAndSet(var, expected, update,
                switch (order) {
                    case Volatile -> "weakCompareAndSet";
                    case Acquire -> "weakCompareAndSetAcquire";
                    case Release -> "weakCompareAndSetRelease";
                    case Plain -> "weakCompareAndSetPlain";
                    default -> throw new IllegalArgumentException("Unsupported memory order " + order);
                });
    }

    /**
     * Atomically sets the value of {@code var} to {@code update} if its current value
     * is equal to {@code expected}, using {@code volatile} memory ordering.
     * The comparison is "weak", meaning that it may fail sporadically.
     *
     * @param var the variable to update (must not be {@code null})
     * @param expected the expected comparison value (must not be {@code null})
     * @param update the new value to set (must not be {@code null})
     * @return a {@code boolean}-typed expression that is {@code true} if the update succeeded or {@code false} if it did not
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr weakCompareAndSet(Assignable var, Expr expected, Expr update) {
        return weakCompareAndSet(var, expected, update, MemoryOrder.Volatile);
    }

    default Expr compareAndExchange(Assignable var, Expr expected, Expr update, MemoryOrder order) {
        final Const varHandle;
        final List<Expr> args;
        final MethodTypeDesc typeDesc;
        if (var instanceof StaticFieldVarImpl sfv) {
            varHandle = Const.ofStaticFieldVarHandle(sfv.desc());
            args = List.of(expected, update);
            typeDesc = MethodTypeDesc.of(var.type(), var.type(), var.type());
        } else if (var instanceof FieldDeref fd) {
            varHandle = Const.ofFieldVarHandle(fd.desc());
            args = List.of(fd.instance(), expected, update);
            typeDesc = MethodTypeDesc.of(var.type(), fd.instance().type(), var.type(), var.type());
        } else if (var instanceof ArrayDeref ad) {
            varHandle = Const.ofArrayVarHandle(ad.type().arrayType());
            args = List.of(ad.array(), ad.index(), expected, update);
            typeDesc = MethodTypeDesc.of(var.type(), ad.array().type(), CD_int, var.type(), var.type());
        } else {
            throw new IllegalArgumentException("Unsupported target for atomic operations: " + var);
        }
        return invokeVirtual(ClassMethodDesc.of(
                CD_VarHandle,
                switch (order) {
                    case Volatile -> "compareAndExchange";
                    case Acquire -> "compareAndExchangeAcquire";
                    case Release -> "compareAndExchangeRelease";
                    default -> throw new IllegalArgumentException("Unsupported memory order " + order);
                },
                typeDesc), varHandle, args);
    }

    default Expr compareAndExchange(Assignable var, Expr expected, Expr update) {
        return compareAndExchange(var, expected, update, MemoryOrder.Volatile);
    }

    private Expr getAndXxx(Assignable var, Expr arg, String name) {
        final Const varHandle;
        final List<Expr> args;
        final MethodTypeDesc typeDesc;
        if (var instanceof StaticFieldVarImpl sfv) {
            varHandle = Const.ofStaticFieldVarHandle(sfv.desc());
            args = List.of(arg);
            typeDesc = MethodTypeDesc.of(var.type(), var.type());
        } else if (var instanceof FieldDeref fd) {
            varHandle = Const.ofFieldVarHandle(fd.desc());
            args = List.of(fd.instance(), arg);
            typeDesc = MethodTypeDesc.of(var.type(), fd.instance().type(), var.type());
        } else if (var instanceof ArrayDeref ad) {
            varHandle = Const.ofArrayVarHandle(ad.type().arrayType());
            args = List.of(ad.array(), ad.index(), arg);
            typeDesc = MethodTypeDesc.of(var.type(), ad.array().type(), CD_int, var.type());
        } else {
            throw new IllegalArgumentException("Unsupported target for atomic operations: " + var);
        }
        return invokeVirtual(ClassMethodDesc.of(
                CD_VarHandle,
                name,
                typeDesc), varHandle, args);
    }

    /**
     * Atomically get and set the value of the target assignable expression.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param newValue the value to store (must not be {@code null})
     * @param order the memory order (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr getAndSet(Assignable var, Expr newValue, MemoryOrder order) {
        return getAndXxx(var, newValue, switch (order) {
            case Volatile -> "getAndSet";
            case Acquire -> "getAndSetAcquire";
            case Release -> "getAndSetRelease";
            default -> throw new IllegalArgumentException("Unsupported memory order " + order);
        });
    }

    /**
     * Atomically get and set the value of the target assignable expression
     * using {@code volatile} semantics.
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param newValue the value to store (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr getAndSet(Assignable var, Expr newValue) {
        return getAndSet(var, newValue, MemoryOrder.Volatile);
    }

    /**
     * Atomically get, add, and store the value of the target assignable expression.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param amount the value to add to the target (must not be {@code null})
     * @param order the memory order (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr getAndAdd(Assignable var, Expr amount, MemoryOrder order) {
        return getAndXxx(var, amount, switch (order) {
            case Volatile -> "getAndAdd";
            case Acquire -> "getAndAddAcquire";
            case Release -> "getAndAddRelease";
            default -> throw new IllegalArgumentException("Unsupported memory order " + order);
        });
    }

    /**
     * Atomically get, add, and store the value of the target assignable expression
     * using {@code volatile} semantics.
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param amount the value to add to the target (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr getAndAdd(Assignable var, Expr amount) {
        return getAndAdd(var, amount, MemoryOrder.Volatile);
    }

    /**
     * Atomically get, bitwise-or, and store the value of the target assignable expression.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-or with the target (must not be {@code null})
     * @param order the memory order (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr getAndBitwiseOr(Assignable var, Expr other, MemoryOrder order) {
        return getAndXxx(var, other, switch (order) {
            case Volatile -> "getAndBitwiseOr";
            case Acquire -> "getAndBitwiseOrAcquire";
            case Release -> "getAndBitwiseOrRelease";
            default -> throw new IllegalArgumentException("Unsupported memory order " + order);
        });
    }

    /**
     * Atomically get, bitwise-or, and store the value of the target assignable expression
     * with {@code volatile} semantics.
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-or with the target (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr getAndBitwiseOr(Assignable var, Expr other) {
        return getAndBitwiseOr(var, other, MemoryOrder.Volatile);
    }

    /**
     * Atomically get, bitwise-and, and store the value of the target assignable expression.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-and with the target (must not be {@code null})
     * @param order the memory order (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr getAndBitwiseAnd(Assignable var, Expr other, MemoryOrder order) {
        return getAndXxx(var, other, switch (order) {
            case Volatile -> "getAndBitwiseAnd";
            case Acquire -> "getAndBitwiseAndAcquire";
            case Release -> "getAndBitwiseAndRelease";
            default -> throw new IllegalArgumentException("Unsupported memory order " + order);
        });
    }

    /**
     * Atomically get, bitwise-and, and store the value of the target assignable expression
     * with {@code volatile} semantics.
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-and with the target (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr getAndBitwiseAnd(Assignable var, Expr other) {
        return getAndBitwiseAnd(var, other, MemoryOrder.Volatile);
    }

    /**
     * Atomically get, bitwise-xor, and store the value of the target assignable expression.
     * The given memory order is used, and must be one of:
     * <ul>
     * <li>{@link MemoryOrder#Acquire}</li>
     * <li>{@link MemoryOrder#Release}</li>
     * <li>{@link MemoryOrder#Volatile}</li>
     * </ul>
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-xor with the target (must not be {@code null})
     * @param order the memory order (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically,
     *         or if {@code order} is not one of the allowed values
     */
    default Expr getAndBitwiseXor(Assignable var, Expr other, MemoryOrder order) {
        return getAndXxx(var, other, switch (order) {
            case Volatile -> "getAndBitwiseXor";
            case Acquire -> "getAndBitwiseXorAcquire";
            case Release -> "getAndBitwiseXorRelease";
            default -> throw new IllegalArgumentException("Unsupported memory order " + order);
        });
    }

    /**
     * Atomically get, bitwise-xor, and store the value of the target assignable expression
     * with {@code volatile} semantics.
     *
     * @param var the target assignable expression (must not be {@code null})
     * @param other the value to bitwise-xor with the target (must not be {@code null})
     * @return the previous value of the target expression (not {@code null})
     * @throws IllegalArgumentException if the target variable cannot be accessed atomically
     */
    default Expr getAndBitwiseXor(Assignable var, Expr other) {
        return getAndBitwiseXor(var, other, MemoryOrder.Volatile);
    }

    // fences

    /**
     * Emit a {@linkplain VarHandle#fullFence() full fence}.
     */
    default void fullFence() {
        invokeStatic(ClassMethodDesc.of(CD_VarHandle, "fullFence", MethodTypeDesc.of(CD_void)));
    }

    /**
     * Emit an {@linkplain VarHandle#acquireFence() acquire fence}.
     */
    default void acquireFence() {
        invokeStatic(ClassMethodDesc.of(CD_VarHandle, "acquireFence", MethodTypeDesc.of(CD_void)));
    }

    /**
     * Emit a {@linkplain VarHandle#releaseFence() release fence}.
     */
    default void releaseFence() {
        invokeStatic(ClassMethodDesc.of(CD_VarHandle, "release", MethodTypeDesc.of(CD_void)));
    }

    /**
     * Emit a {@linkplain VarHandle#loadLoadFence() <em>LoadLoad</em> fence}.
     */
    default void loadLoadFence() {
        invokeStatic(ClassMethodDesc.of(CD_VarHandle, "loadLoadFence", MethodTypeDesc.of(CD_void)));
    }

    /**
     * Emit a {@linkplain VarHandle#loadLoadFence() <em>StoreStore</em> fence}.
     */
    default void storeStoreFence() {
        invokeStatic(ClassMethodDesc.of(CD_VarHandle, "storeStoreFence", MethodTypeDesc.of(CD_void)));
    }

    /**
     * Emit a {@linkplain Reference#reachabilityFence(Object) reachability fence} for the given object
     * expression.
     *
     * @param obj the object expression (must not be {@code null})
     * @throws IllegalArgumentException if the expression is not a reference type
     */
    default void reachabilityFence(Expr obj) {
        if (obj.typeKind() != TypeKind.REFERENCE) {
            throw new IllegalArgumentException("Reachability fence can only be emitted for reference types");
        }
        invokeStatic(MethodDesc.of(Reference.class, "reachabilityFence", MethodTypeDesc.of(CD_void, CD_Object)), obj);
    }

    // arrays

    /**
     * Create a new, empty array of the given type with given size.
     *
     * @param componentType the component type (must not be {@code null})
     * @param size the size of the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    Expr newEmptyArray(ClassDesc componentType, Expr size);

    /**
     * Create a new, empty array of the given type with given size.
     *
     * @param componentType the component type (must not be {@code null})
     * @param size the size of the array
     * @return the expression for the new array (not {@code null})
     */
    default Expr newEmptyArray(ClassDesc componentType, int size) {
        return newEmptyArray(componentType, Const.of(size));
    }

    /**
     * Create a new, empty array of the given type with given size.
     *
     * @param componentType the component type (must not be {@code null})
     * @param size the size of the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newEmptyArray(Class<?> componentType, Expr size) {
        return newEmptyArray(Util.classDesc(componentType), size);
    }

    /**
     * Create a new, empty array of the given type with given size.
     *
     * @param componentType the component type (must not be {@code null})
     * @param size the size of the array
     * @return the expression for the new array (not {@code null})
     */
    default Expr newEmptyArray(Class<?> componentType, int size) {
        return newEmptyArray(Util.classDesc(componentType), Const.of(size));
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array after mapping (must not be {@code null})
     * @param mapper function that turns values into expressions (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    <T> Expr newArray(ClassDesc componentType, List<T> values, Function<T, ? extends Expr> mapper);

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(ClassDesc componentType, List<? extends Expr> values) {
        return newArray(componentType, values, Function.identity());
    };

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(ClassDesc componentType, Expr... values) {
        return newArray(componentType, List.of(values));
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array after mapping (must not be {@code null})
     * @param mapper function that turns values into expressions (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default <T> Expr newArray(Class<?> componentType, List<T> values, Function<T, ? extends Expr> mapper) {
        return newArray(Util.classDesc(componentType), values, mapper);
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(Class<?> componentType, List<? extends Expr> values) {
        return newArray(Util.classDesc(componentType), values);
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param componentType the component type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(Class<?> componentType, Expr... values) {
        return newArray(componentType, List.of(values));
    }

    // relational ops

    default Expr isNull(Expr input) {
        return eq(input, Const.ofNull(input.type()));
    }

    default Expr isNotNull(Expr input) {
        return ne(input, Const.ofNull(input.type()));
    }

    /**
     * The equality operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive and reference values.
     * For object equality using {@link Object#equals}, see {@link #objEquals(Expr, Expr)}.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument (must not be {@code null})
     * @return the boolean result expression
     * @see #objEquals(Expr, Expr)
     */
    Expr eq(Expr a, Expr b);

    /**
     * The equality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr eq(Expr a, int b) {
        return eq(a, Const.of(b, a.typeKind()));
    }

    /**
     * The equality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr eq(Expr a, long b) {
        return eq(a, Const.of(b, a.typeKind()));
    }

    /**
     * The equality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr eq(Expr a, float b) {
        return eq(a, Const.of(b, a.typeKind()));
    }

    /**
     * The equality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr eq(Expr a, double b) {
        return eq(a, Const.of(b, a.typeKind()));
    }

    /**
     * The inequality operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive and reference values.
     * For object equality using {@link Object#equals}, see {@link #objEquals(Expr, Expr)}.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument (must not be {@code null})
     * @return the boolean result expression
     * @see #objEquals(Expr, Expr)
     */
    Expr ne(Expr a, Expr b);

    /**
     * The inequality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ne(Expr a, int b) {
        return ne(a, Const.of(b, a.typeKind()));
    }

    /**
     * The inequality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ne(Expr a, long b) {
        return ne(a, Const.of(b, a.typeKind()));
    }

    /**
     * The inequality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ne(Expr a, float b) {
        return ne(a, Const.of(b, a.typeKind()));
    }

    /**
     * The inequality operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ne(Expr a, double b) {
        return ne(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code <} operator in Java
     * for primitive values.
     * This operation does not support reference values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    Expr lt(Expr a, Expr b);

    /**
     * The less-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr lt(Expr a, int b) {
        return lt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr lt(Expr a, long b) {
        return lt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr lt(Expr a, float b) {
        return lt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr lt(Expr a, double b) {
        return lt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code >} operator in Java
     * for primitive values.
     * This operation does not support reference values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    Expr gt(Expr a, Expr b);

    /**
     * The greater-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr gt(Expr a, int b) {
        return gt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr gt(Expr a, long b) {
        return gt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr gt(Expr a, float b) {
        return gt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr gt(Expr a, double b) {
        return gt(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than-or-equals operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code <=} operator in Java
     * for primitive values.
     * This operation does not support reference values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    Expr le(Expr a, Expr b);

    /**
     * The less-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr le(Expr a, int b) {
        return le(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr le(Expr a, long b) {
        return le(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr le(Expr a, float b) {
        return le(a, Const.of(b, a.typeKind()));
    }

    /**
     * The less-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code <=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr le(Expr a, double b) {
        return le(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than-or-equals operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code >=} operator in Java
     * for primitive values.
     * This operation does not support reference values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    Expr ge(Expr a, Expr b);

    /**
     * The greater-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ge(Expr a, int b) {
        return ge(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ge(Expr a, long b) {
        return ge(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ge(Expr a, float b) {
        return ge(a, Const.of(b, a.typeKind()));
    }

    /**
     * The greater-than-or-equals operator.
     * The second argument will be cast to the same {@linkplain TypeKind type kind} as the first argument.
     * This works equivalently to the {@code >=} operator in Java
     * for primitive values.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument
     * @return the boolean result expression
     */
    default Expr ge(Expr a, double b) {
        return ge(a, Const.of(b, a.typeKind()));
    }

    /**
     * The general comparison operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * <p>
     * For primitives, this returns {@code -1}, {@code 0}, or {@code 1} if
     * the second argument is less than, equal to, or greater than the first argument, respectively.
     * <p>
     * For reference values, this returns the result of natural-order comparisons
     * using {@link Comparable#compareTo(Object)}.
     * If the {@linkplain #cast(Expr, ClassDesc) static type} of either value does not implement this interface,
     * the class will not verify.
     * <p>
     * Comparisons between floating point values will have behavior equivalent to that of the
     * {@link Float#compare(float, float)} or {@link Double#compare(double, double)} methods,
     * particularly as this relates to {@code NaN} and negative-zero values.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the comparison result expression (not {@code null})
     */
    Expr cmp(Expr a, Expr b);

    /**
     * The general comparison operator.
     * This method behaves equivalently to {@link #cmp} in all respects,
     * except that floating point value comparison will result in a {@code -1}
     * if either value is {@code NaN}, and that negative zero is considered equal to
     * positive zero.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the comparison result expression (not {@code null})
     */
    Expr cmpl(Expr a, Expr b);

    /**
     * The general comparison operator.
     * This method behaves equivalently to {@link #cmp} in all respects,
     * except that floating point value comparison will result in a {@code 1}
     * if either value is {@code NaN}, and that negative zero is considered equal to
     * positive zero.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the comparison result expression (not {@code null})
     */
    Expr cmpg(Expr a, Expr b);

    // bitwise

    /**
     * The bitwise {@code and} operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code &} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr and(Expr a, Expr b);

    /**
     * The bitwise {@code and} operator.
     * This method works equivalently to the {@code &} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr and(Expr a, int b) {
        return and(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise {@code and} operator.
     * This method works equivalently to the {@code &} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr and(Expr a, long b) {
        return and(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise {@code or} operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code |} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr or(Expr a, Expr b);

    /**
     * The bitwise {@code or} operator.
     * This method works equivalently to the {@code |} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr or(Expr a, int b) {
        return or(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise {@code or} operator.
     * This method works equivalently to the {@code |} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr or(Expr a, long b) {
        return or(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise {@code xor} operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code ^} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr xor(Expr a, Expr b);

    /**
     * The bitwise {@code xor} operator.
     * This method works equivalently to the {@code ^} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr xor(Expr a, int b) {
        return xor(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise {@code xor} operator.
     * This method works equivalently to the {@code ^} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr xor(Expr a, long b) {
        return xor(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise complement operator.
     * This method works equivalently to the {@code ~} operator in Java.
     *
     * @param a the argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr complement(Expr a);

    /**
     * The bitwise left-shift operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code <<} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr shl(Expr a, Expr b);

    /**
     * The bitwise left-shift operator.
     * This method works equivalently to the {@code <<} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr shl(Expr a, int b) {
        return shl(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise left-shift operator.
     * This method works equivalently to the {@code <<} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr shl(Expr a, long b) {
        return shl(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise signed-right-shift operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code >>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr shr(Expr a, Expr b);

    /**
     * The bitwise signed-right-shift operator.
     * This method works equivalently to the {@code >>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr shr(Expr a, int b) {
        return shr(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise signed-right-shift operator.
     * This method works equivalently to the {@code >>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr shr(Expr a, long b) {
        return shr(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise unsigned-right-shift operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code >>>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr ushr(Expr a, Expr b);

    /**
     * The bitwise unsigned-right-shift operator.
     * This method works equivalently to the {@code >>>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr ushr(Expr a, int b) {
        return ushr(a, Const.of(b, a.typeKind()));
    }

    /**
     * The bitwise unsigned-right-shift operator.
     * This method works equivalently to the {@code >>>} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr ushr(Expr a, long b) {
        return ushr(a, Const.of(b, a.typeKind()));
    }

    // arithmetic

    /**
     * The arithmetic addition operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code +} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr add(Expr a, Expr b);

    /**
     * The arithmetic addition operator.
     * This method works equivalently to the {@code +} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr add(Expr a, int b) {
        return add(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic addition operator.
     * This method works equivalently to the {@code +} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr add(Expr a, long b) {
        return add(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic addition operator.
     * This method works equivalently to the {@code +} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr add(Expr a, float b) {
        return add(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic addition operator.
     * This method works equivalently to the {@code +} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr add(Expr a, double b) {
        return add(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic subtraction operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr sub(Expr a, Expr b);

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr sub(Expr a, int b) {
        return sub(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr sub(Expr a, long b) {
        return sub(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr sub(Expr a, float b) {
        return sub(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr sub(Expr a, double b) {
        return sub(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr sub(int a, Expr b) {
        return sub(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr sub(long a, Expr b) {
        return sub(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr sub(float a, Expr b) {
        return sub(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic subtraction operator.
     * This method works equivalently to the {@code -} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr sub(double a, Expr b) {
        return sub(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic multiplication operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code *} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr mul(Expr a, Expr b);

    /**
     * The arithmetic multiplication operator.
     * This method works equivalently to the {@code *} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr mul(Expr a, int b) {
        return mul(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic multiplication operator.
     * This method works equivalently to the {@code *} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr mul(Expr a, long b) {
        return mul(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic multiplication operator.
     * This method works equivalently to the {@code *} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr mul(Expr a, float b) {
        return mul(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic multiplication operator.
     * This method works equivalently to the {@code *} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr mul(Expr a, double b) {
        return mul(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic division operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr div(Expr a, Expr b);

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr div(Expr a, int b) {
        return div(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr div(Expr a, long b) {
        return div(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr div(Expr a, float b) {
        return div(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr div(Expr a, double b) {
        return div(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr div(int a, Expr b) {
        return div(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr div(long a, Expr b) {
        return div(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr div(float a, Expr b) {
        return div(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic division operator.
     * This method works equivalently to the {@code /} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr div(double a, Expr b) {
        return div(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic remainder operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    Expr rem(Expr a, Expr b);

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr rem(Expr a, int b) {
        return rem(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr rem(Expr a, long b) {
        return rem(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr rem(Expr a, float b) {
        return rem(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument (must not be {@code null})
     * @param b the second argument
     * @return the operation result (not {@code null})
     */
    default Expr rem(Expr a, double b) {
        return rem(a, Const.of(b, a.typeKind()));
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr rem(int a, Expr b) {
        return rem(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr rem(long a, Expr b) {
        return rem(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr rem(float a, Expr b) {
        return rem(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic remainder operator.
     * This method works equivalently to the {@code %} operator in Java.
     *
     * @param a the first argument
     * @param b the second argument (must not be {@code null})
     * @return the operation result (not {@code null})
     */
    default Expr rem(double a, Expr b) {
        return rem(Const.of(a, b.typeKind()), b);
    }

    /**
     * The arithmetic negation operator.
     * This method works equivalently to the {@code -} unary operator in Java.
     *
     * @param a the argument
     * @return the operation result (must not be {@code null})
     */
    Expr neg(Expr a);

    // arithmetic-assign

    /**
     * Add the argument to the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void addAssign(Assignable var, Expr arg);

    /**
     * Subtract the argument from the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void subAssign(Assignable var, Expr arg);

    /**
     * Multiply the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void mulAssign(Assignable var, Expr arg);

    /**
     * Divide the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void divAssign(Assignable var, Expr arg);

    /**
     * Divide the argument with the variable value and assign the remainder back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void remAssign(Assignable var, Expr arg);

    // bitwise-assign

    /**
     * Bitwise-AND the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void andAssign(Assignable var, Expr arg);

    /**
     * Bitwise-OR the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void orAssign(Assignable var, Expr arg);

    /**
     * Bitwise-XOR (exclusive OR) the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void xorAssign(Assignable var, Expr arg);

    /**
     * Bitwise-left-shift the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void shlAssign(Assignable var, Expr arg);

    /**
     * Arithmetically bitwise-right-shift the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void shrAssign(Assignable var, Expr arg);

    /**
     * Logically bitwise-right-shift the argument with the variable value and assign it back.
     *
     * @param var the variable (must not be {@code null})
     * @param arg the argument value (must not be {@code null})
     */
    void ushrAssign(Assignable var, Expr arg);

    // logical

    /**
     * {@return an expression which is the logical (boolean) opposite of the input expression}
     *
     * @param a the input expression (must not be {@code null})
     */
    default Expr logicalNot(Expr a) {
        return a.typeKind() == TypeKind.BOOLEAN ? xor(a, Const.of(1)) : eq(a, Const.of(0, a.typeKind()));
    }

    /**
     * Perform a short-circuiting logical-OR operation (the {@code ||} operator).
     *
     * @param cond the condition to evaluate (must not be {@code null})
     * @param other the expression to evaluate if {@code cond} is {@code false}
     * @return the boolean result of the operation (not {@code null})
     */
    default Expr logicalOr(Expr cond, Consumer<BlockCreator> other) {
        return cond(CD_boolean, cond, bc -> bc.yield(Const.of(true)), other);
    }

    /**
     * Perform a short-circuiting logical-AND operation (the {@code &&} operator).
     *
     * @param cond the condition to evaluate (must not be {@code null})
     * @param other the expression to evaluate if {@code cond} is {@code true}
     * @return the boolean result of the operation (not {@code null})
     */
    default Expr logicalAnd(Expr cond, Consumer<BlockCreator> other) {
        return cond(CD_boolean, cond, other, bc -> bc.yield(Const.of(false)));
    }

    // conditional

    /**
     * Evaluate a conditional expression (the {@code ?:} operator).
     *
     * @param type the result type (must not be {@code null})
     * @param cond the boolean condition to evaluate (must not be {@code null})
     * @param ifTrue the expression to yield if the value was {@code true} (must not be {@code null})
     * @param ifFalse the expression to yield if the value was {@code false} (must not be {@code null})
     * @return the result value (must not be {@code null})
     */
    default Expr cond(Class<?> type, Expr cond, Consumer<BlockCreator> ifTrue, Consumer<BlockCreator> ifFalse) {
        return cond(Util.classDesc(type), cond, ifTrue, ifFalse);
    }

    /**
     * Evaluate a conditional expression (the {@code ?:} operator).
     *
     * @param type the result type (must not be {@code null})
     * @param cond the boolean condition to evaluate (must not be {@code null})
     * @param ifTrue the expression to yield if the value was {@code true} (must not be {@code null})
     * @param ifFalse the expression to yield if the value was {@code false} (must not be {@code null})
     * @return the result value (must not be {@code null})
     */
    Expr cond(ClassDesc type, Expr cond, Consumer<BlockCreator> ifTrue, Consumer<BlockCreator> ifFalse);

    // lambda

    /**
     * Construct a lambda instance with the given type.
     * <p>
     * Note that all values used in the lambda but created outside of it, including {@code this},
     * must be captured using {@link LambdaCreator#capture(String, Expr)} or {@link LambdaCreator#capture(Var)}.
     *
     * @param type the type of the lambda (must not be {@code null})
     * @param builder the builder for the lambda body (must not be {@code null})
     * @return the lambda object (not {@code null})
     */
    default Expr lambda(Class<?> type, Consumer<LambdaCreator> builder) {
        return lambda(Util.findSam(type), Util.classDesc(type), builder);
    }

    /**
     * Construct a lambda instance with the given type.
     * <p>
     * Note that all values used in the lambda but created outside of it, including {@code this},
     * must be captured using {@link LambdaCreator#capture(String, Expr)} or {@link LambdaCreator#capture(Var)}.
     *
     * @param sam the descriptor of the single abstract method of the lambda (must not be {@code null})
     * @param builder the builder for the lambda body (must not be {@code null})
     * @return the lambda object (not {@code null})
     */
    default Expr lambda(MethodDesc sam, Consumer<LambdaCreator> builder) {
        return lambda(sam, sam.owner(), builder);
    }

    /**
     * Construct a lambda instance with the given type.
     * <p>
     * Note that all values used in the lambda but created outside of it, including {@code this},
     * must be captured using {@link LambdaCreator#capture(String, Expr)} or {@link LambdaCreator#capture(Var)}.
     *
     * @param sam the descriptor of the single abstract method of the lambda (must not be {@code null})
     * @param owner the type of the final lambda (must not be {@code null})
     * @param builder the builder for the lambda body (must not be {@code null})
     * @return the lambda object (not {@code null})
     */
    Expr lambda(MethodDesc sam, ClassDesc owner, Consumer<LambdaCreator> builder);

    // anon class

    /**
     * Create a new anonymous class instance.
     * Unlike Java anonymous classes, the anonymous class definition created here may implement additional interfaces.
     * The type of the returned instance is the anonymous class type.
     * <p>
     * Note that all values used in the anonymous class but created outside of it, including {@code this},
     * must be captured using {@link AnonymousClassCreator#capture(String, Expr)} or {@link AnonymousClassCreator#capture(Var)}.
     *
     * @param superCtor the superclass constructor to invoke (must not be {@code null})
     * @param args the constructor arguments (must not be {@code null})
     * @param builder the builder for the anonymous class (must not be {@code null})
     * @return the anonymous class instance (not {@code null})
     */
    Expr newAnonymousClass(ConstructorDesc superCtor, List<? extends Expr> args, Consumer<AnonymousClassCreator> builder);

    /**
     * Create a new anonymous class instance which implements the given interface.
     * The type of the returned instance is the anonymous class type.
     * <p>
     * Note that all values used in the anonymous class but created outside of it, including {@code this},
     * must be captured using {@link AnonymousClassCreator#capture(String, Expr)} or {@link AnonymousClassCreator#capture(Var)}.
     *
     * @param interface_ the interface to implement (must not be {@code null})
     * @param builder the builder for the anonymous class (must not be {@code null})
     * @return the anonymous class instance (not {@code null})
     */
    default Expr newAnonymousClass(ClassDesc interface_, Consumer<AnonymousClassCreator> builder) {
        return newAnonymousClass(
                ConstructorDesc.of(Object.class),
                List.of(),
                cc -> {
                    cc.implements_(interface_);
                    builder.accept(cc);
                });
    }

    /**
     * Create a new anonymous class instance which extends the given class or implements the given interface.
     * If the given supertype is a class, it has to have a zero-parameter constructor.
     * The type of the returned instance is the anonymous class type.
     * <p>
     * Note that all values used in the anonymous class but created outside of it, including {@code this},
     * must be captured using {@link AnonymousClassCreator#capture(String, Expr)} or {@link AnonymousClassCreator#capture(Var)}.
     *
     * @param supertype the supertype to extend or implement (must not be {@code null})
     * @param builder the builder for the anonymous class (must not be {@code null})
     * @return the anonymous class instance (not {@code null})
     */
    default Expr newAnonymousClass(Class<?> supertype, Consumer<AnonymousClassCreator> builder) {
        if (supertype.isInterface()) {
            return newAnonymousClass(Util.classDesc(supertype), builder);
        } else {
            final ConstructorDesc superCtor = ConstructorDesc.of(supertype);
            return newAnonymousClass(superCtor, List.of(), builder);
        }
    }

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
    Expr cast(Expr a, GenericType toType);

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
    default Expr cast(Expr a, Class<?> toType) {
        return cast(a, Util.classDesc(toType));
    }

    /**
     * Cast an object value to the given type without a type check.
     * If the cast is invalid, then class validation will fail.
     *
     * @param a the value to cast (must not be {@code null})
     * @param toType the type to cast to (must not be {@code null})
     * @return the cast value (not {@code null})
     * @see #cast(Expr, ClassDesc)
     */
    Expr uncheckedCast(Expr a, GenericType toType);

    /**
     * Cast an object value to the given type without a type check.
     * If the cast is invalid, then class validation will fail.
     *
     * @param a the value to cast (must not be {@code null})
     * @param toType the type to cast to (must not be {@code null})
     * @return the cast value (not {@code null})
     * @see #cast(Expr, ClassDesc)
     */
    Expr uncheckedCast(Expr a, ClassDesc toType);

    /**
     * Cast an object value to the given type without a type check.
     * If the cast is invalid, then class validation will fail.
     *
     * @param a the value to cast (must not be {@code null})
     * @param toType the type to cast to (must not be {@code null})
     * @return the cast value (not {@code null})
     * @see #cast(Expr, Class)
     */
    default Expr uncheckedCast(Expr a, Class<?> toType) {
        return uncheckedCast(a, Util.classDesc(toType));
    }

    /**
     * Box the given primitive value into its corresponding box type.
     *
     * @param a the primitive value (must not be {@code null})
     * @return the boxed value (not {@code null})
     */
    Expr box(Expr a);

    /**
     * Unbox the given boxed value into its corresponding primitive type.
     *
     * @param a the boxed value (must not be {@code null})
     * @return the primitive value (not {@code null})
     */
    Expr unbox(Expr a);

    // object

    /**
     * Test whether the given object implements the given type.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to test against (must not be {@code null})
     * @return the boolean result of the check (not {@code null})
     */
    default Expr instanceOf(Expr obj, Class<?> type) {
        return instanceOf(obj, Util.classDesc(type));
    }

    /**
     * Test whether the given object implements the given type.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to test against (must not be {@code null})
     * @return the boolean result of the check (not {@code null})
     */
    default Expr instanceOf(Expr obj, ClassDesc type) {
        return instanceOf(obj, GenericType.of(type));
    }

    /**
     * Test whether the given object implements the given type.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to test against (must not be {@code null})
     * @return the boolean result of the check (not {@code null})
     */
    Expr instanceOf(Expr obj, GenericType type);

    /**
     * Construct a new instance.
     *
     * @param genericType the generic type of the new object (must not be {@code null})
     * @param ctor the constructor to call (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    Expr new_(GenericType genericType, ConstructorDesc ctor, List<? extends Expr> args);

    /**
     * Construct a new instance.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    Expr new_(ConstructorDesc ctor, List<? extends Expr> args);

    /**
     * Construct a new instance.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ConstructorDesc ctor) {
        return new_(ctor, List.of());
    }

    /**
     * Construct a new instance.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param arg0 the sole arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ConstructorDesc ctor, Expr arg0) {
        return new_(ctor, List.of(arg0));
    }

    /**
     * Construct a new instance.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param arg0 the first argument to pass to the constructor (must not be {@code null})
     * @param arg1 the second argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ConstructorDesc ctor, Expr arg0, Expr arg1) {
        return new_(ctor, List.of(arg0, arg1));
    }

    /**
     * Construct a new instance.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ConstructorDesc ctor, Expr... args) {
        return new_(ctor, List.of(args));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ClassDesc type, List<? extends Expr> args) {
        return new_(ConstructorDesc.of(type, args.stream().map(Expr::type).toList()), args);
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ClassDesc type) {
        return new_(type, List.of());
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the sole argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ClassDesc type, Expr arg0) {
        return new_(type, List.of(arg0));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the first argument to pass to the constructor (must not be {@code null})
     * @param arg1 the second argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ClassDesc type, Expr arg0, Expr arg1) {
        return new_(type, List.of(arg0, arg1));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(ClassDesc type, Expr... args) {
        return new_(type, List.of(args));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(GenericType type, List<? extends Expr> args) {
        return new_(ConstructorDesc.of(type.desc(), args.stream().map(Expr::type).toList()), args);
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(GenericType type) {
        return new_(type, List.of());
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the sole argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(GenericType type, Expr arg0) {
        return new_(type, List.of(arg0));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the first argument to pass to the constructor (must not be {@code null})
     * @param arg1 the second argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(GenericType type, Expr arg0, Expr arg1) {
        return new_(type, List.of(arg0, arg1));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(GenericType type, Expr... args) {
        return new_(type, List.of(args));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(Class<?> type, List<? extends Expr> args) {
        return new_(Util.classDesc(type), args);
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(Class<?> type) {
        return new_(type, List.of());
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the sole argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(Class<?> type, Expr arg0) {
        return new_(type, List.of(arg0));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param arg0 the first argument to pass to the constructor (must not be {@code null})
     * @param arg1 the second argument to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(Class<?> type, Expr arg0, Expr arg1) {
        return new_(type, List.of(arg0, arg1));
    }

    /**
     * Construct a new instance.
     *
     * @param type the type to construct (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the new object (not {@code null})
     */
    default Expr new_(Class<?> type, Expr... args) {
        return new_(type, List.of(args));
    }

    // invocation

    /**
     * Invoke a static method.
     *
     * @param genericReturnType the generic return type (must not be {@code null})
     * @param method the method to call (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeStatic(GenericType genericReturnType, MethodDesc method, List<? extends Expr> args);

    /**
     * Invoke a static method.
     *
     * @param method the method to call (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeStatic(MethodDesc method, List<? extends Expr> args);

    /**
     * Invoke a static method.
     *
     * @param method the method to call (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeStatic(MethodDesc method) {
        return invokeStatic(method, List.of());
    }

    /**
     * Invoke a static method.
     *
     * @param method the method to call (must not be {@code null})
     * @param arg0 the sole argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeStatic(MethodDesc method, Expr arg0) {
        return invokeStatic(method, List.of(arg0));
    }

    /**
     * Invoke a static method.
     *
     * @param method the method to call (must not be {@code null})
     * @param arg0 the first argument to pass to the method (must not be {@code null})
     * @param arg1 the second argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeStatic(MethodDesc method, Expr arg0, Expr arg1) {
        return invokeStatic(method, List.of(arg0, arg1));
    }

    /**
     * Invoke a static method.
     *
     * @param method the method to call (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeStatic(MethodDesc method, Expr... args) {
        return invokeStatic(method, List.of(args));
    }

    /**
     * Invoke a virtual method.
     *
     * @param genericReturnType the generic return type (must not be {@code null})
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeVirtual(GenericType genericReturnType, MethodDesc method, Expr instance, List<? extends Expr> args);

    /**
     * Invoke a virtual method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeVirtual(MethodDesc method, Expr instance, List<? extends Expr> args);

    /**
     * Invoke a virtual method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeVirtual(MethodDesc method, Expr instance) {
        return invokeVirtual(method, instance, List.of());
    }

    /**
     * Invoke a virtual method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the sole argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeVirtual(MethodDesc method, Expr instance, Expr arg0) {
        return invokeVirtual(method, instance, List.of(arg0));
    }

    /**
     * Invoke a virtual method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the first argument to pass to the method (must not be {@code null})
     * @param arg1 the second argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeVirtual(MethodDesc method, Expr instance, Expr arg0, Expr arg1) {
        return invokeVirtual(method, instance, List.of(arg0, arg1));
    }

    /**
     * Invoke a virtual method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeVirtual(MethodDesc method, Expr instance, Expr... args) {
        return invokeVirtual(method, instance, List.of(args));
    }

    /**
     * Invoke a method using "special" semantics.
     *
     * @param genericReturnType the generic return type (must not be {@code null})
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeSpecial(GenericType genericReturnType, MethodDesc method, Expr instance, List<? extends Expr> args);

    /**
     * Invoke a method using "special" semantics.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeSpecial(MethodDesc method, Expr instance, List<? extends Expr> args);

    /**
     * Invoke a method using "special" semantics.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeSpecial(MethodDesc method, Expr instance) {
        return invokeSpecial(method, instance, List.of());
    }

    /**
     * Invoke a method using "special" semantics.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the sole argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeSpecial(MethodDesc method, Expr instance, Expr arg0) {
        return invokeSpecial(method, instance, List.of(arg0));
    }

    /**
     * Invoke a method using "special" semantics.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the first argument to pass to the method (must not be {@code null})
     * @param arg1 the second argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeSpecial(MethodDesc method, Expr instance, Expr arg0, Expr arg1) {
        return invokeSpecial(method, instance, List.of(arg0, arg1));
    }

    /**
     * Invoke a method using "special" semantics.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeSpecial(MethodDesc method, Expr instance, Expr... args) {
        return invokeSpecial(method, instance, List.of(args));
    }

    /**
     * Invoke a constructor using "special" semantics.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the constructor call result (not {@code null}, usually {@link Const#ofVoid()})
     */
    Expr invokeSpecial(ConstructorDesc ctor, Expr instance, List<? extends Expr> args);

    /**
     * Invoke a constructor using "special" semantics.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @return the constructor call result (not {@code null}, usually {@link Const#ofVoid()})
     */
    default Expr invokeSpecial(ConstructorDesc ctor, Expr instance) {
        return invokeSpecial(ctor, instance, List.of());
    }

    /**
     * Invoke a constructor using "special" semantics.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the sole argument to pass to the constructor (must not be {@code null})
     * @return the constructor call result (not {@code null}, usually {@link Const#ofVoid()})
     */
    default Expr invokeSpecial(ConstructorDesc ctor, Expr instance, Expr arg0) {
        return invokeSpecial(ctor, instance, List.of(arg0));
    }

    /**
     * Invoke a constructor using "special" semantics.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the first argument to pass to the constructor (must not be {@code null})
     * @param arg1 the second argument to pass to the constructor (must not be {@code null})
     * @return the constructor call result (not {@code null}, usually {@link Const#ofVoid()})
     */
    default Expr invokeSpecial(ConstructorDesc ctor, Expr instance, Expr arg0, Expr arg1) {
        return invokeSpecial(ctor, instance, List.of(arg0, arg1));
    }

    /**
     * Invoke a constructor using "special" semantics.
     *
     * @param ctor the constructor to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the constructor (must not be {@code null})
     * @return the constructor call result (not {@code null}, usually {@link Const#ofVoid()})
     */
    default Expr invokeSpecial(ConstructorDesc ctor, Expr instance, Expr... args) {
        return invokeSpecial(ctor, instance, List.of(args));
    }

    /**
     * Invoke an interface method.
     *
     * @param genericReturnType the generic return type (must not be {@code null})
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    Expr invokeInterface(GenericType genericReturnType, MethodDesc method, Expr instance, List<? extends Expr> args);

    /**
     * Invoke an interface method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeInterface(MethodDesc method, Expr instance, List<? extends Expr> args) {
        return invokeInterface(GenericType.of(method.returnType()), method, instance, args);
    }

    /**
     * Invoke an interface method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeInterface(MethodDesc method, Expr instance) {
        return invokeInterface(method, instance, List.of());
    }

    /**
     * Invoke an interface method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the sole argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeInterface(MethodDesc method, Expr instance, Expr arg0) {
        return invokeInterface(method, instance, List.of(arg0));
    }

    /**
     * Invoke an interface method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param arg0 the first argument to pass to the method (must not be {@code null})
     * @param arg1 the second argument to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeInterface(MethodDesc method, Expr instance, Expr arg0, Expr arg1) {
        return invokeInterface(method, instance, List.of(arg0, arg1));
    }

    /**
     * Invoke an interface method.
     *
     * @param method the method to call (must not be {@code null})
     * @param instance the invocation target (must not be {@code null})
     * @param args the arguments to pass to the method (must not be {@code null})
     * @return the method call result (not {@code null})
     */
    default Expr invokeInterface(MethodDesc method, Expr instance, Expr... args) {
        return invokeInterface(method, instance, List.of(args));
    }

    Expr invokeDynamic(DynamicCallSiteDesc callSiteDesc, List<? extends Expr> args);

    default Expr invokeDynamic(DynamicCallSiteDesc callSiteDesc, Expr... args) {
        return invokeDynamic(callSiteDesc, List.of(args));
    }

    // control flow

    /**
     * Build a for-each loop over an array or collection.
     *
     * @param items the array or collection (must not be {@code null})
     * @param builder the builder for the loop body (must not be {@code null})
     */
    void forEach(Expr items, BiConsumer<BlockCreator, ? super LocalVar> builder);

    /**
     * Create a nested block.
     *
     * @param nested the builder for the block body (must not be {@code null})
     */
    void block(Consumer<BlockCreator> nested);

    /**
     * Create a block expression of given {@code type}. The block must {@link #yield(Expr)} its result.
     *
     * @param type the output type (must not be {@code null})
     * @param nested the builder for the block body (must not be {@code null})
     * @return the returned value (not {@code null})
     */
    Expr blockExpr(ClassDesc type, Consumer<BlockCreator> nested);

    /**
     * Create a block expression of given {@code type}. The block must {@link #yield(Expr)} its result.
     *
     * @param type the output type (must not be {@code null})
     * @param nested the builder for the block body (must not be {@code null})
     * @return the returned value (not {@code null})
     */
    default Expr blockExpr(Class<?> type, Consumer<BlockCreator> nested) {
        return blockExpr(Util.classDesc(type), nested);
    }

    /**
     * If the given object is an instance of the given type, then execute the block with the narrowed object.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifTrue the builder for a block to run if the type was successfully narrowed (must not be {@code null})
     */
    default void ifInstanceOf(Expr obj, Class<?> type, BiConsumer<BlockCreator, ? super LocalVar> ifTrue) {
        ifInstanceOf(obj, Util.classDesc(type), ifTrue);
    }

    /**
     * If the given object is an instance of the given type, then execute the block with the narrowed object.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifTrue the builder for a block to run if the type was successfully narrowed (must not be {@code null})
     */
    void ifInstanceOf(Expr obj, ClassDesc type, BiConsumer<BlockCreator, ? super LocalVar> ifTrue);

    /**
     * If the given object is <em>not</em> an instance of the given type, then execute the given block.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifFalse the builder for a block to run if the type did not match (must not be {@code null})
     */
    default void ifNotInstanceOf(Expr obj, Class<?> type, Consumer<BlockCreator> ifFalse) {
        ifNotInstanceOf(obj, Util.classDesc(type), ifFalse);
    }

    /**
     * If the given object is <em>not</em> an instance of the given type, then execute the given block.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifFalse the builder for a block to run if the type did not match (must not be {@code null})
     */
    void ifNotInstanceOf(Expr obj, ClassDesc type, Consumer<BlockCreator> ifFalse);

    /**
     * If the given object is an instance of the given type, then execute the first block with the narrowed object,
     * otherwise execute the other block.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifTrue the builder for a block to run if the type was successfully narrowed (must not be {@code null})
     * @param ifFalse the builder for a block to run if the type did not match (must not be {@code null})
     */
    default void ifInstanceOfElse(Expr obj, Class<?> type, BiConsumer<BlockCreator, ? super LocalVar> ifTrue,
            Consumer<BlockCreator> ifFalse) {
        ifInstanceOfElse(obj, Util.classDesc(type), ifTrue, ifFalse);
    }

    /**
     * If the given object is an instance of the given type, then execute the first block with the narrowed object,
     * otherwise execute the other block.
     *
     * @param obj the object to test (must not be {@code null})
     * @param type the type to check for (must not be {@code null})
     * @param ifTrue the builder for a block to run if the type was successfully narrowed (must not be {@code null})
     * @param ifFalse the builder for a block to run if the type did not match (must not be {@code null})
     */
    void ifInstanceOfElse(Expr obj, ClassDesc type, BiConsumer<BlockCreator, ? super LocalVar> ifTrue,
            Consumer<BlockCreator> ifFalse);

    /**
     * A general {@code if} conditional.
     *
     * @param cond the boolean condition expression (must not be {@code null})
     * @param whenTrue the builder for a block to execute if the condition is true (must not be {@code null})
     */
    void if_(Expr cond, Consumer<BlockCreator> whenTrue);

    /**
     * An inverted {@code if} conditional.
     *
     * @param cond the boolean condition expression (must not be {@code null})
     * @param whenFalse the builder for a block to execute if the condition is false (must not be {@code null})
     */
    void ifNot(Expr cond, Consumer<BlockCreator> whenFalse);

    /**
     * A general {@code if}-{@code else} conditional.
     *
     * @param cond the boolean condition expression (must not be {@code null})
     * @param whenTrue the builder for a block to execute if the condition is true (must not be {@code null})
     * @param whenFalse the builder for a block to execute if the condition is false (must not be {@code null})
     */
    void ifElse(Expr cond, Consumer<BlockCreator> whenTrue, Consumer<BlockCreator> whenFalse);

    /**
     * An {@code if (obj == null)} conditional.
     *
     * @param obj the object reference to test (must not be {@code null})
     * @param whenTrue the builder for a block to execute if the object reference is null (must not be {@code null})
     */
    default void ifNull(Expr obj, Consumer<BlockCreator> whenTrue) {
        if_(eq(obj, Const.ofNull(obj.type())), whenTrue);
    }

    /**
     * An {@code if (obj != null)} conditional.
     *
     * @param obj the object reference to test (must not be {@code null})
     * @param whenTrue the builder for a block to execute if the object reference is not null (must not be {@code null})
     */
    default void ifNotNull(Expr obj, Consumer<BlockCreator> whenTrue) {
        if_(ne(obj, Const.ofNull(obj.type())), whenTrue);
    }

    /**
     * Construct a {@code switch} statement for {@code enum} constants.
     *
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     */
    default void switchEnum(Expr val, Consumer<SwitchCreator> builder) {
        switchEnum(CD_void, val, builder);
    }

    /**
     * Construct a {@code switch} expression for {@code enum} constants.
     *
     * @param outputType the output type of this {@code switch} (must not be {@code null})
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     * @return the switch expression result (not {@code null})
     */
    Expr switchEnum(ClassDesc outputType, Expr val, Consumer<SwitchCreator> builder);

    /**
     * Construct a {@code switch} expression for {@code enum} constants.
     *
     * @param outputType the output type of this {@code switch} (must not be {@code null})
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     * @return the switch expression result (not {@code null})
     */
    default Expr switchEnum(Class<?> outputType, Expr val, Consumer<SwitchCreator> builder) {
        return switchEnum(Util.classDesc(outputType), val, builder);
    }

    /**
     * Construct a {@code switch} statement.
     * The type of the switch value must be of one of these supported types:
     * <ul>
     * <li>{@code int} (which includes {@code byte}, {@code char}, {@code short}, and {@code boolean})</li>
     * <li>{@code long}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.Class}</li>
     * </ul>
     * The type of the {@code switch} creator depends on the type of the value.
     * For {@code enum} switches, use {@link #switchEnum(Expr, Consumer)}.
     *
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     */
    default void switch_(Expr val, Consumer<SwitchCreator> builder) {
        switch_(CD_void, val, builder);
    }

    /**
     * Construct a {@code switch} expression.
     * The type of the switch value must be of one of these supported types:
     * <ul>
     * <li>{@code int} (which includes {@code byte}, {@code char}, {@code short}, and {@code boolean})</li>
     * <li>{@code long}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.Class}</li>
     * </ul>
     * The type of the {@code switch} creator depends on the type of the value.
     * For {@code enum} switches, use {@link #switchEnum(Expr, Consumer)}.
     *
     * @param outputType the output type of this {@code switch} (must not be {@code null})
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     * @return the switch expression result (not {@code null})
     */
    Expr switch_(ClassDesc outputType, Expr val, Consumer<SwitchCreator> builder);

    /**
     * Construct a {@code switch} expression.
     * The type of the switch value must be of one of these supported types:
     * <ul>
     * <li>{@code int} (which includes {@code byte}, {@code char}, {@code short}, and {@code boolean})</li>
     * <li>{@code long}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.Class}</li>
     * </ul>
     * The type of the {@code switch} creator depends on the type of the value.
     * For {@code enum} switches, use {@link #switchEnum(Expr, Consumer)}.
     *
     * @param outputType the output type of this {@code switch} (must not be {@code null})
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     * @return the switch expression result (not {@code null})
     */
    default Expr switch_(Class<?> outputType, Expr val, Consumer<SwitchCreator> builder) {
        return switch_(Util.classDesc(outputType), val, builder);
    }

    /**
     * Exit the given enclosing block.
     * Blocks that have a non-{@code void} {@linkplain #type() type}
     * may not be the target of a {@code break}.
     *
     * @param outer the block to break (must not be {@code null})
     */
    void break_(BlockCreator outer);

    /**
     * Resume the next iteration of an enclosing loop.
     * A block creator is a loop if it was created using one of:
     * <ul>
     * <li>{@link #loop(Consumer)}</li>
     * <li>{@link #while_(Consumer, Consumer)}</li>
     * <li>{@link #doWhile(Consumer, Consumer)}</li>
     * <li>{@link #forEach(Expr, BiConsumer)}</li>
     * </ul>
     * To repeat an iteration, see {@link #goto_(BlockCreator)}.
     *
     * @param loop the loop to continue (must not be {@code null})
     * @throws IllegalArgumentException if the given block creator does not correspond to a loop
     */
    void continue_(BlockCreator loop);

    /**
     * Jump to the start of the given enclosing block.
     * Blocks which are part of an expression-accepting operation may
     * not be the target of {@code goto_}.
     *
     * @param outer the block to restart (must not be {@code null})
     */
    void goto_(BlockCreator outer);

    /**
     * Jump to the start of this block.
     */
    default void gotoStart() {
        goto_(this);
    }

    /**
     * Jump to a specific case in the given enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #gotoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    void gotoCase(SwitchCreator switch_, Const case_);

    /**
     * Jump to a specific case in the given enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #gotoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    default void gotoCase(SwitchCreator switch_, int case_) {
        gotoCase(switch_, Const.of(case_));
    }

    /**
     * Jump to a specific case in the given enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #gotoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    default void gotoCase(SwitchCreator switch_, String case_) {
        gotoCase(switch_, Const.of(case_));
    }

    /**
     * Jump to a specific case in the given enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #gotoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    default void gotoCase(SwitchCreator switch_, Enum<?> case_) {
        gotoCase(switch_, Const.of(case_));
    }

    /**
     * Jump to a specific case in the given enclosing {@code switch}.
     * If the case is not represented in the {@code switch}, then
     * it will be as if {@link #gotoDefault(SwitchCreator)} was called instead.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     * @param case_ the constant representing the case to go to (must not be {@code null})
     */
    default void gotoCase(SwitchCreator switch_, Class<?> case_) {
        gotoCase(switch_, Const.of(case_));
    }

    /**
     * Jump to the default case in the given enclosing {@code switch}.
     *
     * @param switch_ the enclosing {@code switch} (must not be {@code null})
     */
    void gotoDefault(SwitchCreator switch_);

    /**
     * Enter a loop.
     * The loop may be exited by calling {@link #break_(BlockCreator)} on the loop's block.
     *
     * @param body the loop body (must not be {@code null})
     */
    void loop(Consumer<BlockCreator> body);

    /**
     * Enter a {@code while} loop.
     * The loop may be exited early by calling {@link #break_(BlockCreator)} on the loop's block.
     *
     * @param cond the condition which is evaluated at the top of the block (must not be {@code null})
     * @param body the loop body (must not be {@code null})
     */
    void while_(Consumer<BlockCreator> cond, Consumer<BlockCreator> body);

    /**
     * Enter a {@code do}-{@code while} loop.
     * The loop may be exited early by calling {@link #break_(BlockCreator)} on the loop's block.
     *
     * @param body the loop body (must not be {@code null})
     * @param cond the condition which is evaluated at the bottom of the block (must not be {@code null})
     */
    void doWhile(Consumer<BlockCreator> body, Consumer<BlockCreator> cond);

    /**
     * Enter a {@code try} block.
     *
     * @param body the handler to produce the {@code try}, {@code catch}, and/or {@code finally} sections
     */
    void try_(Consumer<TryCreator> body);

    /**
     * Open a resource and run the given body with the resource, automatically closing it at the end.
     *
     * @param resource the resource to automatically close (must not be {@code null})
     * @param body the creator for the body of the resource operation (must not be {@code null})
     */
    void autoClose(Expr resource, BiConsumer<BlockCreator, ? super LocalVar> body);

    /**
     * Open a resource and run the given body with the resource, automatically closing it at the end.
     *
     * @param resource the resource to automatically close (must not be {@code null})
     * @param body the creator for the body of the resource operation (must not be {@code null})
     */
    void autoClose(LocalVar resource, Consumer<BlockCreator> body);

    /**
     * Create a {@code synchronized} block. When the given {@code body} is executed,
     * the monitor of given {@code monitor} is locked.
     *
     * @param monitor the expression of the object whose monitor is to be locked (must not be {@code null})
     * @param body the creator for the body of the block (must not be {@code null})
     */
    void synchronized_(Expr monitor, Consumer<BlockCreator> body);

    /**
     * Create a block which holds a {@link Lock}. When the given {@code body} is executed,
     * the given {@code lock} is held.
     *
     * @param jucLock the expression of the lock object to be locked (must not be {@code null})
     * @param body the creator for the body of the block (must not be {@code null})
     */
    void locked(Expr jucLock, Consumer<BlockCreator> body);

    // returning normally

    /**
     * Return from the current method.
     */
    void return_();

    /**
     * Return from the current method.
     *
     * @param val the return value (must not be {@code null})
     */
    void return_(Expr val);

    /**
     * Return from the current method.
     *
     * @param val the return value (must not be {@code null})
     */
    default void return_(String val) {
        return_(Const.of(val));
    }

    /**
     * Return from the current method.
     *
     * @param val the return value (must not be {@code null})
     */
    default void return_(Class<?> val) {
        return_(Const.of(val));
    }

    /**
     * Return from the current method.
     *
     * @param val the return value
     */
    default void return_(boolean val) {
        return_(Const.of(val));
    }

    /**
     * Return from the current method.
     *
     * @param val the return value
     */
    default void return_(int val) {
        return_(Const.of(val));
    }

    /**
     * Return {@code true} from the current method.
     */
    default void returnTrue() {
        return_(true);
    }

    /**
     * Return {@code false} from the current method.
     */
    default void returnFalse() {
        return_(false);
    }

    /**
     * Return {@code 0} from the current method.
     */
    default void returnIntZero() {
        return_(Const.of(0));
    }

    /**
     * Return {@code null} from the current method.
     */
    void returnNull();

    // throwing

    /**
     * Throw the given exception object.
     *
     * @param val the exception object (must not be {@code null})
     */
    void throw_(Expr val);

    /**
     * Throw a new exception of the given type.
     *
     * @param type the exception type (must not be {@code null})
     */
    default void throw_(ClassDesc type) {
        throw_(new_(type));
    }

    /**
     * Throw a new exception of the given type with a message.
     *
     * @param type the exception type (must not be {@code null})
     * @param message the message (must not be {@code null})
     */
    default void throw_(ClassDesc type, String message) {
        throw_(new_(type, Const.of(message)));
    }

    /**
     * Throw a new exception of the given type with a message.
     *
     * @param type the exception type (must not be {@code null})
     * @param message the message (must not be {@code null})
     */
    default void throw_(ClassDesc type, Expr message) {
        throw_(new_(type, message));
    }

    /**
     * Throw a new exception of the given type.
     *
     * @param type the exception type (must not be {@code null})
     */
    default void throw_(Class<? extends Throwable> type) {
        throw_(Util.classDesc(type));
    }

    /**
     * Throw a new exception of the given type with a message.
     *
     * @param type the exception type (must not be {@code null})
     * @param message the message
     */
    default void throw_(Class<? extends Throwable> type, String message) {
        if (message == null) {
            throw_(type);
        } else {
            throw_(Util.classDesc(type), message);
        }
    }

    /**
     * Throw a new exception of the given type with a message.
     *
     * @param type the exception type (must not be {@code null})
     * @param message the message
     */
    default void throw_(Class<? extends Throwable> type, Expr message) {
        if (message == null) {
            throw_(type);
        } else {
            throw_(Util.classDesc(type), message);
        }
    }

    // useful helpers/utilities

    /**
     * Generates call to one of the static methods to compute a hash code of given expression.
     * That is:
     * <ul>
     * <li>for reference types, {@code Objects.hashCode(expr)}</li>
     * <li>for primitive types, the static {@code hashCode(expr)} method on the corresponding wrapper class</li>
     * </ul>
     *
     * @param expr the expression, which can be of any type (must not be {@code null})
     * @return an {@code int} expression representing the hash code of given expression (not {@code null})
     */
    Expr objHashCode(Expr expr);

    /**
     * Generates call to the {@code Objects#equals(a, b)} method if at least one
     * of the given expressions is of a reference type (boxing the other if primitive).
     * If both expressions are of a primitive type, this is equivalent to {@link #eq(Expr, Expr)}.
     *
     * @param a the first expression (must not be {@code null})
     * @param b the second expression (must not be {@code null})
     * @return a {@code boolean} expression representing the equality between the two values (not {@code null})
     */
    Expr objEquals(Expr a, Expr b);

    /**
     * Generates call to one of the {@code String#valueOf(expr)} overloads, based on the type of the argument.
     *
     * @param expr the expression, which can be of any type
     * @return a {@code String} expression representing the string value of given expression (not {@code null})
     */
    Expr objToString(Expr expr);

    /**
     * Generates call to one of the {@code Arrays#hashCode(expr)} overloads, based on the type of the argument,
     * or to {@code Arrays#deepHashCode(expr)} in case of multidimensional arrays.
     *
     * @param expr the array instance (must not be {@code null})
     * @return an {@code int} expression representing the hash code of given array (not {@code null})
     */
    Expr arrayHashCode(Expr expr);

    /**
     * Generates call to one of the {@code Arrays#equals(a, b)} overloads, based on the type of the first argument,
     * or to {@code Arrays#deepEquals(a, b)} in case of multidimensional arrays.
     *
     * @param a the first array instance (must not be {@code null})
     * @param b the second array instance (must not be {@code null})
     * @return a {@code boolean} expression representing the equality between the two arrays (not {@code null})
     */
    Expr arrayEquals(Expr a, Expr b);

    /**
     * Generates call to one of the {@code Arrays#toString(expr)} overloads, based on the type of the argument,
     * or to {@code Arrays#deepToString(expr)} in case of multidimensional arrays.
     *
     * @param expr the array instance (must not be {@code null})
     * @return a {@code String} expression representing the string value of given array (not {@code null})
     */
    Expr arrayToString(Expr expr);

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Object}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default ObjectOps withObject(Expr receiver) {
        return new ObjectOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Class}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default ClassOps withClass(Expr receiver) {
        return new ClassOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link String}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default StringOps withString(Expr receiver) {
        return new StringOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Collection}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */

    default CollectionOps withCollection(Expr receiver) {
        return new CollectionOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link List}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default ListOps withList(Expr receiver) {
        return new ListOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Set}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default SetOps withSet(Expr receiver) {
        return new SetOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Map}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default MapOps withMap(Expr receiver) {
        return new MapOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Iterator}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default IteratorOps withIterator(Expr receiver) {
        return new IteratorOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Optional}}
     *
     * @param receiver the instance to invoke upon (must not be {@code null})
     */
    default OptionalOps withOptional(Expr receiver) {
        return new OptionalOps(this, receiver);
    }

    /**
     * {@return a convenience wrapper for accessing instance methods of {@link Throwable}}
     *
     * @param throwable the instance to invoke upon (must not be {@code null})
     */
    default ThrowableOps withThrowable(Expr throwable) {
        return new ThrowableOps(this, throwable);
    }

    /**
     * Creates a {@code StringBuilder} generator that helps to generate a chain of
     * {@code append} calls and a final {@code toString} call.
     *
     * <pre>
     * StringBuilderOps str = bc.withNewStringBuilder();
     * str.append("constant");
     * str.append(someExpr);
     * Expr result = str.toString_();
     * </pre>
     *
     * The {@code append} method mimics the regular {@code StringBuilder.append}, so
     * it accepts {@code Expr}s of all types for which {@code StringBuilder}
     * has an overload:
     * <ul>
     * <li>primitive types</li>
     * <li>{@code char[]}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.CharSequence}</li>
     * <li>{@code java.lang.Object}</li>
     * </ul>
     *
     * Notably, arrays except of {@code char[]} are appended using {@code Object.toString}
     * and if {@code Arrays.toString} should be used, it must be generated manually.
     * <p>
     * Methods for appending only a part of {@code char[]} or {@code CharSequence} are not
     * provided. Other {@code StringBuilder} methods are not provided either. This is just
     * a simple utility for generating code that concatenates strings, e.g. for implementing
     * the {@code toString} method.
     *
     * @return a convenience wrapper for accessing instance methods of a newly created {@link StringBuilder}
     */
    default StringBuilderOps withNewStringBuilder() {
        return new StringBuilderOps(this);
    }

    /**
     * Creates a {@code StringBuilder} generator that helps to generate a chain of
     * {@code append} calls and a final {@code toString} call.
     *
     * <pre>
     * StringBuilderOps str = bc.withNewStringBuilder(16);
     * str.append("constant");
     * str.append(someExpr);
     * Expr result = str.toString_();
     * </pre>
     *
     * The {@code append} method mimics the regular {@code StringBuilder.append}, so
     * it accepts {@code Expr}s of all types for which {@code StringBuilder}
     * has an overload:
     * <ul>
     * <li>primitive types</li>
     * <li>{@code char[]}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.CharSequence}</li>
     * <li>{@code java.lang.Object}</li>
     * </ul>
     *
     * Notably, arrays except of {@code char[]} are appended using {@code Object.toString}
     * and if {@code Arrays.toString} should be used, it must be generated manually.
     * <p>
     * Methods for appending only a part of {@code char[]} or {@code CharSequence} are not
     * provided. Other {@code StringBuilder} methods are not provided either. This is just
     * a simple utility for generating code that concatenates strings, e.g. for implementing
     * the {@code toString} method.
     *
     * @param capacity the capacity of the newly created {@link StringBuilder}
     * @return a convenience wrapper for accessing instance methods of a newly created {@link StringBuilder}
     */
    default StringBuilderOps withNewStringBuilder(int capacity) {
        return new StringBuilderOps(this, capacity);
    }

    /**
     * Creates a {@code StringBuilder} generator that helps to generate a chain of
     * {@code append} calls and a final {@code toString} call.
     *
     * <pre>
     * StringBuilderOps str = bc.withStringBuilder(theStringBuilder);
     * str.append("constant");
     * str.append(someExpr);
     * Expr result = str.toString_();
     * </pre>
     *
     * The {@code append} method mimics the regular {@code StringBuilder.append}, so
     * it accepts {@code Expr}s of all types for which {@code StringBuilder}
     * has an overload:
     * <ul>
     * <li>primitive types</li>
     * <li>{@code char[]}</li>
     * <li>{@code java.lang.String}</li>
     * <li>{@code java.lang.CharSequence}</li>
     * <li>{@code java.lang.Object}</li>
     * </ul>
     *
     * Notably, arrays except of {@code char[]} are appended using {@code Object.toString}
     * and if {@code Arrays.toString} should be used, it must be generated manually.
     * <p>
     * Methods for appending only a part of {@code char[]} or {@code CharSequence} are not
     * provided. Other {@code StringBuilder} methods are not provided either. This is just
     * a simple utility for generating code that concatenates strings, e.g. for implementing
     * the {@code toString} method.
     *
     * @param receiver the {@link StringBuilder}
     * @return a convenience wrapper for accessing instance methods of the given {@link StringBuilder}
     */
    default StringBuilderOps withStringBuilder(Expr receiver) {
        return new StringBuilderOps(this, receiver);
    }

    /**
     * Generate a call to {@link Class#forName(String)} which uses the defining class loader of this class.
     *
     * @param className the class name (must not be {@code null})
     * @return the loaded class expression (not {@code null})
     */
    Expr classForName(Expr className);

    /**
     * Generate a call to {@link List#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the list after mapping (must not be {@code null})
     * @param mapper function that turns values of type {@code T} into expressions (must not be {@code null}
     *        and must not produce {@code null})
     * @return the list expression (not {@code null})
     * @see #withList(Expr)
     */
    <T> Expr listOf(List<T> items, Function<T, ? extends Expr> mapper);

    /**
     * Generate a call to {@link List#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the list (must not be {@code null})
     * @return the list expression (not {@code null})
     * @see #withList(Expr)
     */
    default Expr listOf(List<? extends Expr> items) {
        return listOf(items, Function.identity());
    }

    /**
     * Generate a call to {@link List#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the list (must not be {@code null})
     * @return the list expression (not {@code null})
     * @see #withList(Expr)
     */
    default Expr listOf(Expr... items) {
        return listOf(List.of(items));
    }

    /**
     * Generate a call to {@link Set#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the set after mapping (must not be {@code null})
     * @param mapper function that turns values of type {@code T} into expressions (must not be {@code null}
     *        and must not produce {@code null})
     * @return the list expression (not {@code null})
     * @see #withSet(Expr)
     */
    <T> Expr setOf(List<T> items, Function<T, ? extends Expr> mapper);

    /**
     * Generate a call to {@link Set#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the list (must not be {@code null})
     * @return the set expression (not {@code null})
     * @see #withSet(Expr)
     */
    default Expr setOf(List<? extends Expr> items) {
        return setOf(items, Function.identity());
    }

    /**
     * Generate a call to {@link Set#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the items to add to the set (must not be {@code null})
     * @return the set expression (not {@code null})
     * @see #withSet(Expr)
     */
    default Expr setOf(Expr... items) {
        return setOf(List.of(items));
    }

    /**
     * Generate a call to {@link Map#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the keys and values from which the map is populated
     * @return map expression (not {@code null})
     * @see BlockCreator#withMap(Expr)
     */
    Expr mapOf(List<? extends Expr> items);

    /**
     * Generate a call to {@link Map#of()} or one of its variants, based on the number of arguments.
     *
     * @param items the keys and values from which the map is populated
     * @return map expression (not {@code null})
     * @see BlockCreator#withMap(Expr)
     */
    default Expr mapOf(Expr... items) {
        return mapOf(List.of(items));
    }

    /**
     * Generate a call to {@link Map#entry(Object, Object)}.
     *
     * @param key the key for the new entry (must not be {@code null})
     * @param value the value for the new entry (must not be {@code null})
     * @return the new map entry (not {@code null})
     */
    Expr mapEntry(Expr key, Expr value);

    /**
     * Generate a call to {@link Optional#of(Object)}.
     *
     * @param value the expression to pass in to the call (must not be {@code null})
     * @return optional expression (not {@code null})
     * @see BlockCreator#withOptional(Expr)
     */
    Expr optionalOf(Expr value);

    /**
     * Generate a call to {@link Optional#ofNullable(Object)}.
     *
     * @param value the expression to pass in to the call (must not be {@code null})
     * @return optional expression (not {@code null})
     * @see BlockCreator#withOptional(Expr)
     */
    Expr optionalOfNullable(Expr value);

    /**
     * Iterate the given target.
     * In other words, generate a call to {@link Iterable#iterator()}.
     *
     * @param items the iterable object expression (must not be {@code null})
     * @return the iterator expression (not {@code null})
     */
    Expr iterate(Expr items);

    /**
     * {@return an expression representing the current thread from the perspective of the running method body}
     */
    Expr currentThread();

    /**
     * Close the given target.
     *
     * @param closeable the closeable object (must not be {@code null})
     */
    void close(Expr closeable);

    // debug stuff

    /**
     * Change the current line number from this point.
     *
     * @param lineNumber the line number
     */
    void line(int lineNumber);

    /**
     * Insert a {@code printf} statement.
     *
     * @param format the format string (must not be {@code null})
     * @param values the value expression(s) (must not be {@code null})
     */
    void printf(String format, List<? extends Expr> values);

    /**
     * Insert a {@code printf} statement.
     *
     * @param format the format string (must not be {@code null})
     * @param values the value expression(s) (must not be {@code null})
     */
    default void printf(String format, Expr... values) {
        printf(format, List.of(values));
    }

    /**
     * Produce an assertion.
     *
     * @param assertion the assertion expression maker (must not be {@code null})
     * @param message the message to print if the assertion fails (must not be {@code null})
     */
    void assert_(Consumer<BlockCreator> assertion, String message);
}
