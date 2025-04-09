package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.ABSTRACT;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;

public final class InterfaceMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    InterfaceMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(ABSTRACT, PUBLIC), Set.of(ABSTRACT, PUBLIC, SYNCHRONIZED, SYNTHETIC, BRIDGE, VARARGS));
    }

    void accept(final Consumer<? super InterfaceMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
