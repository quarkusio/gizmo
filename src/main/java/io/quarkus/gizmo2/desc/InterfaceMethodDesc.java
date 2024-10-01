package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.impl.GizmoImpl;
import io.quarkus.gizmo2.impl.InterfaceMethodDescImpl;

public sealed interface InterfaceMethodDesc extends MethodDesc permits InterfaceMethodDescImpl {
    static InterfaceMethodDesc of(ClassDesc owner, String name, MethodTypeDesc type) {
        return GizmoImpl.current().interfaceMethodDesc(owner, name, type);
    }
}
