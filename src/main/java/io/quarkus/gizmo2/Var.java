package io.quarkus.gizmo2;

/**
 * An {@link Assignable} expression whose storage is a variable.
 */
public sealed interface Var extends Assignable permits FieldVar, LocalVar, ParamVar {
    /**
     * {@return the variable name}
     */
    String name();
}
