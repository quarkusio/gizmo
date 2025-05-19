package io.quarkus.gizmo2.creator;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.List;

/**
 * The possible access levels for an item.
 */
public enum AccessLevel implements Modifier {
    /**
     * The {@code private} access level.
     */
    PRIVATE(ACC_PRIVATE),
    /**
     * The package-private ("default") access level.
     */
    PACKAGE_PRIVATE(0),
    /**
     * The {@code protected} access level.
     */
    PROTECTED(ACC_PROTECTED),
    /**
     * The {@code public} access level.
     */
    PUBLIC(ACC_PUBLIC);

    private final int mask;

    /**
     * The list of access values.
     */
    public static final List<AccessLevel> values = List.of(values());

    AccessLevel(final int mask) {
        this.mask = mask;
    }

    public boolean validIn(ModifierLocation location) {
        return location.supports(this);
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
