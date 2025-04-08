package io.quarkus.gizmo2.impl;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PRIVATE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

public final class PrivateInterfaceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    PrivateInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(PRIVATE), Set.of(PRIVATE, SYNCHRONIZED, SYNTHETIC, BRIDGE, VARARGS));
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super PrivateInterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
