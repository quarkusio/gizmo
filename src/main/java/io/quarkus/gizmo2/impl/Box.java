package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_Void;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Opcode;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.desc.ClassMethodDesc;

final class Box extends Cast {
    private final Invoke boxing;
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
        super(a, toType);
        ClassDesc boxType = boxing(a.type());
        this.boxing = new Invoke(Opcode.INVOKESTATIC,
                ClassMethodDesc.of(boxType, "valueOf", MethodTypeDesc.of(boxType, a.type())),
                null, List.of(a));
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
        boxing.writeCode(cb, block);
    }
}
