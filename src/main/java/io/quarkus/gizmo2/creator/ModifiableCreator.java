package io.quarkus.gizmo2.creator;

import java.util.Arrays;
import java.util.Collection;

/**
 * A creator for something which can have modifiers.
 */
public sealed interface ModifiableCreator permits AnnotatableCreator, ExecutableCreator {
    /**
     * {@return the modifier location constant for this creator (not {@code null})}
     */
    ModifierLocation modifierLocation();

    /**
     * Add the given modifier flag to this creator.
     *
     * @param flag the flag to add (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support the given flag
     */
    void withFlag(ModifierFlag flag);

    /**
     * Add the given modifier flags to this creator.
     *
     * @param flags the flags to add (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void withFlags(Collection<ModifierFlag> flags) {
        flags.forEach(this::withFlag);
    }

    /**
     * Add the given modifier flags to this creator.
     *
     * @param flags the flags to add (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void withFlags(ModifierFlag... flags) {
        withFlags(Arrays.asList(flags));
    }

    /**
     * Remove the given modifier flag from this creator.
     *
     * @param flag the flag to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support the given flag
     */
    void withoutFlag(ModifierFlag flag);

    /**
     * Remove the given modifier flags from this creator.
     *
     * @param flags the flags to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void withoutFlags(Collection<ModifierFlag> flags) {
        flags.forEach(this::withoutFlag);
    }

    /**
     * Remove the given modifier flags from this creator.
     *
     * @param flags the flags to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void withoutFlags(ModifierFlag... flags) {
        withFlags(Arrays.asList(flags));
    }

    /**
     * {@return {@code true} if the given modifier is supported by this creator, or {@code false} if it is not}
     *
     * @param modifier the modifier to test (must not be {@code null})
     */
    default boolean supports(Modifier modifier) {
        return modifier.validIn(modifierLocation());
    }

    /**
     * Set the access level of this creator.
     *
     * @param access the access level to set (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support the given access level
     */
    void withAccess(AccessLevel access);

    /**
     * Set the access level of this creator to {@code public}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code public} access level
     */
    default void public_() {
        withAccess(AccessLevel.PUBLIC);
    }

    /**
     * Set the access level of this creator to {@code protected}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code protected} access level
     */
    default void protected_() {
        withAccess(AccessLevel.PROTECTED);
    }

    /**
     * Set the access level of this creator to package-private.
     *
     * @throws IllegalArgumentException if this creator does not support the package-private access level
     */
    default void packagePrivate() {
        withAccess(AccessLevel.PACKAGE_PRIVATE);
    }

    /**
     * Set the access level of this creator to {@code private}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code private} access level
     */
    default void private_() {
        withAccess(AccessLevel.PRIVATE);
    }

    /**
     * Add the {@code final} modifier flag to this creator.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code final} modifier flag
     */
    default void final_() {
        withFlag(ModifierFlag.FINAL);
    }

    /**
     * Add the "synthetic" modifier flag to this creator.
     *
     * @throws IllegalArgumentException if this creator does not support the "synthetic" modifier flag
     */
    default void synthetic() {
        withFlag(ModifierFlag.SYNTHETIC);
    }
}
