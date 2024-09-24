package io.quarkus.gizmo2.impl.constant;

import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class StringConstant extends ConstantImpl {

    private final String value;

    public StringConstant(String value) {
        super(ConstantDescs.CD_String);
        this.value = value;
    }

    public StringConstant(final ConstantDesc constantDesc) {
        this((String) constantDesc);
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.ldc(value);
    }

    public String desc() {
        return value;
    }

    public Optional<String> describeConstable() {
        return Optional.of(desc());
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof StringConstant other && equals(other);
    }

    public boolean equals(final StringConstant other) {
        return this == other || other != null && value.equals(other.value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
