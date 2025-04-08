package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class PrivateInterfaceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    PrivateInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.PRIVATE.mask());
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
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
