package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class DefaultMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    DefaultMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE_DEFAULT_METHOD;
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super DefaultMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
