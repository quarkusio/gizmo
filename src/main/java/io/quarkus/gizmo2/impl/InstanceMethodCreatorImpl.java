package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, 0);
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
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
