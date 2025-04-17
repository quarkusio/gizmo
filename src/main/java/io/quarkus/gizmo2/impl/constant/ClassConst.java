package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.quarkus.gizmo2.impl.Util;

public final class ClassConst extends ConstImpl {

    private final ClassDesc value;

    public ClassConst(ClassDesc value) {
        super(ConstantDescs.CD_Class);
        this.value = value;
    }

    public boolean isNonZero() {
        return true;
    }

    public ClassConst(final ConstantDesc constantDesc) {
        this((ClassDesc) constantDesc);
    }

    public ClassDesc desc() {
        return value;
    }

    public Optional<ClassDesc> describeConstable() {
        return Optional.of(desc());
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof ClassConst other && equals(other);
    }

    public boolean equals(final ClassConst other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b.append("Class["), value).append(']');
    }
}
