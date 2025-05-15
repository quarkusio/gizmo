package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;

import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;

public final class AbstractMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    AbstractMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name);
        flags |= ACC_PUBLIC | ACC_ABSTRACT;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_ABSTRACT_METHOD;
    }

    void accept(final Consumer<? super AbstractMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
