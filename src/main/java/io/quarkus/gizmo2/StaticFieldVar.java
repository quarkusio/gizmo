package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.StaticFieldVarImpl;

public sealed interface StaticFieldVar extends FieldVar permits StaticFieldVarImpl {
}
