package io.quarkus.gizmo2;

import io.quarkus.gizmo2.impl.ThisExpr;

/**
 * An lvalue expression that is stored in a variable.
 */
public sealed interface Var extends Expr permits FieldVar, LocalVar, ParamVar, ThisExpr {
    /**
     * {@return the variable name}
     */
    String name();


}
