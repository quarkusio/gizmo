package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.creator.ParamCreator;
import io.quarkus.gizmo2.creator.SwitchCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.desc.FieldDesc;

/**
 * A typed thing whose type is a simple type.
 */
public sealed interface SimpleTyped extends Typed
        permits Expr, FieldDesc, BlockCreator, ClassCreator, FieldCreator, ParamCreator, SwitchCreator, TypeCreator {
    /**
     * {@return the type of this entity (not {@code null})}
     */
    ClassDesc type();

    /**
     * {@return the type kind of this entity (not {@code null})}
     */
    default TypeKind typeKind() {
        return TypeKind.from(type());
    }

    /**
     * {@return the number of slots occupied by this entity}
     */
    default int slotSize() {
        return typeKind().slotSize();
    }

    /**
     * {@return {@code true} if this entity has {@code void} type, or {@code false} otherwise}
     */
    default boolean isVoid() {
        return typeKind() == TypeKind.VOID;
    }

    /**
     * {@return {@code true} if this entity has a primitive type, or {@code false} if it does not}
     */
    default boolean isPrimitive() {
        return type().isPrimitive();
    }
}
