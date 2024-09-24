package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.dmlloyd.classfile.attribute.ConstantValueAttribute;
import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticFieldCreator;

public final class StaticFieldCreatorImpl extends FieldCreatorImpl implements StaticFieldCreator {
    private Constant initial;
    private Consumer<BlockCreator> initializer;

    public StaticFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc);
    }

    public void withInitial(final Constant initial) {
        if (this.initial != null || initializer != null) {
            throw new IllegalStateException("A static field may have only one initializer");
        }
        this.initial = Objects.requireNonNull(initial, "initial");
    }

    public void withInitializer(final Function<BlockCreator, Expr> init) {
        if (initial != null || initializer != null) {
            throw new IllegalStateException("A static field may have only one initializer");
        }
        initializer = (b0 -> {
            FieldDesc desc = desc();
            b0.set(Expr.staticField(desc), b0.blockExpr(desc.type(), init));
        });
    }

    void accept(Consumer<StaticFieldCreator> builder) {
        builder.accept(this);
        if (initializer != null) {
            tc.initializer(initializer);
        }
        tc.zb.withField(name(), desc().type(), fb -> {
            fb.withFlags(flags);
            if (initial != null) {
                fb.with(ConstantValueAttribute.of(initial.desc()));
            }
        });
    }
}
