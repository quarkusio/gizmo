package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.creator.GenericType.ClassType;
import io.quarkus.gizmo2.creator.GenericType.ParameterizedType;
import io.quarkus.gizmo2.creator.GenericType.TypeVariable;

/**
 * Creates a generic signature for a class.
 */
public interface ClassSignatureCreator {
    void addTypeParameter(TypeVariable typeParameter);

    void extends_(ClassType superClassType);

    void extends_(ParameterizedType superClassType);

    void implements_(ClassType interfaceType);

    void implements_(ParameterizedType interfaceType);
}
