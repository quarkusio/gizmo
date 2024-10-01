package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.MethodCreatorImpl;

public sealed interface MethodCreator extends ExecutableCreator permits AbstractMethodCreator, InstanceMethodCreator, StaticMethodCreator, MethodCreatorImpl {

    MethodDesc desc();

    /**
     * Change the return type of this method.
     * The method type is changed with the new return type.
     *
     * @param type the return type (must not be {@code null})
     */
    void returning(ClassDesc type);

    void returning(Class<?> type);
}
