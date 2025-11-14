package io.quarkus.gizmo2.creator.ops;

import static io.quarkus.gizmo2.desc.Descs.*;

import io.quarkus.gizmo2.Const;
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
        super(bc, obj);
    }

    /**
     * Generate a call to {@link String#isEmpty()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr isEmpty() {
        return bc.invokeVirtual(MD_String.isEmpty, obj);
    }

    /**
     * Generate a call to {@link String#length()}.
     *
     * @return the expression of the result (not {@code null})
     */
    public Expr length() {
        return bc.invokeVirtual(MD_String.length, obj);
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the expression of the start index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(Expr start) {
        return bc.invokeVirtual(MD_String.substring_1, obj, start);
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the start index
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(int start) {
        return substring(Const.of(start));
    }

    /**
     * Generate a call to {@link String#substring(int,int)}.
     *
     * @param start the expression of the start index (must not be {@code null})
     * @param end the expression of the end index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(Expr start, Expr end) {
        return bc.invokeVirtual(MD_String.substring_2, obj, start, end);
    }

    /**
     * Generate a call to {@link String#substring(int)}.
     *
     * @param start the start index
     * @param end the end index
     * @return the expression of the result (not {@code null})
     */
    public Expr substring(int start, int end) {
        return substring(Const.of(start), Const.of(end));
    }

    /**
     * Generate a call to {@link String#charAt(int)}.
     *
     * @param index the expression of the index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr charAt(Expr index) {
        return bc.invokeVirtual(MD_String.charAt, obj, index);
    }

    /**
     * Generate a call to {@link String#charAt(int)}.
     *
     * @param index the index
     * @return the expression of the result (not {@code null})
     */
    public Expr charAt(int index) {
        return charAt(Const.of(index));
    }

    /**
     * Generate a call to {@link String#codePointAt(int)}.
     *
     * @param index the expression of the index (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr codePointAt(Expr index) {
        return bc.invokeVirtual(MD_String.codePointAt, obj, index);
    }

    /**
     * Generate a call to {@link String#codePointAt(int)}.
     *
     * @param index the index
     * @return the expression of the result (not {@code null})
     */
    public Expr codePointAt(int index) {
        return codePointAt(Const.of(index));
    }

    /**
     * Generate a call to {@link String#indexOf(int)} or {@link String#indexOf(String)}.
     *
     * @param item the expression of the search item (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr indexOf(Expr item) {
        return switch (item.typeKind().asLoadable()) {
            case INT -> bc.invokeVirtual(MD_String.indexOf_int, obj, item);
            case REFERENCE -> bc.invokeVirtual(MD_String.indexOf_String, obj, item);
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
        return indexOf(Const.of(ch));
    }

    /**
     * Generate a call to {@link String#indexOf(String)}.
     *
     * @param str the string to search for (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr indexOf(String str) {
        return indexOf(Const.of(str));
    }

    /**
     * Generate a call to {@link String#lastIndexOf(int)} or {@link String#lastIndexOf(String)}.
     *
     * @param item the expression of the search item (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr lastIndexOf(Expr item) {
        return switch (item.typeKind().asLoadable()) {
            case INT -> bc.invokeVirtual(MD_String.lastIndexOf_int, obj, item);
            case REFERENCE -> bc.invokeVirtual(MD_String.lastIndexOf_String, obj, item);
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
        return lastIndexOf(Const.of(ch));
    }

    /**
     * Generate a call to {@link String#lastIndexOf(String)}.
     *
     * @param str the string to search for (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr lastIndexOf(String str) {
        return lastIndexOf(Const.of(str));
    }

    /**
     * Generate a call to {@link String#concat(String)}.
     *
     * @param other the string to concatenate to this one (must not be {@code null})
     * @return the expression of the result (not {@code null})
     */
    public Expr concat(final Expr other) {
        return bc.invokeVirtual(MD_String.concat, obj, other);
    }

    @Override
    public Expr compareTo(final Expr other) {
        return bc.invokeVirtual(MD_String.compareTo, obj, other);
    }
}
