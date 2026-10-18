package io.quarkus.gizmo2.transformer;

import io.quarkus.gizmo2.impl.FieldTransformerImpl;

/**
 * A transformer for an existing field.
 */
public sealed interface FieldTransformer extends ModifiableTransformer permits FieldTransformerImpl {
}
