package io.quarkus.gizmo2;

public sealed interface Var extends LValueExpr permits LocalVar, ParamVar, FieldVar {
    String name();


}
