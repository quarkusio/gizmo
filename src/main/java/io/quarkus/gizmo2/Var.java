package io.quarkus.gizmo2;

/**
 * An assignable expression that is stored in a variable.
 */
public sealed interface Var extends Assignable permits FieldVar, LocalVar, ParamVar {
    /**
     * {@return the variable name}
     */
    String name();
}
