package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.Item;

/**
 * An instance field variable.
 */
public sealed interface InstanceFieldVar extends FieldVar permits Item.FieldDeref {
}
