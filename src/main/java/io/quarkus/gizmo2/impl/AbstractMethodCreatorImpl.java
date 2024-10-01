package io.quarkus.gizmo2.impl;

import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;

public final class AbstractMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    AbstractMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.ABSTRACT.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case PUBLIC, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE, ABSTRACT, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super AbstractMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
