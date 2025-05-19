package io.quarkus.gizmo2.creator;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.List;

/**
 * A modifier for a type or member.
 */
public enum ModifierFlag implements Modifier {
    /**
     * The {@code abstract} modifier.
     */
    ABSTRACT(ACC_ABSTRACT, ACC_FINAL),
    /**
     * The "bridge" modifier.
     */
    BRIDGE(ACC_BRIDGE),
    /**
     * The {@code final} modifier.
     */
    FINAL(ACC_FINAL, ACC_ABSTRACT | ACC_VOLATILE),
    /**
     * The "mandated" modifier.
     */
    MANDATED(ACC_MANDATED),
    /**
     * The {@code static} modifier.
     */
    STATIC(ACC_STATIC),
    /**
     * The {@code synchronized} modifier.
     */
    SYNCHRONIZED(ACC_SYNCHRONIZED),
    /**
     * The "synthetic" modifier.
     */
    SYNTHETIC(ACC_SYNTHETIC),
    /**
     * The {@code transient} modifier.
     */
    TRANSIENT(ACC_TRANSIENT),
    /**
     * The variable-argument modifier.
     */
    VARARGS(ACC_VARARGS),
    /**
     * The {@code volatile} modifier.
     */
    VOLATILE(ACC_VOLATILE, ACC_FINAL),
    ;

    /**
     * The modifier flag list in order by ordinal.
     */
    public static final List<ModifierFlag> values = List.of(values());

    private final int sets;
    private final int clears;

    ModifierFlag(final int sets) {
        this(sets, 0);
    }

    ModifierFlag(final int sets, final int clears) {
        this.sets = sets;
        this.clears = clears;
    }

    public boolean validIn(final ModifierLocation location) {
        return location.supports(this);
    }

    public int mask() {
        return sets;
    }

    /**
     * {@return the mask of bits that are cleared when this modifier flag is set}
     */
    public int clearMask() {
        return clears;
    }
}
