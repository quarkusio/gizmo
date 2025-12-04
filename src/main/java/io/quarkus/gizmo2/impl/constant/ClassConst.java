package io.quarkus.gizmo2.impl.constant;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Short;
import static java.lang.constant.ConstantDescs.CD_Void;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.util.Optional;

import io.quarkus.gizmo2.impl.BlockCreatorImpl;
import io.quarkus.gizmo2.impl.StackMapBuilder;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.classfile.CodeBuilder;

public final class ClassConst extends ConstImpl {

    private final ClassDesc value;

    public ClassConst(ClassDesc value) {
        super(CD_Class);
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

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block, final StackMapBuilder smb) {
        if (value.isPrimitive()) {
            // use the javac translation strategy: read the `TYPE` field from the wrapper class
            // by default, condy would be used, which we don't want
            ClassDesc wrapper = switch (value.descriptorString()) {
                case "B" -> CD_Byte;
                case "S" -> CD_Short;
                case "C" -> CD_Character;
                case "I" -> CD_Integer;
                case "J" -> CD_Long;
                case "F" -> CD_Float;
                case "D" -> CD_Double;
                case "Z" -> CD_Boolean;
                case "V" -> CD_Void;
                default -> throw impossibleSwitchCase(value);
            };
            cb.getstatic(wrapper, "TYPE", CD_Class);
            smb.push(type());
            smb.wroteCode();
        } else {
            super.writeCode(cb, block, smb);
        }
    }
}
