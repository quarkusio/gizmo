package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.Objects;

import io.quarkus.gizmo2.desc.MemberDesc;
import io.quarkus.gizmo2.impl.FieldDescImpl;
import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A descriptor for a field.
 */
public sealed interface FieldDesc extends MemberDesc permits FieldDescImpl {
    /**
     * Construct a new instance.
     *
     * @param owner the descriptor of the class which contains the field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @param type the descriptor of the field type (must not be {@code null})
     * @return the field descriptor (not {@code null})
     */
    static FieldDesc of(ClassDesc owner, String name, ClassDesc type) {
        return GizmoImpl.current().fieldDesc(owner, name, type);
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @return the field descriptor (not {@code null})
     */
    static FieldDesc of(Class<?> owner, String name) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        try {
            return of(Util.classDesc(owner), name, Util.classDesc(owner.getDeclaredField(name).getType()));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field \"" + name + "\" found on " + owner);
        }
    }

    /**
     * {@return the descriptor of the field type}
     */
    ClassDesc type();
}
