package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.InterfaceMethodDesc;

public final class InterfaceMethodDescImpl extends MethodDescImpl implements InterfaceMethodDesc {
    InterfaceMethodDescImpl(final ClassDesc owner, final String name, final MethodTypeDesc type) {
        super(owner, name, type);
    }

    public boolean equals(final MethodDescImpl obj) {
        return obj instanceof InterfaceMethodDescImpl other && super.equals(other);
    }
}
