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

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.AccessMode;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LValueExpr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
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

    /**
     * {@return true if this block is contained by the given block, or false if it is not}
     * @param other the containing block (must not be {@code null})
     */
    boolean isContainedBy(BlockCreator other);

    /**
     * {@return true if this block contains the given block, or false if it does not}
     * @param other the contained block (must not be {@code null})
     */
    default boolean contains(BlockCreator other) {
        return other.isContainedBy(this);
    }

    /**
     * {@return true if this block contains the block of the given variable, or false if it does not}
     * @param var the variable (must not be {@code null})
     */
    default boolean contains(LocalVar var) {
        return contains(var.block());
    }

    // lexical variables

    /**
     * Declare a local variable.
     * Local variables may not be read before they are written.
     *
     * @param name the variable name (must not be {@code null})
     * @param type the variable type (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    LocalVar declare(String name, ClassDesc type);

    /**
     * Declare a local variable.
     * Local variables may not be read before they are written.
     *
     * @param name the variable name (must not be {@code null})
     * @param type the variable type (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    default LocalVar declare(String name, Class<?> type) {
        return declare(name, Util.classDesc(type));
    }

    /**
     * Declare a local variable and define its initial value.
     *
     * @param name the variable name (must not be {@code null})
     * @param value the variable initial value (must not be {@code null})
     * @return the local variable (not {@code null})
     */
    default LocalVar define(String name, Expr value) {
        LocalVar var = declare(name, value.type());
        set(var, value);
        return var;
    }

    /**
     * Declare a local variable which is initialized to the given variable's current value.
     * The given variable may be a parameter, local variable, or field variable.
     *
     * @param original the original variable (must not be {@code null})
     * @return the new local variable (must not be {@code null})
     */
    default LocalVar define(Var original) {
        return define(original.name(), original);
    }


    // reading memory

    /**
     * Read a value from memory with the given atomicity mode.
     *
     * @param var the lvalue (must not be {@code null})
     * @param mode the atomicity mode for the access (must not be {@code null})
     * @return the memory value (not {@code null})
     */
    Expr get(LValueExpr var, AccessMode mode);

    /**
     * Read a value from memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @return the memory value (not {@code null})
     */
    default Expr get(LValueExpr var) {
        return get(var, AccessMode.AsDeclared);
    }

    // writing memory

    /**
     * Write a value to memory with the given atomicity mode.
     *
     * @param var   the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     * @param mode  the atomicity mode for the access (must not be {@code null})
     */
    void set(LValueExpr var, Expr value, AccessMode mode);

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(LValueExpr var, Expr value) {
        set(var, value, AccessMode.AsDeclared);
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(LValueExpr var, Constant value) {
        set(var, (Expr) value);
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(LValueExpr var, ConstantDesc value) {
        set(var, Constant.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(LValueExpr var, Constable value) {
        set(var, Constant.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write (must not be {@code null})
     */
    default void set(LValueExpr var, String value) {
        set(var, Constant.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write
     */
    default void set(LValueExpr var, int value) {
        set(var, Constant.of(value));
    }

    /**
     * Write a value to memory using the declared atomicity mode for the lvalue.
     *
     * @param var the lvalue (must not be {@code null})
     * @param value the value to write
     */
    default void set(LValueExpr var, long value) {
        set(var, Constant.of(value));
    }


    // increment/decrement

    /**
     * Increment some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to increase by (must not be {@code null})
     */
    void inc(LValueExpr var, Constant amount);

    /**
     * Increment some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to increase by
     */
    default void inc(LValueExpr var, int amount) {
        inc(var, Constant.of(amount, var.typeKind()));
    }

    /**
     * Increment some variable by one.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     */
    default void inc(LValueExpr var) {
        inc(var, 1);
    }

    /**
     * Decrement some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to decrease by (must not be {@code null})
     */
    void dec(LValueExpr var, Constant amount);

    /**
     * Decrement some variable by a constant amount.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     * @param amount the constant amount to decrease by
     */
    default void dec(LValueExpr var, int amount) {
        dec(var, Constant.of(amount, var.typeKind()));
    }

    /**
     * Decrement some variable by one.
     * This is not an atomic operation.
     *
     * @param var the variable to modify (must not be {@code null})
     */
    default void dec(LValueExpr var) {
        dec(var, 1);
    }

    // arrays

    /**
     * Create a new, empty array of the given type.
     *
     * @param elemType the element type (must not be {@code null})
     * @param size the size of the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    Expr newEmptyArray(ClassDesc elemType, Expr size);

    /**
     * Create a new, empty array of the given type.
     *
     * @param elemType the element type (must not be {@code null})
     * @param size the size of the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newEmptyArray(Class<?> elemType, Expr size) {
        return newEmptyArray(Util.classDesc(elemType), size);
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param elementType the element type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    Expr newArray(ClassDesc elementType, List<Expr> values);

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param elementType the element type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(ClassDesc elementType, Expr... values) {
        return newArray(elementType, List.of(values));
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param elementType the element type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(Class<?> elementType, List<Expr> values) {
        return newArray(Util.classDesc(elementType), values);
    }

    /**
     * Create a new array with the given type, initialized with the given values.
     *
     * @param elementType the element type (must not be {@code null})
     * @param values the values to assign into the array (must not be {@code null})
     * @return the expression for the new array (not {@code null})
     */
    default Expr newArray(Class<?> elementType, Expr... values) {
        return newArray(elementType, List.of(values));
    }

    // relational ops

    /**
     * The equality operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code ==} operator in Java
     * for primitive and reference values.
     * For object equality using {@link Object#equals}, see {@link #exprEquals(Expr, Expr)}.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument (must not be {@code null})
     * @return the boolean result expression
     * @see #exprEquals(Expr, Expr)
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
        return eq(a, Constant.of(b, a.typeKind()));
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
        return eq(a, Constant.of(b, a.typeKind()));
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
        return eq(a, Constant.of(b, a.typeKind()));
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
        return eq(a, Constant.of(b, a.typeKind()));
    }

    /**
     * The inequality operator.
     * The arguments must be of the same {@linkplain TypeKind type kind}.
     * This works equivalently to the {@code !=} operator in Java
     * for primitive and reference values.
     * For object equality using {@link Object#equals}, see {@link #exprEquals(Expr, Expr)}.
     *
     * @param a the left-hand argument (must not be {@code null})
     * @param b the right-hand argument (must not be {@code null})
     * @return the boolean result expression
     * @see #exprEquals(Expr, Expr)
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
        return ne(a, Constant.of(b, a.typeKind()));
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
        return ne(a, Constant.of(b, a.typeKind()));
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
        return ne(a, Constant.of(b, a.typeKind()));
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
        return ne(a, Constant.of(b, a.typeKind()));
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
        return lt(a, Constant.of(b, a.typeKind()));
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
        return lt(a, Constant.of(b, a.typeKind()));
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
        return lt(a, Constant.of(b, a.typeKind()));
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
        return lt(a, Constant.of(b, a.typeKind()));
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
        return gt(a, Constant.of(b, a.typeKind()));
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
        return gt(a, Constant.of(b, a.typeKind()));
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
        return gt(a, Constant.of(b, a.typeKind()));
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
        return gt(a, Constant.of(b, a.typeKind()));
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
        return le(a, Constant.of(b, a.typeKind()));
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
        return le(a, Constant.of(b, a.typeKind()));
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
        return le(a, Constant.of(b, a.typeKind()));
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
        return le(a, Constant.of(b, a.typeKind()));
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
        return ge(a, Constant.of(b, a.typeKind()));
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
        return ge(a, Constant.of(b, a.typeKind()));
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
        return ge(a, Constant.of(b, a.typeKind()));
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
        return ge(a, Constant.of(b, a.typeKind()));
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
        return and(a, Constant.of(b, a.typeKind()));
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
        return and(a, Constant.of(b, a.typeKind()));
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
        return or(a, Constant.of(b, a.typeKind()));
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
        return or(a, Constant.of(b, a.typeKind()));
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
        return xor(a, Constant.of(b, a.typeKind()));
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
        return xor(a, Constant.of(b, a.typeKind()));
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
        return shl(a, Constant.of(b, a.typeKind()));
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
        return shl(a, Constant.of(b, a.typeKind()));
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
        return shr(a, Constant.of(b, a.typeKind()));
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
        return shr(a, Constant.of(b, a.typeKind()));
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
        return ushr(a, Constant.of(b, a.typeKind()));
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
        return ushr(a, Constant.of(b, a.typeKind()));
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
        return add(a, Constant.of(b, a.typeKind()));
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
        return add(a, Constant.of(b, a.typeKind()));
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
        return add(a, Constant.of(b, a.typeKind()));
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
        return add(a, Constant.of(b, a.typeKind()));
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
        return sub(a, Constant.of(b, a.typeKind()));
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
        return sub(a, Constant.of(b, a.typeKind()));
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
        return sub(a, Constant.of(b, a.typeKind()));
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
        return sub(a, Constant.of(b, a.typeKind()));
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
        return sub(Constant.of(a, b.typeKind()), b);
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
        return sub(Constant.of(a, b.typeKind()), b);
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
        return sub(Constant.of(a, b.typeKind()), b);
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
        return sub(Constant.of(a, b.typeKind()), b);
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
        return mul(a, Constant.of(b, a.typeKind()));
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
        return mul(a, Constant.of(b, a.typeKind()));
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
        return mul(a, Constant.of(b, a.typeKind()));
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
        return mul(a, Constant.of(b, a.typeKind()));
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
        return div(a, Constant.of(b, a.typeKind()));
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
        return div(a, Constant.of(b, a.typeKind()));
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
        return div(a, Constant.of(b, a.typeKind()));
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
        return div(a, Constant.of(b, a.typeKind()));
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
        return div(Constant.of(a, b.typeKind()), b);
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
        return div(Constant.of(a, b.typeKind()), b);
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
        return div(Constant.of(a, b.typeKind()), b);
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
        return div(Constant.of(a, b.typeKind()), b);
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
        return rem(a, Constant.of(b, a.typeKind()));
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
        return rem(a, Constant.of(b, a.typeKind()));
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
        return rem(a, Constant.of(b, a.typeKind()));
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
        return rem(a, Constant.of(b, a.typeKind()));
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
        return rem(Constant.of(a, b.typeKind()), b);
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
        return rem(Constant.of(a, b.typeKind()), b);
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
        return rem(Constant.of(a, b.typeKind()), b);
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
        return rem(Constant.of(a, b.typeKind()), b);
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

    void addAssign(LValueExpr var, Expr arg);

    void subAssign(LValueExpr var, Expr arg);

    void mulAssign(LValueExpr var, Expr arg);

    void divAssign(LValueExpr var, Expr arg);

    void remAssign(LValueExpr var, Expr arg);


    // bitwise-assign

    void andAssign(LValueExpr var, Expr arg);

    void orAssign(LValueExpr var, Expr arg);

    void xorAssign(LValueExpr var, Expr arg);

    void shlAssign(LValueExpr var, Expr arg);

    void shrAssign(LValueExpr var, Expr arg);

    void ushrAssign(LValueExpr var, Expr arg);


    // logical

    default Expr logicalNot(Expr a) {
        return a.typeKind() == TypeKind.BOOLEAN ? xor(a, Constant.of(1)) : eq(a, Constant.of(0, a.typeKind()));
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

    default Expr new_(ConstructorDesc ctor, Expr... args) {
        return new_(ctor, List.of(args));
    }

    default Expr new_(ClassDesc type, List<Expr> args) {
        return new_(ConstructorDesc.of(type, args.stream().map(Expr::type).toList()), args);
    }

    default Expr new_(ClassDesc type, Expr... args) {
        return new_(type, List.of(args));
    }

    default Expr new_(Class<?> type, List<Expr> args) {
        return new_(Util.classDesc(type), args);
    }

    default Expr new_(Class<?> type, Expr... args) {
        return new_(type, List.of(args));
    }

    // invocation

    Expr invokeStatic(MethodDesc method, List<Expr> args);

    default Expr invokeStatic(MethodDesc method, Expr... args) {
        return invokeStatic(method, List.of(args));
    }

    Expr invokeVirtual(MethodDesc method, Expr instance, List<Expr> args);

    default Expr invokeVirtual(MethodDesc method, Expr instance, Expr... args) {
        return invokeVirtual(method, instance, List.of(args));
    }

    Expr invokeSpecial(MethodDesc method, Expr instance, List<Expr> args);

    default Expr invokeSpecial(MethodDesc method, Expr instance, Expr... args) {
        return invokeSpecial(method, instance, List.of(args));
    }

    Expr invokeSpecial(ConstructorDesc method, Expr instance, List<Expr> args);

    default Expr invokeSpecial(ConstructorDesc method, Expr instance, Expr... args) {
        return invokeSpecial(method, instance, List.of(args));
    }

    Expr invokeInterface(MethodDesc method, Expr instance, List<Expr> args);

    default Expr invokeInterface(MethodDesc method, Expr instance, Expr... args) {
        return invokeInterface(method, instance, List.of(args));
    }

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
     * Construct a {@code switch} statement for {@code enum} constants.
     *
     * @param val the value to switch on (must not be {@code null})
     * @param builder the builder for the {@code switch} statement (must not be {@code null})
     */
    void switchEnum(Expr val, Consumer<SwitchCreator> builder);

    /**
     * Construct a {@code switch} statement.
     * The switch value must be one of these supported types:
     * <ul>
     *     <li>{@code int} (which includes {@code byte}, {@code char}, {@code short}, and {@code boolean})</li>
     *     <li>{@code java.lang.String}</li>
     *     <li>{@code java.lang.Class}</li>
     * </ul>
     * The type of the {@code switch} creator depends on the type of the value.
     * For {@code enum} switches, use {@link #switchEnum(Expr, Consumer)}.
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

    /**
     * {@return the hash code of the given expression}
     * @param expr the expression, which can be of any type (must not be {@code null})
     */
    Expr exprHashCode(Expr expr);

    /**
     * Determine the equality of the given objects using <em>value equality</em>
     * rather than <em>reference equality</em>.
     * This is analogous to using {@link Object#equals(Object)} to compare references.
     * For non-reference types, this would be equivalent to {@link #eq(Expr, Expr)}.
     *
     * @param a the first expression (must not be {@code null})
     * @param b the second expression (must not be {@code null})
     * @return a {@code boolean} expression representing the equality (or lack thereof) between the two values
     */
    Expr exprEquals(Expr a, Expr b);

    default Expr exprEquals(Expr a, ConstantDesc b) {
        return exprEquals(a, Constant.of(b));
    }

    /**
     * {@return a string expression for the given expression}
     * @param expr the expression, which can be of any type
     */
    Expr exprToString(Expr expr);

    Expr arrayEquals(Expr a, Expr b);

    Expr loadClass(Expr className);

    Expr listOf(List<Expr> items);

    default Expr listOf(Expr... items) {
        return listOf(List.of(items));
    }

    Expr setOf(List<Expr> items);

    default Expr setOf(Expr... items) {
        return setOf(List.of(items));
    }

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

    default void printf(String format, Expr... values) {
        printf(format, List.of(values));
    }

    void assert_(Function<BlockCreator, Expr> assertion, String message);
}

