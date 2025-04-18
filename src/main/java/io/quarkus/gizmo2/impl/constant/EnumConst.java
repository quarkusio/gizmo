package io.quarkus.gizmo2.impl.constant;

import java.util.Objects;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

public final class EnumConst extends ConstImpl {
    private final Enum.EnumDesc<?> desc;
    private final int hashCode;

    public EnumConst(final Enum.EnumDesc<?> desc) {
        super(desc.constantType());
        this.desc = desc;
        this.hashCode = Objects.hash(desc.constantName(), desc.constantType());
    }

    /**
     * {@return the name of the enum constant}
     */
    public String name() {
        return desc.constantName();
    }

    public Enum.EnumDesc<?> desc() {
        return desc;
    }

    public boolean isNonZero() {
        return true;
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof EnumConst other && equals(other);
    }

    public boolean equals(final EnumConst other) {
        return this == other || other != null && name().equals(other.name()) && type().equals(other.type());
    }

    public int hashCode() {
        return hashCode;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getstatic(desc.constantType(), desc.constantName(), desc.constantType());
    }

    public Optional<Enum.EnumDesc<?>> describeConstable() {
        return Optional.of(desc());
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b, desc.constantType()).append('#').append(desc.constantName());
    }
}
