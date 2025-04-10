package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.smallrye.common.constraint.Assert;

public final class InstanceFieldCreatorImpl extends FieldCreatorImpl implements InstanceFieldCreator {

    private Constant initial;
    private Consumer<BlockCreator> initializer;

    public InstanceFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc, Set.of());
    }

    public void withInitial(final Constant initial) {
        Assert.checkNotNullParam("initial", initial);
        checkOneInit();
        this.initial = initial;
    }

    public void withInitializer(final Consumer<BlockCreator> init) {
        Assert.checkNotNullParam("init", init);
        checkOneInit();
        initializer = (b0 -> {
            FieldDesc desc = desc();
            // todo: replace ThisExpr from a shared instance on TypeCreator
            b0.set(new ThisExpr(owner()).field(desc), b0.blockExpr(desc.type(), init));
        });
    }

    private void checkOneInit() {
        if (initial != null || initializer != null) {
            throw new IllegalStateException("An instance field may have only one initializer");
        }
    }

    void accept(final Consumer<InstanceFieldCreator> builder) {
        builder.accept(this);
        if (initial != null) {
            // todo: replace ThisExpr from a shared instance on TypeCreator
            tc.instancePreinitializer(b0 -> b0.set(new ThisExpr(owner()).field(desc()), initial));
        } else if (initializer != null) {
            tc.instanceInitializer(initializer);
        }
        tc.zb.withField(name(), desc().type(), fb -> {
            fb.withFlags(flags);
            addVisible(fb);
            addInvisible(fb);
        });
    }
}
