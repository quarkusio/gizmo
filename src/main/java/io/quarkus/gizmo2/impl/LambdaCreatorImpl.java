package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;

/**
 * 
 */
public final class LambdaCreatorImpl implements LambdaCreator {
    public ClassDesc type() {
        return null;
    }

    public ParamVar param(final String name, final int position) {
        return null;
    }

    public void body(final Consumer<BlockCreator> builder) {

    }

    public Var capture(final String name, final Expr value) {
        return null;
    }
}
