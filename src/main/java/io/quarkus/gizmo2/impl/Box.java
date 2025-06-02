package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.GenericType;

final class Box extends Cast {
    private boolean bound;

    private static ClassDesc boxing(ClassDesc unboxType) {
        ClassDesc boxType = Conversions.boxingConversion(unboxType)
                .orElseThrow(() -> new IllegalArgumentException("No box type for " + unboxType.displayName()));
        if (boxType.equals(CD_Void)) {
            throw new IllegalArgumentException("Cannot box void");
        }
        return boxType;
    }

    Box(Expr a) {
        this(a, boxing(a.type()));
    }

    Box(Expr a, ClassDesc toType) {
        super(a, GenericType.of(toType));
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
        ClassDesc boxType = boxing(a.type());
        cb.invoke(Opcode.INVOKESTATIC, boxType, "valueOf", MethodTypeDesc.of(boxType, a.type()), false);
    }
}
