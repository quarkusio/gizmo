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
    public static final GenericType.OfPrimitive GT_boolean = (GenericType.OfPrimitive) GenericType.of(CD_boolean);
    /**
     * The generic type for {@code byte}.
     */
    public static final GenericType.OfPrimitive GT_byte = (GenericType.OfPrimitive) GenericType.of(CD_byte);
    /**
     * The generic type for {@code short}.
     */
    public static final GenericType.OfPrimitive GT_short = (GenericType.OfPrimitive) GenericType.of(CD_short);
    /**
     * The generic type for {@code char}.
     */
    public static final GenericType.OfPrimitive GT_char = (GenericType.OfPrimitive) GenericType.of(CD_char);
    /**
     * The generic type for {@code int}.
     */
    public static final GenericType.OfPrimitive GT_int = (GenericType.OfPrimitive) GenericType.of(CD_int);
    /**
     * The generic type for {@code long}.
     */
    public static final GenericType.OfPrimitive GT_long = (GenericType.OfPrimitive) GenericType.of(CD_long);
    /**
     * The generic type for {@code float}.
     */
    public static final GenericType.OfPrimitive GT_float = (GenericType.OfPrimitive) GenericType.of(CD_float);
    /**
     * The generic type for {@code double}.
     */
    public static final GenericType.OfPrimitive GT_double = (GenericType.OfPrimitive) GenericType.of(CD_double);
    /**
     * The generic type for {@code void}.
     */
    public static final GenericType.OfPrimitive GT_void = (GenericType.OfPrimitive) GenericType.of(CD_void);
    /**
     * The generic type for {@code java.lang.Object}.
     */
    public static final GenericType.OfClass GT_Object = (GenericType.OfClass) GenericType.of(CD_Object);
}
