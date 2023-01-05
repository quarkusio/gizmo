package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.quarkus.gizmo.SignatureBuilder.MethodSignatureBuilder;
import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.TypeVariable;

class MethodSignatureBuilderImpl implements MethodSignatureBuilder {

    private Type returnType = Type.voidType();
    private List<Type> parameterTypes = new ArrayList<>();
    private List<TypeVariable> typeParameters = new ArrayList<>();
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

        // params
        signature.append('(');
        for (Type parameterType : parameterTypes) {
            signature.append(parameterType.toSignature());
        }
        signature.append(')');

        // return type
        signature.append(returnType.toSignature());

        // exceptions
        if (!exceptions.isEmpty()) {
            for (Type exceptionType : exceptions) {
                signature.append('^').append(exceptionType.toSignature());
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
    public MethodSignatureBuilder addParameter(Type parameterType) {
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