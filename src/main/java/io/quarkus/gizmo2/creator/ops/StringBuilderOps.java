package io.quarkus.gizmo2.creator.ops;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link StringBuilder}. The expected usage pattern is:
 * <ol>
 * <li>Create an instance using {@link BlockCreator#withNewStringBuilder()}</li>
 * <li>Append to it using {@link #append(Expr)}</li>
 * <li>Create the final string using {@link #toString_()}</li>
 * </ol>
 * If you need to perform other operations on the {@code StringBuilder}
 * that this class doesn't provide, you should create an instance
 * using {@link BlockCreator#withStringBuilder(Expr)}, which allows
 * you to pass an already created {@code StringBuilder}. This class
 * itself doesn't provide access to the underlying object.
 */
public final class StringBuilderOps extends ObjectOps implements ComparableOps {
    // note that for the `public` methods to be able to `return this`, all constructors
    // must make sure the string builder we operate upon is stored in a local variable!
    private static Var declare(Expr obj, BlockCreator bc) {
        return obj instanceof Var var ? var : bc.localVar("$$stringBuilder", obj);
    }

    /**
     * Construct a new instance on an existing {@link StringBuilder}.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver string builder (must not be {@code null})
     */
    public StringBuilderOps(final BlockCreator bc, final Expr obj) {
        super(StringBuilder.class, bc, declare(obj, bc));
    }

    /**
     * Construct a new instance on a newly created {@link StringBuilder}.
     *
     * @param bc the block creator (must not be {@code null})
     */
    public StringBuilderOps(final BlockCreator bc) {
        super(StringBuilder.class, bc, declare(bc.new_(StringBuilder.class), bc));
    }

    /**
     * Construct a new instance on a newly created {@link StringBuilder}
     * with given {@code capacity}.
     *
     * @param bc the block creator (must not be {@code null})
     * @param capacity the capacity of the newly created {@link StringBuilder}
     */
    public StringBuilderOps(final BlockCreator bc, final int capacity) {
        super(StringBuilder.class, bc, declare(bc.new_(StringBuilder.class, Const.of(capacity)), bc));
    }

    /**
     * Appends the string value of given {@code expr} to this {@code StringBuilder}.
     *
     * @param expr the value to append
     * @return this instance
     */
    public StringBuilderOps append(final Expr expr) {
        switch (expr.type().descriptorString()) {
            case "Z" -> invokeInstance(StringBuilder.class, "append", boolean.class, expr);
            case "B", "S", "I" -> invokeInstance(StringBuilder.class, "append", int.class, expr);
            case "J" -> invokeInstance(StringBuilder.class, "append", long.class, expr);
            case "F" -> invokeInstance(StringBuilder.class, "append", float.class, expr);
            case "D" -> invokeInstance(StringBuilder.class, "append", double.class, expr);
            case "C" -> invokeInstance(StringBuilder.class, "append", char.class, expr);
            case "[C" -> invokeInstance(StringBuilder.class, "append", char[].class, expr);
            case "Ljava/lang/String;" -> invokeInstance(StringBuilder.class, "append", String.class, expr);
            case "Ljava/lang/CharSequence;" -> invokeInstance(StringBuilder.class, "append", CharSequence.class, expr);
            default -> invokeInstance(StringBuilder.class, "append", Object.class, expr);
        }
        return this;
    }

    /**
     * Appends the given {@code char} constant to this {@code StringBuilder}.
     *
     * @param constant the value to append
     * @return this instance
     */
    public StringBuilderOps append(final char constant) {
        return append(Const.of(constant));
    }

    /**
     * Appends the given {@code String} constant to this {@code StringBuilder}.
     *
     * @param constant the value to append
     * @return this instance
     */
    public StringBuilderOps append(final String constant) {
        return append(Const.of(constant));
    }

    /**
     * Appends the given code point to this {@code StringBuilder}.
     *
     * @param codePoint the value to append (must not be {@code null})
     * @return this instance
     */
    public StringBuilderOps appendCodePoint(final Expr codePoint) {
        invokeInstance(StringBuilder.class, "appendCodePoint", int.class, codePoint);
        return this;
    }

    /**
     * Appends the given code point to this {@code StringBuilder}.
     *
     * @param codePoint the value to append
     * @return this instance
     */
    public StringBuilderOps appendCodePoint(final int codePoint) {
        return appendCodePoint(Const.of(codePoint));
    }

    /**
     * Set the length of this {@code StringBuilder}.
     *
     * @param length the length expression (must not be {@code null})
     */
    public void setLength(Expr length) {
        invokeInstance(void.class, "setLength", int.class, length);
    }

    /**
     * Set the length of this {@code StringBuilder}.
     *
     * @param length the constant length
     */
    public void setLength(int length) {
        setLength(Const.of(length));
    }

    @Override
    public Expr compareTo(Expr other) {
        return invokeInstance(int.class, "compareTo", StringBuilder.class, other);
    }
}
