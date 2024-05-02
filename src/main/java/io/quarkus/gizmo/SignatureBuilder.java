package io.quarkus.gizmo;

import io.quarkus.gizmo.Type.ClassType;
import io.quarkus.gizmo.Type.ParameterizedType;
import io.quarkus.gizmo.Type.TypeVariable;

/**
 * Builds a generic signature as defined in JVMS 17, chapter "4.7.9.1. Signatures".
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
     * @return the generic signature
     */
    String build();

    /**
     * Builds a generic signature of a class (including interfaces).
     */
    interface ClassSignatureBuilder extends SignatureBuilder {
        ClassSignatureBuilder addTypeParameter(TypeVariable typeParameter);

        ClassSignatureBuilder setSuperClass(ClassType superClass);

        ClassSignatureBuilder setSuperClass(ParameterizedType superClass);

        ClassSignatureBuilder addInterface(ClassType interfaceType);

        ClassSignatureBuilder addInterface(ParameterizedType interfaceType);
    }

    /**
     * Builds a generic signature of a method (including constructors).
     */
    interface MethodSignatureBuilder extends SignatureBuilder {
        MethodSignatureBuilder addTypeParameter(TypeVariable typeParameter);

        MethodSignatureBuilder setReturnType(Type returnType);

        MethodSignatureBuilder addParameterType(Type parameterType);

        MethodSignatureBuilder addException(ClassType exceptionType);

        MethodSignatureBuilder addException(TypeVariable exceptionType);
    }

    /**
     * Builds a generic signature of a field. Also usable for building generic signatures of record components.
     */
    interface FieldSignatureBuilder extends SignatureBuilder {
        FieldSignatureBuilder setType(Type type);
    }
}
