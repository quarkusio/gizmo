package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;

import io.quarkus.gizmo2.desc.ConstructorDesc;

public final class ConstructorDescImpl implements ConstructorDesc {
    private final ClassDesc owner;
    private final MethodTypeDesc type;
    private final int hashCode;

    public ConstructorDescImpl(final ClassDesc owner, final MethodTypeDesc type) {
        if (!ConstantDescs.CD_void.equals(type.returnType())) {
            throw new IllegalArgumentException("Constructor descriptor must have a return type of void");
        }
        this.owner = owner;
        this.type = type;
        hashCode = Objects.hash(owner, "<init>", type);
    }

    public ClassDesc owner() {
        return owner;
    }

    public String name() {
        return "<init>";
    }

    public MethodTypeDesc type() {
        return type;
    }

    public boolean equals(final Object obj) {
        return obj instanceof ConstructorDescImpl other && equals(other);
    }

    public boolean equals(final ConstructorDescImpl other) {
        return this == other
                || other != null && hashCode == other.hashCode && owner.equals(other.owner) && type.equals(other.type);
    }

    public int hashCode() {
        return hashCode;
    }

    public StringBuilder toString(final StringBuilder b) {
        b.append("Constructor[");
        Util.descName(b, owner);
        b.append("#<init>");
        b.append(type.descriptorString());
        return b.append(']');
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }
}
