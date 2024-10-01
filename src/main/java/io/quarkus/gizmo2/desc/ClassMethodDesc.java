package io.quarkus.gizmo2.desc;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;

import io.quarkus.gizmo2.impl.ClassMethodDescImpl;
import io.quarkus.gizmo2.impl.GizmoImpl;

public sealed interface ClassMethodDesc extends MethodDesc permits ClassMethodDescImpl {
    static ClassMethodDesc of(ClassDesc owner, String name, MethodTypeDesc type) {
        return GizmoImpl.current().classMethodDesc(owner, name, type);
    }

    static ClassMethodDesc of(ClassDesc owner, String name, MethodType type) {
        return of(owner, name, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }
}
