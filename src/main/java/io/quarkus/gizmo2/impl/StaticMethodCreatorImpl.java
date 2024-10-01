package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.StaticMethodCreator;

public final class StaticMethodCreatorImpl extends MethodCreatorImpl implements StaticMethodCreator {

    public StaticMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.STATIC.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, STATIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    int firstSlot() {
        return 0;
    }

    void accept(final Consumer<? super StaticMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
