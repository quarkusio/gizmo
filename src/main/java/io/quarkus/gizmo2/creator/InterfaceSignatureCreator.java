package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.creator.GenericType.ClassType;
import io.quarkus.gizmo2.creator.GenericType.ParameterizedType;
import io.quarkus.gizmo2.creator.GenericType.TypeVariable;

/**
 * Creates a generic signature for an interface.
 */
public interface InterfaceSignatureCreator {
    void addTypeParameter(TypeVariable typeParameter);

    void extends_(ClassType interfaceType);

    void extends_(ParameterizedType interfaceType);
}
