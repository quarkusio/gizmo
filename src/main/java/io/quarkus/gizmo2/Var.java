package io.quarkus.gizmo2;

/**
 * An lvalue expression that is stored in a variable.
 */
public sealed interface Var extends Expr permits FieldVar, LocalVar, ParamVar {
    /**
     * {@return the variable name}
     */
    String name();
}
