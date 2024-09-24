package io.quarkus.gizmo2;

import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import io.quarkus.gizmo2.impl.MethodDescImpl;
import io.quarkus.gizmo2.impl.Util;

public sealed interface MethodDesc extends MemberDesc permits ClassMethodDesc, InterfaceMethodDesc, MethodDescImpl {
    static MethodDesc of(Class<?> owner, String name, MethodTypeDesc type) {
        return owner.isInterface() ? InterfaceMethodDesc.of(Util.classDesc(owner), name, type) : ClassMethodDesc.of(Util.classDesc(owner), name, type);
    }

    static MethodDesc of(Class<?> owner, String name, MethodType type) {
        return of(owner, name, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }

    static MethodDesc of(Class<?> owner, String name, Class<?> returning, Class<?>... argTypes) {
        return of(owner, name, MethodType.methodType(returning, argTypes));
    }

    static MethodDesc of(Class<?> owner, String name, Class<?> returning, List<Class<?>> argTypes) {
        return of(owner, name, MethodType.methodType(returning, argTypes));
    }

    MethodTypeDesc type();
}
