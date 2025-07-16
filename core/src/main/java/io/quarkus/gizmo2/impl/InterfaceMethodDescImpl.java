package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.desc.InterfaceMethodDesc;

public final class InterfaceMethodDescImpl extends MethodDescImpl implements InterfaceMethodDesc {
    public InterfaceMethodDescImpl(final ClassDesc owner, final String name, final MethodTypeDesc type) {
        super(owner, name, type);
    }

    public boolean equals(final MethodDescImpl obj) {
        return obj instanceof InterfaceMethodDescImpl other && super.equals(other);
    }

    public StringBuilder toString(final StringBuilder b) {
        b.append("InterfaceMethod[");
        Util.descName(b, owner());
        b.append('#').append(name());
        b.append(type().descriptorString());
        return b.append(']');
    }
}
