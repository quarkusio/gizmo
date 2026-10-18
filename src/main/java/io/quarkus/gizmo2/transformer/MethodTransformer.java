package io.quarkus.gizmo2.transformer;

import io.quarkus.gizmo2.impl.MethodTransformerImpl;

/**
 * A transformer for an existing method or constructor.
 */
public sealed interface MethodTransformer extends ModifiableTransformer permits MethodTransformerImpl {
    /**
     * Rename the method.
     * Call sites of the method are not changed.
     *
     * @param newName the new method name (must not be {@code null})
     * @throws IllegalArgumentException if this transformer is for a constructor
     */
    void rename(String newName);
}
