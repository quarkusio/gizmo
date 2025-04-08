package io.quarkus.gizmo2.impl;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.FINAL;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PRIVATE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PROTECTED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

public final class InstanceMethodCreatorImpl extends MethodCreatorImpl implements InstanceMethodCreator {
    InstanceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(), Set.of(PUBLIC, PRIVATE, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS));
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    void accept(final Consumer<? super InstanceMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
