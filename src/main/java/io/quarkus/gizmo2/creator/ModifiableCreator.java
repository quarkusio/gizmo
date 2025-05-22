package io.quarkus.gizmo2.creator;

import java.util.Arrays;
import java.util.Collection;

import io.quarkus.gizmo2.impl.ModifiableCreatorImpl;

/**
 * A creator for something which can have modifiers.
 */
public sealed interface ModifiableCreator extends AnnotatableCreator
        permits ExecutableCreator, MemberCreator, ParamCreator, TypeCreator, ModifiableCreatorImpl {
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
    void addFlag(ModifierFlag flag);

    /**
     * Add the given modifier flags to this creator.
     *
     * @param flags the flags to add (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void addFlags(Collection<ModifierFlag> flags) {
        flags.forEach(this::addFlag);
    }

    /**
     * Add the given modifier flags to this creator.
     *
     * @param flags the flags to add (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void addFlags(ModifierFlag... flags) {
        addFlags(Arrays.asList(flags));
    }

    /**
     * Remove the given modifier flag from this creator.
     *
     * @param flag the flag to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support the given flag
     */
    void removeFlag(ModifierFlag flag);

    /**
     * Remove the given modifier flags from this creator.
     *
     * @param flags the flags to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void removeFlags(Collection<ModifierFlag> flags) {
        flags.forEach(this::removeFlag);
    }

    /**
     * Remove the given modifier flags from this creator.
     *
     * @param flags the flags to remove (must not be {@code null})
     * @throws IllegalArgumentException if this creator does not support one of the given flags
     */
    default void removeFlags(ModifierFlag... flags) {
        removeFlags(Arrays.asList(flags));
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
    void setAccess(AccessLevel access);

    /**
     * Set the access level of this creator to {@code public}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code public} access level
     */
    default void public_() {
        setAccess(AccessLevel.PUBLIC);
    }

    /**
     * Set the access level of this creator to {@code protected}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code protected} access level
     */
    default void protected_() {
        setAccess(AccessLevel.PROTECTED);
    }

    /**
     * Set the access level of this creator to package-private.
     *
     * @throws IllegalArgumentException if this creator does not support the package-private access level
     */
    default void packagePrivate() {
        setAccess(AccessLevel.PACKAGE_PRIVATE);
    }

    /**
     * Set the access level of this creator to {@code private}.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code private} access level
     */
    default void private_() {
        setAccess(AccessLevel.PRIVATE);
    }

    /**
     * Add the {@code final} modifier flag to this creator.
     *
     * @throws IllegalArgumentException if this creator does not support the {@code final} modifier flag
     */
    default void final_() {
        addFlag(ModifierFlag.FINAL);
    }

    /**
     * Add the "synthetic" modifier flag to this creator.
     *
     * @throws IllegalArgumentException if this creator does not support the "synthetic" modifier flag
     */
    default void synthetic() {
        addFlag(ModifierFlag.SYNTHETIC);
    }
}
