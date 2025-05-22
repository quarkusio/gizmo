package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class InterfaceMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    InterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        modifiers |= ACC_PUBLIC | ACC_ABSTRACT;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE_ABSTRACT_METHOD;
    }

    void accept(final Consumer<? super InterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), modifiers, mb -> {
            doBody(null, mb);
        });
    }
}
