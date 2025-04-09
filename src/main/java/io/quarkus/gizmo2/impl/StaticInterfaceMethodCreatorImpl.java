package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.FINAL;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.STATIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;

public final class StaticInterfaceMethodCreatorImpl extends MethodCreatorImpl implements StaticMethodCreator {
    StaticInterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(STATIC, PUBLIC), Set.of(PUBLIC, STATIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS));
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
