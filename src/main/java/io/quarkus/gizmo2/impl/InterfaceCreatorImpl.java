package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;

public final class InterfaceCreatorImpl extends TypeCreatorImpl implements InterfaceCreator {

    InterfaceCreatorImpl(final ClassDesc type, final ClassBuilder zb) {
        super(type, zb, AccessFlag.INTERFACE.mask()
                | AccessFlag.ABSTRACT.mask()
                | AccessFlag.SYNTHETIC.mask()
                | AccessFlag.PUBLIC.mask());
    }

    public void withFlag(final AccessFlag flag) {
        switch (flag) {
            case INTERFACE, PUBLIC -> super.withFlag(flag);
            default -> throw new IllegalArgumentException(flag.toString());
        }
    }

    public MethodDesc defaultMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new DefaultMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public MethodDesc privateMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new PrivateInterfaceMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public MethodDesc method(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new AbstractMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    void accept(final Consumer<InterfaceCreator> builder) {
        preAccept();
        builder.accept(this);
        postAccept();
    }
}
