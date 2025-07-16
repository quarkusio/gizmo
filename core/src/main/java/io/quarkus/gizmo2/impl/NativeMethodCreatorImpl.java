package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class NativeMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    NativeMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        // not a user-visible modifier, so set it explicitly here
        modifiers |= ACC_NATIVE;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_NATIVE_METHOD;
    }

    void accept(final Consumer<? super NativeMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), modifiers, mb -> {
            doBody(null, mb);
        });
    }
}
