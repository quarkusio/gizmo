package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.IntConst;

/**
 * A switch over {@code int} values.
 */
public final class IntSwitchCreatorImpl extends PerfectHashSwitchCreatorImpl<IntConst> {
    IntSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, IntConst.class);
    }

    int staticHash(final IntConst val) {
        return val.intValue();
    }

    void hash(final CodeBuilder cb) {
        // no operation (int value is already on stack)
    }
}
