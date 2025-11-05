package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

public final class NullConst extends ConstImpl {

    private final DynamicConstantDesc<Object> desc = DynamicConstantDesc.of(ConstantDescs.BSM_NULL_CONSTANT, "_", type());

    public NullConst(final ClassDesc type) {
        super(type);
    }

    public boolean isZero() {
        return true;
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.aconst_null();
        // TODO: work around https://github.com/eclipse-openj9/openj9/issues/22812
        cb.checkcast(type());
    }

    public ConstantDesc desc() {
        return desc;
    }

    public Optional<ConstantDesc> describeConstable() {
        return Optional.of(desc());
    }

    public boolean equals(final ConstImpl obj) {
        return obj instanceof NullConst other && equals(other);
    }

    public boolean equals(final NullConst other) {
        return this == other || other != null && type().equals(other.type());
    }

    public int hashCode() {
        return type().hashCode() + 19;
    }

    public String toString() {
        return "null";
    }

    public StringBuilder toShortString(final StringBuilder b) {
        return Util.descName(b.append('('), type()).append(")null");
    }
}
