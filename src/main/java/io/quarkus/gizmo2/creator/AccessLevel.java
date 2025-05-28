package io.quarkus.gizmo2.creator;

import static io.github.dmlloyd.classfile.ClassFile.*;
import static io.quarkus.gizmo2.creator.ModifierLocation.*;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The possible access levels for an item.
 */
public enum AccessLevel implements Modifier {
    /**
     * The {@code private} access level.
     */
    PRIVATE(ACC_PRIVATE, EnumSet.of(
            INTERFACE_CONCRETE_METHOD,
            INTERFACE_STATIC_METHOD,
            CLASS_CONSTRUCTOR,
            CLASS_CONCRETE_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD,
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD,
            NESTED_CLASS,
            NESTED_INTERFACE)),
    /**
     * The package-private ("default") access level.
     */
    PACKAGE_PRIVATE(0, EnumSet.of(
            CLASS_CONSTRUCTOR,
            CLASS_CONCRETE_METHOD,
            CLASS_ABSTRACT_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD,
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD,
            CLASS,
            INTERFACE,
            NESTED_CLASS,
            NESTED_INTERFACE)),
    /**
     * The {@code protected} access level.
     */
    PROTECTED(ACC_PROTECTED, EnumSet.of(
            CLASS_CONSTRUCTOR,
            CLASS_CONCRETE_METHOD,
            CLASS_ABSTRACT_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD,
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD,
            NESTED_CLASS,
            NESTED_INTERFACE)),
    /**
     * The {@code public} access level.
     */
    PUBLIC(ACC_PUBLIC, EnumSet.of(
            INTERFACE_CONCRETE_METHOD,
            INTERFACE_ABSTRACT_METHOD,
            INTERFACE_STATIC_FIELD,
            INTERFACE_STATIC_METHOD,
            CLASS_CONSTRUCTOR,
            CLASS_CONCRETE_METHOD,
            CLASS_ABSTRACT_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD,
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD,
            CLASS,
            INTERFACE,
            NESTED_CLASS,
            NESTED_INTERFACE));

    private final int mask;
    private final Set<ModifierLocation> validLocations;

    /**
     * The list of access values.
     */
    public static final List<AccessLevel> values = List.of(values());

    AccessLevel(final int mask, final Set<ModifierLocation> validLocations) {
        this.mask = mask;
        this.validLocations = validLocations;
    }

    public boolean validIn(ModifierLocation location) {
        return validLocations.contains(location);
    }

    public int mask() {
        return mask;
    }

    /**
     * {@return the access level indicated by the given bit mask}
     * If multiple access bits are present, then the access level with the highest bit value is returned.
     *
     * @param bits the bit mask
     */
    public static AccessLevel of(int bits) {
        int hob = Integer.highestOneBit(bits & fullMask());
        return switch (hob) {
            case ACC_PROTECTED -> PROTECTED;
            case ACC_PRIVATE -> PRIVATE;
            case ACC_PUBLIC -> PUBLIC;
            default -> PACKAGE_PRIVATE;
        };
    }

    /**
     * {@return the full bitmask for all access levels}
     */
    public static int fullMask() {
        return ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE;
    }
}
