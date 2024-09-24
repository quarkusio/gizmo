package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.ParamVarImpl;

public sealed interface ParamVar extends Var permits ParamVarImpl {
    /**
     * {@return the parameter index, counting from zero}
     * The index does not include any "{@code this}" variable.
     * The index only increments once per parameter, even
     * if the parameter type is {@code long} or {@code double}.
     */
    int index();
}
