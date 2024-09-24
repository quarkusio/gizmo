package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class NullConstant extends ConstantImpl {

    private final DynamicConstantDesc<Object> desc = DynamicConstantDesc.of(ConstantDescs.BSM_NULL_CONSTANT, "_", type());

    public NullConstant(final ClassDesc type) {
        super(type);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.aconst_null();
    }

    public ConstantDesc desc() {
        return desc;
    }

    public Optional<ConstantDesc> describeConstable() {
        return Optional.of(desc());
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof NullConstant other && equals(other);
    }

    public boolean equals(final NullConstant other) {
        return this == other || other != null && type().equals(other.type());
    }

    public int hashCode() {
        return type().hashCode() + 19;
    }
}
