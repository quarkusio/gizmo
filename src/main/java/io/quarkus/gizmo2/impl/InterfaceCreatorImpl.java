package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

public final class InterfaceCreatorImpl extends TypeCreatorImpl implements InterfaceCreator {
    InterfaceCreatorImpl(final ClassDesc type, final ClassBuilder zb) {
        super(type, zb);
    }

    public MethodDesc defaultMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        return null;
    }

    public MethodDesc method(final String name, final Consumer<AbstractMethodCreator> builder) {
        return null;
    }

    void accept(final Consumer<InterfaceCreator> builder) {
        builder.accept(this);
    }
}
