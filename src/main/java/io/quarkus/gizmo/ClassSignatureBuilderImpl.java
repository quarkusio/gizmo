package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.DotName;

import io.quarkus.gizmo.SignatureBuilder.ClassSignatureBuilder;
import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.ParameterizedType;
import io.quarkus.gizmo.Type.TypeVariable;

class ClassSignatureBuilderImpl implements ClassSignatureBuilder {

    private Type superClass = Type.classType(DotName.OBJECT_NAME);
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private List<Type> superInterfaces = new ArrayList<>();

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
        signature.append(superClass.toSignature());

        // interfaces
        if (!superInterfaces.isEmpty()) {
            for (Type superInterface : superInterfaces) {
                signature.append(superInterface.toSignature());
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
    public ClassSignatureBuilder addSuperInterface(ClassType interfaceType) {
        superInterfaces.add(interfaceType);
        return this;
    }

    @Override
    public ClassSignatureBuilder addSuperInterface(ParameterizedType interfaceType) {
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
            for (Type typeArgument : type.asParameterizedType().typeArguments) {
                if (containsWildcard(typeArgument)) {
                    return true;
                }
            }
        }
        return false;
    }

}