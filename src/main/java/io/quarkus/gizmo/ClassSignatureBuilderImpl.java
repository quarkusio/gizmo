package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.gizmo.SignatureBuilder.ClassSignatureBuilder;
import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.ParameterizedType;
import io.quarkus.gizmo.Type.TypeVariable;

class ClassSignatureBuilderImpl implements ClassSignatureBuilder {
    
    List<TypeVariable> typeParameters = new ArrayList<>();
    Type superClass = ClassType.OBJECT;
    List<Type> superInterfaces = new ArrayList<>();

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

        // superclass
        superClass.appendToSignature(signature);

        // interfaces
        if (!superInterfaces.isEmpty()) {
            for (Type superInterface : superInterfaces) {
                superInterface.appendToSignature(signature);
            }
        }
        return signature.toString();
    }

    @Override
    public ClassSignatureBuilder addTypeParameter(TypeVariable typeParameter) {
        typeParameters.add(typeParameter);
        return this;
    }

    @Override
    public ClassSignatureBuilder setSuperClass(ClassType superClass) {
        this.superClass = superClass;
        return this;
    }

    @Override
    public ClassSignatureBuilder setSuperClass(ParameterizedType superClass) {
        if (containsWildcard(superClass)) {
            throw new IllegalArgumentException("A super type may not specify a wilcard");
        }

        this.superClass = superClass;
        return this;
    }

    @Override
    public ClassSignatureBuilder addInterface(ClassType interfaceType) {
        superInterfaces.add(interfaceType);
        return this;
    }

    @Override
    public ClassSignatureBuilder addInterface(ParameterizedType interfaceType) {
        if (containsWildcard(interfaceType)) {
            throw new IllegalArgumentException("A super type may not specify a wilcard");
        }
        superInterfaces.add(interfaceType);
        return this;
    }

    private boolean containsWildcard(Type type) {
        if (type.isWildcard()) {
            return true;
        } else if (type.isParameterizedType()) {
            for (Type typeArgument : type.asParameterizedType().getTypeArguments()) {
                if (containsWildcard(typeArgument)) {
                    return true;
                }
            }
        }
        return false;
    }
}
