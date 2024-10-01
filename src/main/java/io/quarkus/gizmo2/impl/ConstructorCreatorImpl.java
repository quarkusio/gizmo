package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;

public final class ConstructorCreatorImpl extends ExecutableCreatorImpl implements ConstructorCreator {
    private ConstructorDesc desc;

    ConstructorCreatorImpl(final TypeCreatorImpl owner) {
        super(owner, 0);
    }

    public ConstructorDesc desc() {
        ConstructorDesc desc = this.desc;
        if (desc == null) {
            MethodTypeDesc mtd = MethodTypeDesc.of(ConstantDescs.CD_void, params.stream().map(ParamVarImpl::type).toArray(ClassDesc[]::new));
            this.desc = desc = ConstructorDesc.of(owner(), mtd);
        }
        return desc;
    }

    void doCode(final Consumer<BlockCreator> builder, final CodeBuilder cb, final List<ParamVarImpl> params) {
        cb.localVariable(0, "this", owner.type(), cb.startLabel(), cb.endLabel());
        super.doCode(builder, cb, params);
    }

    public void body(final BiConsumer<BlockCreator, Expr> builder) {
        super.body(bc -> builder.accept(bc, new ThisExpr(owner())));
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNTHETIC, BRIDGE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super ConstructorCreatorImpl> builder) {
        builder.accept(this);
    }

    public MethodTypeDesc type() {
        return desc().type();
    }
}
