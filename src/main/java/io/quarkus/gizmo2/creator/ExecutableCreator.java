package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.MethodTyped;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.impl.ExecutableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A creator for an executable (i.e. something that can be called with arguments).
 */
public sealed interface ExecutableCreator extends MethodTyped, TypeParameterizedCreator
        permits InstanceExecutableCreator, MethodCreator, StaticExecutableCreator, ExecutableCreatorImpl {
    /**
     * {@return the type descriptor of this executable (not {@code null})}
     */
    MethodTypeDesc type();

    /**
     * Establish the type of this executable.
     * Created parameters will be required to conform to this type.
     * If some parameters have already been created,
     * then their types must match their corresponding types in the given descriptor.
     *
     * @param desc the executable type (must not be {@code null})
     * @throws IllegalStateException if the executable type has already been established
     * @throws IllegalArgumentException if some parameters are already defined,
     *         and their types do not match the given type descriptor
     */
    void withType(MethodTypeDesc desc);

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * it is changed to include the new parameter.
     * If the method type is already set,
     * then any type given to the new parameter must match the type of the next parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param builder the parameter builder (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     */
    ParamVar parameter(String name, Consumer<ParamCreator> builder);

    /**
     * Add a parameter at the given position.
     * If the method type has not yet been established,
     * it is changed to include the new parameter.
     * If the method type is already set,
     * then any type given to the new parameter must match the type of the corresponding parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param position the parameter position, counting up from zero (not including {@code this})
     * @param builder the parameter builder (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     * @throws IndexOutOfBoundsException if the parameter position is out of range
     */
    ParamVar parameter(String name, int position, Consumer<ParamCreator> builder);

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * an exception is thrown.
     * If the method type is already set,
     * then the new parameter will be given the type of the next parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the next position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     */
    default ParamVar parameter(String name) {
        return parameter(name, pc -> {
        });
    }

    /**
     * Add a parameter at the given position.
     * If the method type has not yet been established,
     * an exception is thrown.
     * If the method type is already set,
     * then the new parameter will be given the type of the next parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param position the parameter position, counting up from zero (not including {@code this})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the given position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     * @throws IndexOutOfBoundsException if the parameter position is out of range
     */
    default ParamVar parameter(String name, int position) {
        return parameter(name, position, pc -> {
        });
    }

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * it is changed to include the new parameter.
     * If the method type is already set,
     * then the given type must match the type of the next parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param type the parameter type (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the next position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     */
    default ParamVar parameter(String name, ClassDesc type) {
        return parameter(name, pc -> {
            pc.withType(type);
        });
    }

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * it is changed to include the new parameter,
     * and the position must correspond to the next unset parameter.
     * If the method type is already set,
     * then the given type must match the type of the corresponding parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param type the parameter type (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the given position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     * @throws IndexOutOfBoundsException if the parameter position is out of range
     */
    default ParamVar parameter(String name, int position, ClassDesc type) {
        return parameter(name, position, pc -> {
            pc.withType(type);
        });
    }

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * it is changed to include the new parameter.
     * If the method type is already set,
     * then the given type must match the type of the next parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param type the parameter type (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the next position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     */
    default ParamVar parameter(String name, Class<?> type) {
        return parameter(name, Util.classDesc(type));
    }

    /**
     * Add a parameter.
     * If the method type has not yet been established,
     * it is changed to include the new parameter,
     * and the position must correspond to the next unset parameter.
     * If the method type is already set,
     * then the given type must match the type of the corresponding parameter of the method type.
     *
     * @param name the parameter name (must not be {@code null})
     * @param type the parameter type (must not be {@code null})
     * @return the parameter variable (not {@code null})
     * @throws IllegalStateException if the type of this executable has not yet been established,
     *         or if the parameter with the given position has already been declared
     * @throws IllegalArgumentException if the type does not match the corresponding parameter type in the established method
     *         type, or if the number of parameters exceeds the number of parameters in the established type
     * @throws IndexOutOfBoundsException if the parameter position is out of range
     */
    default ParamVar parameter(String name, int position, Class<?> type) {
        return parameter(name, position, Util.classDesc(type));
    }

    /**
     * Declare that this method throws exceptions of the given type.
     *
     * @param throwableType the generic exception type (must not be {@code null})
     */
    void throws_(GenericType.OfThrows throwableType);

    /**
     * Declare that this method throws exceptions of the given type.
     *
     * @param throwableType the exception type (must not be {@code null})
     */
    default void throws_(ClassDesc throwableType) {
        throws_((GenericType.OfThrows) GenericType.of(throwableType));
    }

    /**
     * Declare that this method throws exceptions of the given type.
     *
     * @param throwableType the exception type (must not be {@code null})
     */
    default void throws_(Class<? extends Throwable> throwableType) {
        throws_(Util.classDesc(throwableType));
    }
}
