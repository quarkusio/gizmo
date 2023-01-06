package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.quarkus.gizmo.SignatureBuilder.MethodSignatureBuilder;
import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.TypeVariable;
import io.quarkus.gizmo.Type.VoidType;

class MethodSignatureBuilderImpl implements MethodSignatureBuilder {
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private Type returnType = VoidType.INSTANCE;
    private List<Type> parameterTypes = new ArrayList<>();
    private List<Type> exceptions = new ArrayList<>();

    @Override
    public String build() {
        StringBuilder signature = new StringBuilder();

        // type params
        if (!typeParameters.isEmpty()) {
            signature.append('<');
            for (TypeVariable typeParameter : typeParameters) {
                typeParameter.appendTypeParameterToSignature(signature);
            }
            signature.append('>');
        }

        // param types
        signature.append('(');
        for (Type parameterType : parameterTypes) {
            parameterType.appendToSignature(signature);
        }
        signature.append(')');

        // return type
        returnType.appendToSignature(signature);

        // exception types
        if (!exceptions.isEmpty()) {
            for (Type exceptionType : exceptions) {
                signature.append('^');
                exceptionType.appendToSignature(signature);
            }
        }

        return signature.toString();
    }

    @Override
    public MethodSignatureBuilder addTypeParameter(TypeVariable typeParameter) {
        typeParameters.add(Objects.requireNonNull(typeParameter));
        return this;
    }

    @Override
    public MethodSignatureBuilder setReturnType(Type returnType) {
        this.returnType = Objects.requireNonNull(returnType);
        return this;
    }

    @Override
    public MethodSignatureBuilder addParameterType(Type parameterType) {
        this.parameterTypes.add(Objects.requireNonNull(parameterType));
        return this;
    }

    @Override
    public MethodSignatureBuilder addException(ClassType exceptionType) {
        this.exceptions.add(Objects.requireNonNull(exceptionType));
        return this;
    }

    @Override
    public MethodSignatureBuilder addException(TypeVariable exceptionType) {
        this.exceptions.add(Objects.requireNonNull(exceptionType));
        return this;
    }
}
