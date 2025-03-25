package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;

public final class NativeMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    NativeMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.NATIVE.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PRIVATE, PROTECTED, SYNTHETIC, BRIDGE, NATIVE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super NativeMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
