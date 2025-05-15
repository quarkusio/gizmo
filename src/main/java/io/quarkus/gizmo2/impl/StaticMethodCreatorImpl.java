package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.creator.StaticMethodCreator;

public final class StaticMethodCreatorImpl extends MethodCreatorImpl implements StaticMethodCreator {
    StaticMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        flags |= ACC_STATIC;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_STATIC_METHOD;
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    int firstSlot() {
        return 0;
    }

    void accept(final Consumer<? super StaticMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
