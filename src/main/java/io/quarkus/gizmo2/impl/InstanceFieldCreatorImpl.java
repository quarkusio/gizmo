package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.InstanceFieldCreator;

public final class InstanceFieldCreatorImpl extends FieldCreatorImpl implements InstanceFieldCreator {

    public InstanceFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(owner, name, tc);
    }

    void accept(final Consumer<InstanceFieldCreator> builder) {
        tc.zb.withField(name(), desc().type(), fb -> {
            builder.accept(this);
            fb.withFlags(flags);
            addVisible(fb);
            addInvisible(fb);
        });
    }
}
