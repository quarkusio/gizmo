package io.quarkus.gizmo2.impl;

import io.quarkus.gizmo2.creator.FieldSignatureCreator;
import io.quarkus.gizmo2.creator.GenericType;

public final class FieldSignatureCreatorImpl implements FieldSignatureCreator {
    GenericType type;

    @Override
    public void type(GenericType type) {
        this.type = type;
    }
}
