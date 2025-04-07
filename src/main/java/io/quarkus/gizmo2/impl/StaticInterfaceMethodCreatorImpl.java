package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;

import java.util.function.Consumer;

public final class StaticInterfaceMethodCreatorImpl extends MethodCreatorImpl implements StaticMethodCreator {
    StaticInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.STATIC.mask() | AccessFlag.PUBLIC.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, STATIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    int firstSlot() {
        return 0;
    }

    void accept(final Consumer<? super StaticInterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
