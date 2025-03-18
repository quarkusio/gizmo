package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.LongConstant;

/**
 * A switch over {@code long} values.
 */
public final class LongSwitchCreatorImpl extends HashSwitchCreatorImpl<LongConstant> {
    LongSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, LongConstant.class);
    }

    boolean staticEquals(final LongConstant a, final LongConstant b) {
        return a.longValue() == b.longValue();
    }

    void equaller(final CodeBuilder cb, final LongConstant value, final Label ifEq) {
        value.writeCode(cb, enclosing);
        cb.lcmp();
        cb.ifeq(ifEq);
    }

    int staticHash(final LongConstant val) {
        return val.hashCode();
    }

    void hash(final CodeBuilder cb) {
        cb.invokestatic(CD_Long, "hashCode", MethodTypeDesc.of(CD_int, CD_long));
    }
}
