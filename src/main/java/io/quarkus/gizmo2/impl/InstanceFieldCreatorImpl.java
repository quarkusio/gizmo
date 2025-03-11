package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.InstanceFieldCreator;

public final class InstanceFieldCreatorImpl extends FieldCreatorImpl implements InstanceFieldCreator {

    public InstanceFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc, 0);
    }

    void accept(final Consumer<InstanceFieldCreator> builder) {
        builder.accept(this);
        tc.zb.withField(name(), desc().type(), fb -> {
            fb.withFlags(flags);
            addVisible(fb);
            addInvisible(fb);
        });
    }
}
