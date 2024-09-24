package io.quarkus.gizmo2.creator;

import java.util.function.Function;

import io.quarkus.gizmo2.Expr;

public interface SwitchExprCreator {
    void case_(Expr val, Function<BlockCreator, Expr> body);

    void default_(Function<BlockCreator, Expr> body);
}
