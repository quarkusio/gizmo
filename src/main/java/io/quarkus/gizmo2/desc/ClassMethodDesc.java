package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;

import io.quarkus.gizmo2.impl.ClassMethodDescImpl;
import io.quarkus.gizmo2.impl.GizmoImpl;

/**
 * A descriptor for a method on a class.
 */
public sealed interface ClassMethodDesc extends MethodDesc permits ClassMethodDescImpl {
    /**
     * Create a new class method descriptor.
     *
     * @param owner the method owner (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @return the new descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, MethodTypeDesc type) {
        return GizmoImpl.current().classMethodDesc(owner, name, type);
    }

    /**
     * Create a new class method descriptor.
     *
     * @param owner the method owner (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @return the new descriptor (not {@code null})
     */
    static ClassMethodDesc of(ClassDesc owner, String name, MethodType type) {
        return of(owner, name, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }
}
