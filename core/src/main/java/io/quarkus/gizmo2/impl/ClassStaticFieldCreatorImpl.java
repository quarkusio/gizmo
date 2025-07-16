package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.creator.ModifierLocation;

public final class ClassStaticFieldCreatorImpl extends StaticFieldCreatorImpl {
    public ClassStaticFieldCreatorImpl(final TypeCreatorImpl tc, final ClassDesc owner, final String name) {
        super(tc, owner, name);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS_STATIC_FIELD;
    }
}
