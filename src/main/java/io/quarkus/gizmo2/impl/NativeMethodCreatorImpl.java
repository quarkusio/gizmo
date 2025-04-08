package io.quarkus.gizmo2.impl;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.NATIVE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PRIVATE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PROTECTED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

public final class NativeMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    NativeMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(NATIVE), Set.of(PUBLIC, PRIVATE, PROTECTED, SYNTHETIC, BRIDGE, NATIVE, VARARGS));
    }

    void accept(final Consumer<? super NativeMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
