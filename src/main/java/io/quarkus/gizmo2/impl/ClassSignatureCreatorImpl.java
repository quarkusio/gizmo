package io.quarkus.gizmo2.impl;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.gizmo2.creator.ClassSignatureCreator;
import io.quarkus.gizmo2.creator.GenericType;

public final class ClassSignatureCreatorImpl implements ClassSignatureCreator {
    final List<GenericType.TypeVariable> typeParameters = new ArrayList<>();
    GenericType.ReferenceType superClassType = GenericType.ClassType.OBJECT;
    final List<GenericType.ReferenceType> interfaceTypes = new ArrayList<>();

    @Override
    public void addTypeParameter(GenericType.TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
    }

    @Override
    public void extends_(GenericType.ClassType superClassType) {
        this.superClassType = superClassType;
    }

    @Override
    public void extends_(GenericType.ParameterizedType superClassType) {
        this.superClassType = superClassType;
    }

    @Override
    public void implements_(GenericType.ClassType interfaceType) {
        interfaceTypes.add(interfaceType);
    }

    @Override
    public void implements_(GenericType.ParameterizedType interfaceType) {
        interfaceTypes.add(interfaceType);
    }
}
