package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

public sealed interface FieldVar extends Var permits InstanceFieldVar, StaticFieldVar {
    default ClassDesc type() {
        return desc().type();
    }

    default String name() {
        return desc().name();
    }

    default ClassDesc owner() {
        return desc().owner();
    }

    FieldDesc desc();

    default boolean bound() {
        return false;
    }
}
