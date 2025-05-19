package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.creator.ModifierLocation;

public final class InterfaceStaticFieldCreatorImpl extends StaticFieldCreatorImpl {
    public InterfaceStaticFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(tc, owner, name);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE_STATIC_FIELD;
    }
}
