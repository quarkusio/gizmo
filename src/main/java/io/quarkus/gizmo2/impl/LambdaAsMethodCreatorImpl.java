package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;

import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.LambdaCreator;

public final class LambdaAsMethodCreatorImpl implements LambdaCreator {
    private final ClassDesc functionalInterfaceDesc;
    private final MethodTypeDesc samDesc;
    private final MethodCreatorImpl samCreator;
    private final List<Expr> captures;

    private boolean parametersDefined;

    public LambdaAsMethodCreatorImpl(ClassDesc functionalInterfaceDesc, MethodTypeDesc samDesc,
            MethodCreatorImpl samCreator, List<Expr> captures) {
        this.functionalInterfaceDesc = functionalInterfaceDesc;
        this.samDesc = samDesc;
        this.samCreator = samCreator;
        this.captures = captures;
    }

    public ClassDesc type() {
        return functionalInterfaceDesc;
    }

    public ParamVar parameter(final String name, final int position) {
        parametersDefined = true;
        return samCreator.parameter(name, captures.size() + position, samDesc.parameterType(position));
    }

    public void body(final Consumer<BlockCreator> builder) {
        samCreator.body(builder);
    }

    public Var capture(final String name, final Expr value) {
        if (parametersDefined) {
            throw new IllegalStateException("All captures must be defined before parameters are defined");
        }
        captures.add(value);
        return samCreator.parameter(name, value.genericType());
    }
}
