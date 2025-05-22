package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import io.quarkus.gizmo2.desc.FieldDesc;

/**
 * A variable corresponding to a field.
 */
public sealed interface FieldVar extends Var permits InstanceFieldVar, StaticFieldVar {
    default ClassDesc type() {
        return desc().type();
    }

    default String name() {
        return desc().name();
    }

    /**
     * {@return the descriptor of the class which contains the field}
     */
    default ClassDesc owner() {
        return desc().owner();
    }

    /**
     * {@return the descriptor of the field}
     */
    FieldDesc desc();
}
