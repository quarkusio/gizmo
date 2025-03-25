package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;

public final class LambdaCreatorImpl implements LambdaCreator {
    private final AnonymousClassCreatorImpl acc;
    private final InstanceMethodCreatorImpl sam;

    public LambdaCreatorImpl(final AnonymousClassCreatorImpl acc, final InstanceMethodCreatorImpl sam) {
        this.acc = acc;
        this.sam = sam;
    }

    public ClassDesc type() {
        return acc.type();
    }

    public ParamVar parameter(final String name, final int position) {
        return sam.parameter(name, position);
    }

    public void body(final Consumer<BlockCreator> builder) {
        sam.body(builder);
    }

    public Var capture(final String name, final Expr value) {
        return acc.capture(name, value);
    }
}
