package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class PrivateInterfaceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    PrivateInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE_PRIVATE_INSTANCE_METHOD;
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super PrivateInterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
