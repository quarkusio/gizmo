package io.quarkus.gizmo2;

/**
 * An lvalue expression that is stored in a variable.
 */
public sealed interface Var extends LValueExpr permits LocalVar, ParamVar, FieldVar {
    /**
     * {@return the variable name}
     */
    String name();


}
