package io.quarkus.gizmo2.impl.constant;

import static java.lang.constant.ConstantDescs.CD_Class;
import static java.lang.constant.ConstantDescs.CD_ConstantBootstraps;
import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.DirectMethodHandleDesc;
import java.lang.constant.DynamicConstantDesc;
import java.util.Optional;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.impl.BlockCreatorImpl;

public final class StaticFinalFieldConstant extends ConstantImpl {
    private static final DirectMethodHandleDesc BSM_GET_STATIC_FINAL2
        = ConstantDescs.ofConstantBootstrap(CD_ConstantBootstraps, "getStaticFinal",
            CD_Object, CD_Class, CD_Class);

    private final FieldDesc fieldDesc;

    public StaticFinalFieldConstant(final FieldDesc fieldDesc) {
        super(fieldDesc.type());
        this.fieldDesc = fieldDesc;
    }

    public boolean equals(final ConstantImpl obj) {
        return obj instanceof StaticFinalFieldConstant other && equals(other);
    }

    public boolean equals(final StaticFinalFieldConstant other) {
        return this == other || other != null && fieldDesc.equals(other.fieldDesc);
    }

    public int hashCode() {
        return fieldDesc.hashCode();
    }

    public void writeCode(final CodeBuilder cb, final BlockCreatorImpl block) {
        cb.getstatic(fieldDesc.owner(), fieldDesc.name(), fieldDesc.type());
    }

    public ConstantDesc desc() {
        String name = fieldDesc.name();
        ClassDesc type = fieldDesc.type();
        ClassDesc owner = fieldDesc.owner();
        if (type.equals(owner)) {
            return DynamicConstantDesc.ofNamed(
                ConstantDescs.BSM_GET_STATIC_FINAL,
                name,
                owner
            );
        } else {
            return DynamicConstantDesc.ofNamed(
                BSM_GET_STATIC_FINAL2,
                name,
                type,
                owner
            );
        }
    }

    public Optional<? extends ConstantDesc> describeConstable() {
        return Optional.of(desc());
    }

    public StringBuilder toShortString(final StringBuilder b) {
        b.append("StaticFinalField[");
        fieldDesc.toString(b);
        return b.append(']');
    }
}
