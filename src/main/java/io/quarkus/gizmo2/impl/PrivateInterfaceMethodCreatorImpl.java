package io.quarkus.gizmo2.impl;

import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class PrivateInterfaceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    PrivateInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.PRIVATE.mask());
    }

    void doCode(final Consumer<BlockCreator> builder, final CodeBuilder cb, final List<ParamVarImpl> params) {
        cb.localVariable(0, "this", owner.type(), cb.startLabel(), cb.endLabel());
        super.doCode(builder, cb, params);
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    @Override
    public Var this_() {
        return new ThisExpr(owner());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PRIVATE, SYNCHRONIZED, SYNTHETIC, BRIDGE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super PrivateInterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
