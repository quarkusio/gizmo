package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.LongConst;
import io.smallrye.classfile.CodeBuilder;
import io.smallrye.classfile.Label;

/**
 * A switch over {@code long} values.
 */
public final class LongSwitchCreatorImpl extends HashSwitchCreatorImpl<LongConst> {
    LongSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, LongConst.class);
    }

    boolean staticEquals(final LongConst a, final LongConst b) {
        return a.longValue() == b.longValue();
    }

    void equaller(final CodeBuilder cb, final LongConst value, final Label ifEq, final StackMapBuilder smb) {
        value.writeCode(cb, enclosing, smb);
        cb.lcmp();
        cb.ifeq(ifEq);
    }

    int staticHash(final LongConst val) {
        return val.hashCode();
    }

    void hash(final CodeBuilder cb) {
        cb.invokestatic(CD_Long, "hashCode", MethodTypeDesc.of(CD_int, CD_long));
    }
}
