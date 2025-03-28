package io.quarkus.gizmo2.creator.ops;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;

/**
 * Operations on {@link String}.
 */
public final class StringOps extends ObjectOps implements ComparableOps {
    /**
     * Construct a new instance.
     *
     * @param bc the block creator (must not be {@code null})
     * @param obj the receiver string (must not be {@code null})
     */
    public StringOps(final BlockCreator bc, final Expr obj) {
        super(String.class, bc, obj);
    }

    /**
     * Generate a call to {@link String#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return invokeInstance(boolean.class, "isEmpty");
    }

    /**
     * Generate a call to {@link String#length()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr length() {
        return invokeInstance(int.class, "length");
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the expression of the start index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(Expr start) {
        return invokeInstance(String.class, "substring", int.class, start);
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the start index
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(int start) {
        return substring(Constant.of(start));
    }

    /**
     * Generate a call to {@link String#substring(int,int)}.
     *
     * @param start the expression of the start index (must not be {@code null})
     * @param end the expression of the end index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(Expr start, Expr end) {
        return invokeInstance(String.class, "substring", int.class, int.class, start, end);
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the start index
     * @param end the end index
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(int start, int end) {
        return substring(Constant.of(start), Constant.of(end));
    }

    /**
     * Generate a call to {@link String#charAt(int)}.
     *
     * @param index the expression of the index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr charAt(Expr index) {
        return invokeInstance(char.class, "charAt", int.class, index);
    }

    /**
     * Generate a call to {@link String#charAt(int)}.
     *
     * @param index the index
     * @return the expression of the result (not {@code null})
     */
    public Expr charAt(int index) {
        return charAt(Constant.of(index));
    }

    /**
     * Generate a call to {@link String#codePointAt(int)}.
     *
     * @param index the expression of the index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr codePointAt(Expr index) {
        return invokeInstance(int.class, "codePointAt", int.class, index);
    }

    /**
     * Generate a call to {@link String#codePointAt(int)}.
     *
     * @param index the index
     * @return the expression of the result (not {@code null})
     */
    public Expr codePointAt(int index) {
        return codePointAt(Constant.of(index));
    }

    /**
     * Generate a call to {@link String#indexOf(int)} or {@link String#indexOf(String)}.
     *
     * @param item the expression of the search item (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr indexOf(Expr item) {
        return switch (item.typeKind().asLoadable()) {
            case INT -> invokeInstance(int.class, "indexOf", int.class, item);
            case REFERENCE -> invokeInstance(int.class, "indexOf", String.class, item);
            default -> throw new IllegalArgumentException("Invalid item type " + item.type());
        };
    }

    /**
     * Generate a call to {@link String#indexOf(int)}.
     *
     * @param ch the character to search for
     * @return the expression of the result (not {@code null})
     */
    public Expr indexOf(int ch) {
        return indexOf(Constant.of(ch));
    }

    /**
     * Generate a call to {@link String#indexOf(String)}.
     *
     * @param str the string to search for (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr indexOf(String str) {
        return indexOf(Constant.of(str));
    }

    /**
     * Generate a call to {@link String#lastIndexOf(int)} or {@link String#lastIndexOf(String)}.
     *
     * @param item the expression of the search item (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr lastIndexOf(Expr item) {
        return switch (item.typeKind().asLoadable()) {
            case INT -> invokeInstance(int.class, "lastIndexOf", int.class, item);
            case REFERENCE -> invokeInstance(int.class, "lastIndexOf", String.class, item);
            default -> throw new IllegalArgumentException("Invalid item type " + item.type());
        };
    }

    /**
     * Generate a call to {@link String#lastIndexOf(int)}.
     *
     * @param ch the character to search for
     * @return the expression of the result (not {@code null})
     */
    public Expr lastIndexOf(int ch) {
        return lastIndexOf(Constant.of(ch));
    }

    /**
     * Generate a call to {@link String#lastIndexOf(String)}.
     *
     * @param str the string to search for (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr lastIndexOf(String str) {
        return lastIndexOf(Constant.of(str));
    }

    /**
     * Generate a call to {@link String#concat(String)}.
     *
     * @param other the string to concatenate to this one (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr concat(final Expr other) {
        return invokeInstance(String.class, "concat", String.class, other);
    }

    @Override
    public Expr compareTo(final Expr other) {
        return invokeInstance(int.class, "compareTo", String.class, other);
    }
}
