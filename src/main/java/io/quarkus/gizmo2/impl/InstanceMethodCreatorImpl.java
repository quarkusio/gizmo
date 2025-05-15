package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super InstanceMethodCreatorImpl> builder) {
        builder.accept(this);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_CONCRETE_METHOD;
    }
}
