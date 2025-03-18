package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.EnumConstant;

/**
 * A switch over {@code enum} values.
 */
public final class EnumSwitchCreatorImpl extends HashSwitchCreatorImpl<EnumConstant> {
    EnumSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, EnumConstant.class);
    }

    boolean staticEquals(final EnumConstant a, final EnumConstant b) {
        return a.equals(b);
    }

    void equaller(final CodeBuilder cb, final EnumConstant value, final Label ifEq) {
        value.writeCode(cb, enclosing);
        cb.if_acmpeq(ifEq);
    }

    int staticHash(final EnumConstant val) {
        return val.name().hashCode();
    }

    void hash(final CodeBuilder cb) {
        cb.invokevirtual(CD_Enum, "name", MethodTypeDesc.of(CD_String));
        cb.invokevirtual(CD_String, "hashCode", MethodTypeDesc.of(CD_int));
    }
}
