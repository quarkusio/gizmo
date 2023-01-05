package io.quarkus.gizmo;

import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.ParameterizedType;
import io.quarkus.gizmo.Type.TypeVariable;

/**
 * Builds a signature as defined in JVMS 17, chapter "4.7.9.1. Signatures".
 * 
 * @see SignatureElement#setSignature(String)
 */
public interface SignatureBuilder {

    static ClassSignatureBuilder forClass() {
        return new ClassSignatureBuilderImpl();
    }

    static MethodSignatureBuilder forMethod() {
        return new MethodSignatureBuilderImpl();
    }

    static FieldSignatureBuilder forField() {
        return new FieldSignatureBuilderImpl();
    }

    /**
     * @return the signature
     */
    String build();

    interface ClassSignatureBuilder extends SignatureBuilder {

        ClassSignatureBuilder addTypeParameter(TypeVariable typeParameter);

        ClassSignatureBuilder setSuperClass(ClassType superClass);
        
        ClassSignatureBuilder setSuperClass(ParameterizedType superClass);

        ClassSignatureBuilder addSuperInterface(ClassType interfaceType);
        
        ClassSignatureBuilder addSuperInterface(ParameterizedType interfaceType);
    }

    interface MethodSignatureBuilder extends SignatureBuilder {

        MethodSignatureBuilder addTypeParameter(TypeVariable typeParameter);

        MethodSignatureBuilder setReturnType(Type returnType);

        MethodSignatureBuilder addParameter(Type parameter);

        MethodSignatureBuilder addException(ClassType exceptionType);
        
        MethodSignatureBuilder addException(TypeVariable exceptionType);
    }

    interface FieldSignatureBuilder extends SignatureBuilder {

        FieldSignatureBuilder setType(Type type);
    }

}
