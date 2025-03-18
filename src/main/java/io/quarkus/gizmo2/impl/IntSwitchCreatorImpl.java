package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.IntConstant;

/**
 * A switch over {@code int} values.
 */
public final class IntSwitchCreatorImpl extends PerfectHashSwitchCreatorImpl<IntConstant> {
    IntSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, IntConstant.class);
    }

    int staticHash(final IntConstant val) {
        return val.intValue();
    }

    void hash(final CodeBuilder cb) {
        // no operation (int value is already on stack)
    }
}
