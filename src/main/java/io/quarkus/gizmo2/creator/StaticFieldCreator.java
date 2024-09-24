package io.quarkus.gizmo2.creator;

import java.util.function.Function;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.StaticFieldCreatorImpl;

public sealed interface StaticFieldCreator extends FieldCreator permits StaticFieldCreatorImpl {
    void withInitial(Constant initial);

    void withInitializer(Function<BlockCreator, Expr> init);
}
