package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.TypeKind;

final class Unbox extends Cast {
    private static ClassDesc unboxing(ClassDesc boxType) {
        ClassDesc unboxType = Conversions.unboxingConversion(boxType)
                .orElseThrow(() -> new IllegalArgumentException("No unbox type for " + boxType.displayName()));
        if (unboxType.equals(CD_void)) {
            throw new IllegalArgumentException("Cannot unbox void");
        }
        return unboxType;
    }

    Unbox(Expr a) {
        super(a, unboxing(a.type()), null);
    }

    @Override
    public void writeCode(CodeBuilder cb, BlockCreatorImpl block) {
        cb.invoke(Opcode.INVOKEVIRTUAL, a.type(), switch (TypeKind.from(type())) {
            case BOOLEAN -> "booleanValue";
            case BYTE -> "byteValue";
            case CHAR -> "charValue";
            case SHORT -> "shortValue";
            case INT -> "intValue";
            case LONG -> "longValue";
            case FLOAT -> "floatValue";
            case DOUBLE -> "doubleValue";
            default -> throw impossibleSwitchCase(TypeKind.from(type()));
        }, MethodTypeDesc.of(type()), false);
    }
}
