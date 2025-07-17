package io.quarkus.gizmo2.creator;

import java.util.List;
import java.util.stream.Stream;

/**
 * The possible locations where a modifier flag or an access level may be specified.
 */
public enum ModifierLocation {
    // by default, we only put the `synthetic` flag on classes/interfaces and not their members,
    // because synthetic members are ignored by (at least) the IntelliJ IDEA decompiler

    // IMPORTANT: If any changes are made here, the table in `MANUAL.adoc` should be updated as well
    /**
     * A public (default) interface instance method.
     */
    INTERFACE_DEFAULT_METHOD(
            bitsOf(AccessLevel.PUBLIC),
            bitsOf(ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            0,
            AccessLevel.PUBLIC),
    /**
     * A private interface instance method.
     */
    INTERFACE_PRIVATE_INSTANCE_METHOD(
            bitsOf(AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            0,
            AccessLevel.PRIVATE),
    /**
     * A regular (abstract) interface instance method.
     */
    INTERFACE_ABSTRACT_METHOD(
            bitsOf(AccessLevel.PUBLIC),
            bitsOf(ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            bitsOf(ModifierFlag.ABSTRACT),
            AccessLevel.PUBLIC),
    /**
     * A static field on an interface.
     */
    INTERFACE_STATIC_FIELD(
            bitsOf(AccessLevel.PUBLIC),
            bitsOf(ModifierFlag.SYNTHETIC, ModifierFlag.VOLATILE),
            bitsOf(ModifierFlag.STATIC, ModifierFlag.FINAL),
            AccessLevel.PUBLIC),
    /**
     * A static method on an interface.
     */
    INTERFACE_STATIC_METHOD(
            bitsOf(AccessLevel.PUBLIC),
            bitsOf(ModifierFlag.FINAL, ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            bitsOf(ModifierFlag.STATIC),
            AccessLevel.PUBLIC),
    /**
     * A constructor of a class.
     */
    CLASS_CONSTRUCTOR(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            0,
            AccessLevel.PUBLIC),
    /**
     * A non-native instance method of a class.
     */
    CLASS_INSTANCE_METHOD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.BRIDGE, ModifierFlag.FINAL, ModifierFlag.SYNCHRONIZED, ModifierFlag.SYNTHETIC,
                    ModifierFlag.VARARGS),
            0,
            AccessLevel.PUBLIC),
    /**
     * An abstract instance method of a class.
     */
    CLASS_ABSTRACT_METHOD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE),
            bitsOf(ModifierFlag.BRIDGE, ModifierFlag.SYNCHRONIZED, ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            bitsOf(ModifierFlag.ABSTRACT),
            AccessLevel.PUBLIC),
    /**
     * A native method of a class.
     */
    CLASS_NATIVE_METHOD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.BRIDGE, ModifierFlag.STATIC, ModifierFlag.SYNTHETIC, ModifierFlag.VARARGS),
            0,
            AccessLevel.PRIVATE),
    /**
     * A non-native static method of a class.
     */
    CLASS_STATIC_METHOD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.BRIDGE, ModifierFlag.FINAL, ModifierFlag.SYNCHRONIZED, ModifierFlag.SYNTHETIC,
                    ModifierFlag.VARARGS),
            bitsOf(ModifierFlag.STATIC),
            AccessLevel.PUBLIC),
    /**
     * An instance field of a class.
     */
    CLASS_INSTANCE_FIELD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.FINAL, ModifierFlag.TRANSIENT, ModifierFlag.VOLATILE, ModifierFlag.SYNTHETIC),
            0,
            AccessLevel.PRIVATE),
    /**
     * A static field of a class.
     */
    CLASS_STATIC_FIELD(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PROTECTED, AccessLevel.PACKAGE_PRIVATE, AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.FINAL, ModifierFlag.VOLATILE, ModifierFlag.SYNTHETIC),
            bitsOf(ModifierFlag.STATIC),
            AccessLevel.PRIVATE),
    /**
     * A top-level class.
     */
    CLASS(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PACKAGE_PRIVATE),
            bitsOf(ModifierFlag.ABSTRACT, ModifierFlag.FINAL, ModifierFlag.SYNTHETIC),
            0,
            AccessLevel.PUBLIC, ModifierFlag.SYNTHETIC),
    /**
     * A top-level interface.
     */
    INTERFACE(
            bitsOf(AccessLevel.PUBLIC, AccessLevel.PACKAGE_PRIVATE),
            bitsOf(ModifierFlag.SYNTHETIC),
            bitsOf(ModifierFlag.ABSTRACT),
            AccessLevel.PUBLIC, ModifierFlag.SYNTHETIC),
    /**
     * An anonymous class.
     */
    ANONYMOUS_CLASS(
            bitsOf(AccessLevel.PRIVATE),
            bitsOf(ModifierFlag.SYNTHETIC),
            bitsOf(ModifierFlag.FINAL),
            AccessLevel.PRIVATE, ModifierFlag.SYNTHETIC),
    /**
     * A parameter of a method or constructor.
     */
    PARAMETER(
            0,
            bitsOf(ModifierFlag.FINAL, ModifierFlag.SYNTHETIC, ModifierFlag.MANDATED),
            0),
    /**
     * A local variable.
     */
    LOCAL_VARIABLE(
            0,
            bitsOf(ModifierFlag.FINAL),
            0),
            ;

    public static final List<ModifierLocation> values = List.of(values());

    // the allowed access levels
    private final byte validAccesses;
    // the allowed settable/clearable flags
    private final byte validFlags;
    // the fixed flags
    private final byte requiredFlags;
    // the initial modifier bits
    private final int defaultModifierBits;

    ModifierLocation(final int validAccesses, final int validFlags, final int requiredFlags,
            final Modifier... defaultModifiers) {
        this.validAccesses = (byte) validAccesses;
        this.validFlags = (byte) validFlags;
        this.requiredFlags = (byte) requiredFlags;
        this.defaultModifierBits = Stream.of(defaultModifiers).filter(this::check).mapToInt(Modifier::mask)
                .reduce(bitsToMask(requiredFlags), (a, b) -> a | b);
    }

    private static int bitsToMask(int requiredFlags) {
        int mask = 0;
        while (requiredFlags != 0) {
            int lob = Integer.lowestOneBit(requiredFlags);
            mask |= ModifierFlag.values.get(Integer.numberOfTrailingZeros(lob)).mask();
            requiredFlags &= ~lob;
        }
        return mask;
    }

    private boolean check(Modifier m) {
        assert supports(m) : "Unexpected invalid default modifier";
        return true;
    }

    private static int bitsOf(Enum<?>... vals) {
        return Stream.of(vals).mapToInt(Enum::ordinal).map(a -> 1 << a).reduce(0, (a, b) -> a | b);
    }

    public boolean supports(Modifier m) {
        return m.validIn(this);
    }

    public boolean supports(AccessLevel level) {
        return (validAccesses & 1 << level.ordinal()) != 0;
    }

    /**
     * {@return {@code true} if the given flag may be set for this element, or {@code false} if it must always be clear}
     */
    public boolean supports(ModifierFlag flag) {
        return (validFlags & 1 << flag.ordinal()) != 0;
    }

    /**
     * {@return {@code true} if the given flag must always be set for this element, or {@code false} if it may be cleared}
     */
    public boolean requires(ModifierFlag flag) {
        return (requiredFlags & 1 << flag.ordinal()) != 0;
    }

    /**
     * {@return the default initial modifier bits for this location}
     */
    public int defaultModifierBits() {
        return defaultModifierBits;
    }
}
