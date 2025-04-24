package io.quarkus.gizmo2.desc;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.SimpleTyped;
import io.quarkus.gizmo2.impl.FieldDescImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A descriptor for a field.
 */
public sealed interface FieldDesc extends MemberDesc, SimpleTyped permits FieldDescImpl {
    /**
     * Construct a new instance.
     *
     * @param owner the descriptor of the class which contains the field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @param type the descriptor of the field type (must not be {@code null})
     * @return the field descriptor (not {@code null})
     */
    static FieldDesc of(ClassDesc owner, String name, ClassDesc type) {
        checkNotNullParam("owner", owner);
        checkNotNullParam("name", name);
        checkNotNullParam("type", type);
        return new FieldDescImpl(owner, name, type);
    }

    /**
     * Construct a new instance.
     *
     * @param owner the class which contains the field (must not be {@code null})
     * @param name the name of the field (must not be {@code null})
     * @return the field descriptor (not {@code null})
     */
    static FieldDesc of(Class<?> owner, String name) {
        checkNotNullParam("owner", owner);
        checkNotNullParam("name", name);
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
