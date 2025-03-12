package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Function;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.SwitchExprCreator;

public final class SwitchExprCreatorImpl implements SwitchExprCreator {
    // TODO

    public void case_(final Constant val, final Function<BlockCreator, Expr> body) {

    }

    public void default_(final Function<BlockCreator, Expr> body) {

    }

    public ClassDesc type() {
        return null;
    }
}
