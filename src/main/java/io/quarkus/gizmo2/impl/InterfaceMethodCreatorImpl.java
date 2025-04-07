package io.quarkus.gizmo2.impl;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.Var;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

import java.util.function.Consumer;

public final class InterfaceMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    InterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, AccessFlag.ABSTRACT.mask() | AccessFlag.PUBLIC.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case ABSTRACT, PUBLIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, VARARGS -> flags |= flag.mask();
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    void accept(final Consumer<? super InterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
