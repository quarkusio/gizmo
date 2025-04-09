package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.ABSTRACT;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.BRIDGE;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PROTECTED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.PUBLIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNCHRONIZED;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.SYNTHETIC;
import static io.github.dmlloyd.classfile.extras.reflect.AccessFlag.VARARGS;

import java.util.Set;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AbstractMethodCreator;

public final class AbstractMethodCreatorImpl extends MethodCreatorImpl implements AbstractMethodCreator {
    AbstractMethodCreatorImpl(final TypeCreatorImpl owner, final String name) {
        super(owner, name, Set.of(ABSTRACT), Set.of(PUBLIC, PROTECTED, SYNCHRONIZED, SYNTHETIC, BRIDGE, ABSTRACT, VARARGS));
    }

    void accept(final Consumer<? super AbstractMethodCreatorImpl> builder) {
        builder.accept(this);
        typeCreator.zb.withMethod(name(), type(), flags, mb -> {
            doBody(null, mb);
        });
    }
}
