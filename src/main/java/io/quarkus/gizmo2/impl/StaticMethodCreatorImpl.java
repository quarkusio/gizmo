package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.FINAL;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PRIVATE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PROTECTED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.STATIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.StaticMethodCreator;

public final class StaticMethodCreatorImpl extends MethodCreatorImpl implements StaticMethodCreator {
    StaticMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(STATIC),
                Set.of(PUBLIC, PRIVATE, PROTECTED, STATIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, FINAL, VARARGS));
    }

    public void body(final Consumer<BlockCreator> builder) {
        super.body(builder);
    }

    int firstSlot() {
        return 0;
    }

    void accept(final Consumer<? super StaticMethodCreatorImpl> builder) {
        builder.accept(this);
    }
}
