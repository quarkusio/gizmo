package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;
import io.quarkus.gizmo2.desc.ClassMethodDesc;

final class Unbox extends Cast {
    private final Invoke unboxing;
    private boolean bound;

    private static ClassDesc unboxing(ClassDesc boxType) {
        ClassDesc unboxType = Conversions.unboxingConversion(boxType)
                .orElseThrow(() -> new IllegalArgumentException("No unbox type for " + boxType.displayName()));
        if (unboxType.equals(CD_void)) {
            throw new IllegalArgumentException("Cannot unbox void");
        }
        return unboxType;
    }

    Unbox(Expr a) {
        super(a, unboxing(a.type()));
        this.unboxing = new Invoke(Opcode.INVOKEVIRTUAL,
                ClassMethodDesc.of(a.type(), switch (TypeKind.from(toType)) {
                    case BOOLEAN -> "booleanValue";
                    case BYTE -> "byteValue";
                    case CHAR -> "charValue";
                    case SHORT -> "shortValue";
                    case INT -> "intValue";
                    case LONG -> "longValue";
                    case FLOAT -> "floatValue";
                    case DOUBLE -> "doubleValue";
                    default -> throw impossibleSwitchCase(TypeKind.from(toType));
                }, MethodTypeDesc.of(toType, Util.NO_DESCS)),
                a, List.of());
    }

    @Override
    public boolean bound() {
        return bound;
    }

    @Override
    protected void bind() {
        bound = true;
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        unboxing.writeCode(cb, block);
    }
}
