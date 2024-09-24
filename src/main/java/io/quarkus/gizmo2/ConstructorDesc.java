package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.lang.invoke.MethodType;
import java.util.List;

import io.quarkus.gizmo2.impl.ConstructorDescImpl;
import io.quarkus.gizmo2.impl.Util;

public sealed interface ConstructorDesc extends MemberDesc permits ConstructorDescImpl {
    static ConstructorDesc of(ClassDesc owner, MethodTypeDesc type) {
        return new ConstructorDescImpl(owner, type);
    }

    static ConstructorDesc of(ClassDesc owner) {
        return of(owner, MethodTypeDesc.of(ConstantDescs.CD_void));
    }

    static ConstructorDesc of(ClassDesc owner, List<ClassDesc> argTypes) {
        return of(owner, MethodTypeDesc.of(ConstantDescs.CD_void, argTypes.toArray(ClassDesc[]::new)));
    }

    static ConstructorDesc of(Class<?> owner, MethodTypeDesc type) {
        return of(Util.classDesc(owner), type);
    }

    static ConstructorDesc of(Class<?> owner, MethodType type) {
        return of(owner, type.describeConstable().orElseThrow(IllegalArgumentException::new));
    }

    static ConstructorDesc of(Class<?> owner, Class<?>... argTypes) {
        return of(owner, MethodType.methodType(void.class, argTypes));
    }

    static ConstructorDesc of(Class<?> owner, List<Class<?>> argTypes) {
        return of(owner, MethodType.methodType(void.class, argTypes));
    }

    MethodTypeDesc type();
}
