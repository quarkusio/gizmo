package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;

import io.quarkus.gizmo2.GenericType;
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
        this.hashCode = buildHashCode();
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

    public ClassDesc returnType() {
        return type.returnType();
    }

    public GenericType genericReturnType() {
        return GenericType.of(type().returnType());
    }

    public boolean hasGenericReturnType() {
        return false;
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

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    private int buildHashCode() {
        int result = Objects.hashCode(owner.descriptorString());
        result = 31 * result + Objects.hashCode(name);
        result = 31 * result + typeHash();
        return result;
    }

    private int typeHash() {
        int result = type.returnType().descriptorString().hashCode();
        int pc = type.parameterCount();
        for (int i = 0; i < pc; i++) {
            result = 31 * result + type.parameterType(i).descriptorString().hashCode();
        }
        return result;
    }
}
