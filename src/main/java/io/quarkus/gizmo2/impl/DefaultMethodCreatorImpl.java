package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class DefaultMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    DefaultMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.PUBLIC.mask());
    }

    @Override
    public Var this_() {
        return super.this_();
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super DefaultMethodCreatorImpl> builder) {
        builder.accept(this);
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }
}
