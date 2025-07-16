package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.StaticFieldVarImpl;

/**
 * A variable corresponding to a static field.
 */
public sealed interface StaticFieldVar extends FieldVar permits StaticFieldVarImpl {
}
