package io.quarkus.gizmo2.creator;

import static io.github.dmlloyd.classfile.ClassFile.*;
import static io.quarkus.gizmo2.creator.ModifierLocation.*;

import java.util.EnumSet;

/**
 * A modifier for a type or member.
 */
public enum ModifierFlag implements Modifier {
    /**
     * The {@code abstract} modifier.
     */
    ABSTRACT(ACC_ABSTRACT, EnumSet.of(
            CLASS,
            NESTED_CLASS)),
    /**
     * The {@code final} modifier.
     */
    FINAL(ACC_FINAL, EnumSet.of(
            CLASS_CONCRETE_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD,
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD,
            CLASS,
            NESTED_CLASS,
            PARAMETER,
            LOCAL_VARIABLE)),
    /**
     * The "mandated" modifier.
     */
    MANDATED(ACC_MANDATED, EnumSet.of(
            PARAMETER)),
    /**
     * The {@code synchronized} modifier.
     */
    SYNCHRONIZED(ACC_SYNCHRONIZED, EnumSet.of(
            CLASS_CONCRETE_METHOD,
            CLASS_STATIC_METHOD)),
    /**
     * The "synthetic" modifier.
     */
    SYNTHETIC(ACC_SYNTHETIC, EnumSet.of(
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
            NESTED_INTERFACE,
            PARAMETER)),
    /**
     * The {@code transient} modifier.
     */
    TRANSIENT(ACC_TRANSIENT, EnumSet.of(
            CLASS_INSTANCE_FIELD)),
    /**
     * The variable-argument modifier.
     */
    VARARGS(ACC_VARARGS, EnumSet.of(
            INTERFACE_CONCRETE_METHOD,
            INTERFACE_ABSTRACT_METHOD,
            INTERFACE_STATIC_METHOD,
            CLASS_CONSTRUCTOR,
            CLASS_CONCRETE_METHOD,
            CLASS_ABSTRACT_METHOD,
            CLASS_NATIVE_METHOD,
            CLASS_STATIC_METHOD)),
    /**
     * The {@code volatile} modifier.
     */
    VOLATILE(ACC_VOLATILE, EnumSet.of(
            CLASS_INSTANCE_FIELD,
            CLASS_STATIC_FIELD)),
            ;

    private final int mask;
    /**
     * The valid locations.
     * Note that this includes not only locations where the flag may be added, but
     * also locations where it may be <em>removed</em>.
     */
    private final EnumSet<ModifierLocation> validLocations;

    ModifierFlag(final int mask, final EnumSet<ModifierLocation> validLocations) {
        this.mask = mask;
        this.validLocations = validLocations;
    }

    public boolean validIn(final ModifierLocation location) {
        return validLocations.contains(location);
    }

    public int mask() {
        return mask;
    }
}
