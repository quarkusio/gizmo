package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.ExprImpl;

public sealed interface InstanceFieldVar extends FieldVar permits ExprImpl.FieldDeref {
}
