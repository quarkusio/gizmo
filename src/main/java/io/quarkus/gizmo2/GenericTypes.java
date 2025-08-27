package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

/**
 * A holder for common generic types.
 */
public final class GenericTypes {
    private GenericTypes() {
    }

    /**
     * The generic type for {@code boolean}.
     */
    public static final GenericType GT_boolean = GenericType.of(CD_boolean);
    /**
     * The generic type for {@code byte}.
     */
    public static final GenericType GT_byte = GenericType.of(CD_byte);
    /**
     * The generic type for {@code short}.
     */
    public static final GenericType GT_short = GenericType.of(CD_short);
    /**
     * The generic type for {@code char}.
     */
    public static final GenericType GT_char = GenericType.of(CD_char);
    /**
     * The generic type for {@code int}.
     */
    public static final GenericType GT_int = GenericType.of(CD_int);
    /**
     * The generic type for {@code long}.
     */
    public static final GenericType GT_long = GenericType.of(CD_long);
    /**
     * The generic type for {@code float}.
     */
    public static final GenericType GT_float = GenericType.of(CD_float);
    /**
     * The generic type for {@code double}.
     */
    public static final GenericType GT_double = GenericType.of(CD_double);
    /**
     * The generic type for {@code void}.
     */
    public static final GenericType GT_void = GenericType.of(CD_void);
}
