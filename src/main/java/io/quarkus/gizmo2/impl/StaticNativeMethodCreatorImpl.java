package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class StaticNativeMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    StaticNativeMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        modifiers |= ACC_STATIC | ACC_NATIVE;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_NATIVE_METHOD;
    }

    void accept(final Consumer<? super StaticNativeMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), modifiers, mb -> {
            doBody(null, mb);
        });
    }
}
