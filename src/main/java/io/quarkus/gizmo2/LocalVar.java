package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.impl.LocalVarImpl;

/**
 * A local variable.
 */
public sealed interface LocalVar extends Var, LValueExpr permits LocalVarImpl {
    /**
     * {@return the block that owns this local variable}
     */
    BlockCreator block();
}
