package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.creator.GenericType.ClassType;
import io.quarkus.gizmo2.creator.GenericType.TypeVariable;

/**
 * Creates a generic signature for a method or constructor.
 */
public interface MethodSignatureCreator {
    void addTypeParameter(TypeVariable typeParameter);

    void setReturnType(GenericType returnType);

    void addParameterType(GenericType parameterType);

    void addException(ClassType exceptionType);

    void addException(TypeVariable exceptionType);
}
