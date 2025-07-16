package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.FieldDeref;

/**
 * An instance field variable.
 */
public sealed interface InstanceFieldVar extends FieldVar permits FieldDeref {
}
