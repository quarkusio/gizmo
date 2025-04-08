package io.quarkus.gizmo2.creator;

import io.quarkus.gizmo2.impl.StaticFieldCreatorImpl;

/**
 * A creator for a static field.
 */
public sealed interface StaticFieldCreator extends FieldCreator permits StaticFieldCreatorImpl {
}
