package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import io.github.dmlloyd.classfile.TypeKind;
import io.quarkus.gizmo2.creator.ExecutableCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * A typed thing whose type is a method type.
 */
public sealed interface MethodTyped extends Typed permits MethodDesc, ExecutableCreator {

    /**
     * {@return the descriptor of the method's type}
     */
    MethodTypeDesc type();

    /**
     * {@return the return type}
     */
    default ClassDesc returnType() {
        return type().returnType();
    }

    /**
     * {@return the type kind of the return type (not {@code null})}
     */
    default TypeKind returnTypeKind() {
        return TypeKind.from(returnType());
    }

    /**
     * {@return the number of slots occupied by the return type}
     */
    default int returnSlotSize() {
        return returnTypeKind().slotSize();
    }

    /**
     * {@return {@code true} if the return type is {@code void}, or {@code false} otherwise}
     */
    default boolean isVoidReturn() {
        return returnTypeKind() == TypeKind.VOID;
    }

    /**
     * {@return {@code true} if the return type is a primitive type, or {@code false} if it is not}
     */
    default boolean isPrimitiveReturn() {
        return returnType().isPrimitive();
    }

    /**
     * {@return the list of parameter types}
     */
    default List<ClassDesc> parameterTypes() {
        return type().parameterList();
    }

    /**
     * {@return the number of parameters}
     */
    default int parameterCount() {
        return type().parameterCount();
    }

    /**
     * {@return the type of the parameter with the given index}
     *
     * @param idx the parameter index
     * @throws IndexOutOfBoundsException if the parameter index is out of bounds
     */
    default ClassDesc parameterType(int idx) {
        return type().parameterType(idx);
    }

    /**
     * {@return the type kind of the parameter with the given index}
     *
     * @param idx the parameter index
     * @throws IndexOutOfBoundsException if the parameter index is out of bounds
     */
    default TypeKind parameterTypeKind(int idx) {
        return TypeKind.from(parameterType(idx));
    }

    /**
     * {@return the number of slots occupied by the parameter with the given index}
     *
     * @param idx the parameter index
     * @throws IndexOutOfBoundsException if the parameter index is out of bounds
     */
    default int parameterSlotSize(int idx) {
        return parameterTypeKind(idx).slotSize();
    }

    /**
     * {@return {@code true} if the type of the parameter with the given index is a primitive type, or {@code false} if it is
     * not}
     *
     * @param idx the parameter index
     * @throws IndexOutOfBoundsException if the parameter index is out of bounds
     */
    default boolean isPrimitiveParameter(int idx) {
        return parameterType(idx).isPrimitive();
    }
}
