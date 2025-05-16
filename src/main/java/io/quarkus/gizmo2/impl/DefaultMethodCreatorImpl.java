package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class DefaultMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    DefaultMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        flags |= ACC_PUBLIC;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE_CONCRETE_METHOD;
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super DefaultMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
