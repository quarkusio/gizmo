package io.quarkus.gizmo2.impl;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.gizmo2.creator.GenericType;
import io.quarkus.gizmo2.creator.InterfaceSignatureCreator;

public final class InterfaceSignatureCreatorImpl implements InterfaceSignatureCreator {
    final List<GenericType.TypeVariable> typeParameters = new ArrayList<>();
    final List<GenericType.ReferenceType> interfaceTypes = new ArrayList<>();

    @Override
    public void addTypeParameter(GenericType.TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
    }

    @Override
    public void extends_(GenericType.ClassType interfaceType) {
        interfaceTypes.add(interfaceType);
    }

    @Override
    public void extends_(GenericType.ParameterizedType interfaceType) {
        interfaceTypes.add(interfaceType);
    }
}
