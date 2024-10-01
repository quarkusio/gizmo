package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

public final class ClassConstant extends ConstantImpl {

    private final ClassDesc value;

    public ClassConstant(ClassDesc value) {
        super(ConstantDescs.CD_Class);
        this.value = value;
    }

    public ClassConstant(final ConstantDesc constantDesc) {
        this((ClassDesc) constantDesc);
    }

    public ClassDesc desc() {
        return value;
    }

    public Optional<ClassDesc> describeConstable() {
        return Optional.of(desc());
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof ClassConstant other && equals(other);
    }

    public boolean equals(final ClassConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
