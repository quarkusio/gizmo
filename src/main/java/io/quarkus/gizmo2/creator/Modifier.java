package io.quarkus.gizmo2.creator;

/**
 * Some kind of modifier (flag or access level).
 */
public sealed interface Modifier permits Access, ModifierFlag {
    /**
     * {@return {@code true} if this modifier is valid in the given location, or {@code false} if it is invalid}
     */
    boolean validIn(ModifierLocation location);

    /**
     * {@return the bitmask of this modifier}
     */
    int mask();
}
