package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;

import io.quarkus.gizmo2.desc.MethodDesc;

public sealed abstract class MethodDescImpl implements MethodDesc permits ClassMethodDescImpl, InterfaceMethodDescImpl {
    private final ClassDesc owner;
    private final String name;
    private final MethodTypeDesc type;
    private final int hashCode;

    MethodDescImpl(final ClassDesc owner, final String name, final MethodTypeDesc type) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        hashCode = Objects.hash(owner, name, type);
    }

    public ClassDesc owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public MethodTypeDesc type() {
        return type;
    }

    public boolean equals(final Object obj) {
        return obj instanceof MethodDescImpl other && equals(other);
    }

    public boolean equals(final MethodDescImpl other) {
        return this == other || other != null && hashCode == other.hashCode && name.equals(other.name)
                && owner.equals(other.owner) && type.equals(other.type);
    }

    public int hashCode() {
        return hashCode;
    }
}
