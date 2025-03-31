package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Objects;

import io.quarkus.gizmo2.desc.FieldDesc;

public final class FieldDescImpl implements FieldDesc {
    private final ClassDesc owner;
    private final String name;
    private final ClassDesc type;
    private final int hashCode;

    FieldDescImpl(final ClassDesc owner, final String name, final ClassDesc type) {
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

    public ClassDesc type() {
        return type;
    }

    public boolean equals(final Object obj) {
        return obj instanceof FieldDescImpl other && equals(other);
    }

    public boolean equals(final FieldDescImpl other) {
        return this == other || other != null && hashCode == other.hashCode && name.equals(other.name) && owner.equals(other.owner) && type.equals(other.type);
    }

    public int hashCode() {
        return hashCode;
    }

    public StringBuilder toString(final StringBuilder b) {
        return Util.descName(Util.descName(b, owner).append('#').append(name).append(':'), type);
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }
}
