package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.InterfaceMethodDescImpl;

/**
 * A descriptor for a method on an interface.
 */
public sealed interface InterfaceMethodDesc extends MethodDesc permits InterfaceMethodDescImpl {
    /**
     * Create a new interface method descriptor.
     *
     * @param owner the method owner (must not be {@code null})
     * @param name the method name (must not be {@code null})
     * @param type the method type (must not be {@code null})
     * @return the new descriptor (not {@code null})
     */
    static InterfaceMethodDesc of(ClassDesc owner, String name, MethodTypeDesc type) {
        return GizmoImpl.current().interfaceMethodDesc(owner, name, type);
    }
}
