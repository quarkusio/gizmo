package io.quarkus.gizmo2.transformer;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.impl.ModifiableTransformerImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A transformer for an existing class or class member whose modifiers and annotations can be changed.
 */
public sealed interface ModifiableTransformer permits ClassTransformer, FieldTransformer, MethodTransformer,
        ModifiableTransformerImpl {
    /**
     * Add the given modifier flag.
     * Any flags which are mutually exclusive with the given flag are removed.
     *
     * @param flag the flag to add (must not be {@code null})
     */
    void addFlag(ModifierFlag flag);

    /**
     * Remove the given modifier flag.
     *
     * @param flag the flag to remove (must not be {@code null})
     */
    void removeFlag(ModifierFlag flag);

    /**
     * Change the access level.
     *
     * @param access the new access level (must not be {@code null})
     */
    void access(AccessLevel access);

    /**
     * Change the access level to {@code public}.
     */
    default void public_() {
        access(AccessLevel.PUBLIC);
    }

    /**
     * Change the access level to {@code protected}.
     */
    default void protected_() {
        access(AccessLevel.PROTECTED);
    }

    /**
     * Change the access level to package-private.
     */
    default void packagePrivate() {
        access(AccessLevel.PACKAGE_PRIVATE);
    }

    /**
     * Change the access level to {@code private}.
     */
    default void private_() {
        access(AccessLevel.PRIVATE);
    }

    /**
     * Remove the annotation of the given type, if it is present.
     * Both runtime-visible and runtime-invisible annotations are removed.
     *
     * @param annotationType the annotation type (must not be {@code null})
     */
    void removeAnnotation(ClassDesc annotationType);

    /**
     * Remove the annotation of the given type, if it is present.
     * Both runtime-visible and runtime-invisible annotations are removed.
     *
     * @param annotationType the annotation type (must not be {@code null})
     */
    default void removeAnnotation(Class<?> annotationType) {
        removeAnnotation(Util.classDesc(annotationType));
    }
}
