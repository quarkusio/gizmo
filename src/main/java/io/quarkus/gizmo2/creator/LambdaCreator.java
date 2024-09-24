package io.quarkus.gizmo2.creator;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;

public interface LambdaCreator extends StaticExecutableCreator {
    ClassDesc lambdaType();

    Var capture(String name, Expr value);

    default Var capture(LocalVar outer) {
        return capture(outer.name(), outer);
    }

    ParamVar parameter(String name, ClassDesc type);

    void body(Consumer<BlockCreator> builder);
}
