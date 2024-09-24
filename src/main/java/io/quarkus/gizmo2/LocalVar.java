package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.impl.LocalVarImpl;

public sealed interface LocalVar extends Var permits LocalVarImpl {
    /**
     * {@return the block that owns this local variable}
     */
    BlockCreator block();
}
