package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

public final class DynamicConst extends ConstImpl {

    private final DynamicConstantDesc<?> desc;

    public DynamicConst(final DynamicConstantDesc<?> desc) {
        super(desc.constantType());
        this.desc = desc;
    }

    public DynamicConst(final ConstantDesc constantDesc) {
        this((DynamicConstantDesc<?>) constantDesc);
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof DynamicConst other && equals(other);
    }

    public boolean equals(final DynamicConst other) {
        return this == other || other != null && desc.equals(other.desc);
    }

    public int hashCode() {
        return desc.hashCode();
    }

    public DynamicConstantDesc<?> desc() {
        return desc;
    }

    public Optional<DynamicConstantDesc<?>> describeConstable() {
        return Optional.of(desc);
    }
}
