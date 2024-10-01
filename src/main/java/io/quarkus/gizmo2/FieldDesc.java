package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.Objects;

import io.quarkus.gizmo2.desc.MemberDesc;
import io.quarkus.gizmo2.impl.FieldDescImpl;
import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.Util;

public sealed interface FieldDesc extends MemberDesc permits FieldDescImpl {
    static FieldDesc of(ClassDesc owner, String name, ClassDesc type) {
        return GizmoImpl.current().fieldDesc(owner, name, type);
    }

    static FieldDesc of(Class<?> owner, String name) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(name, "name");
        try {
            return of(Util.classDesc(owner), name, Util.classDesc(owner.getDeclaredField(name).getType()));
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("No such field \"" + name + "\" found on " + owner);
        }
    }

    ClassDesc type();
}
