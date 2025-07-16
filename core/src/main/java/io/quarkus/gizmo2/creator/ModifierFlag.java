package io.quarkus.gizmo2.creator;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.List;
import java.util.function.Consumer;

/**
 * A modifier for a type or member.
 */
public enum ModifierFlag implements Modifier {
    /**
     * The {@code abstract} modifier.
     */
    ABSTRACT(ACC_ABSTRACT) {
        public void forEachExclusive(final Consumer<ModifierFlag> action) {
            action.accept(FINAL);
        }
    },
    /**
     * The "bridge" modifier.
     */
    BRIDGE(ACC_BRIDGE),
    /**
     * The {@code final} modifier.
     */
    FINAL(ACC_FINAL) {
        public void forEachExclusive(final Consumer<ModifierFlag> action) {
            action.accept(ABSTRACT);
            action.accept(VOLATILE);
        }
    },
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
    VOLATILE(ACC_VOLATILE) {
        public void forEachExclusive(final Consumer<ModifierFlag> action) {
            action.accept(FINAL);
        }
    },
    ;

    /**
     * The modifier flag list in order by ordinal.
     */
    public static final List<ModifierFlag> values = List.of(values());

    private final int sets;

    ModifierFlag(final int sets) {
        this.sets = sets;
    }

    public boolean validIn(final ModifierLocation location) {
        return location.supports(this);
    }

    public int mask() {
        return sets;
    }

    /**
     * Process the given action for each flag which is mutually exclusive with this one.
     *
     * @param action the action to process (must not be {@code null})
     */
    public void forEachExclusive(Consumer<ModifierFlag> action) {
    }
}
