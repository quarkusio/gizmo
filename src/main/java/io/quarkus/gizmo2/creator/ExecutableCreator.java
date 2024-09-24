package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.impl.Util;

public sealed interface ExecutableCreator extends MemberCreator permits InstanceExecutableCreator, MethodCreator, StaticExecutableCreator {
    MethodTypeDesc type();

    /**
     * Add a parameter.
     * The method type is changed to include the new parameter.
     *
     * @param name the parameter name (must not be {@code null})
     * @param builder the parameter builder (must not be {@code null})
     * @return the parameter variable (not {@code null})
     */
    ParamVar parameter(String name, Consumer<ParamCreator> builder);

    /**
     * Add a parameter.
     * The method type is changed to include the new parameter.
     *
     * @param name the parameter name (must not be {@code null})
     * @param type the parameter type (must not be {@code null})
     * @return the parameter variable (not {@code null})
     */
    default ParamVar parameter(String name, ClassDesc type) {
        return parameter(name, pc -> {
            pc.withType(type);
        });
    }

    default ParamVar parameter(String name, Class<?> type) {
        return parameter(name, Util.classDesc(type));
    }
}
