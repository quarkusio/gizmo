package io.quarkus.gizmo2.impl;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.gizmo2.creator.GenericType;
import io.quarkus.gizmo2.creator.MethodSignatureCreator;

public final class MethodSignatureCreatorImpl implements MethodSignatureCreator {
    final List<GenericType.TypeVariable> typeParameters = new ArrayList<>();
    GenericType returnType = GenericType.voidType();
    final List<GenericType> parameterTypes = new ArrayList<>();
    final List<GenericType.ReferenceType> exceptionTypes = new ArrayList<>();

    @Override
    public void addTypeParameter(GenericType.TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
    }

    @Override
    public void setReturnType(GenericType returnType) {
        this.returnType = returnType;
    }

    @Override
    public void addParameterType(GenericType parameterType) {
        parameterTypes.add(parameterType);
    }

    @Override
    public void addException(GenericType.ClassType exceptionType) {
        exceptionTypes.add(exceptionType);
    }

    @Override
    public void addException(GenericType.TypeVariable exceptionType) {
        exceptionTypes.add(exceptionType);
    }
}
