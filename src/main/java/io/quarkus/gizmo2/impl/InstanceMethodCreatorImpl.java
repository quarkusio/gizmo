package io.quarkus.gizmo2.impl;

import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, 0);
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
        return super.this_();
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super InstanceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
