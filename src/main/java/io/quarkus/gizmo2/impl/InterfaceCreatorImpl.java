package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class InterfaceCreatorImpl extends TypeCreatorImpl implements InterfaceCreator {

    InterfaceCreatorImpl(final ClassDesc type, final ClassOutputImpl output, final ClassBuilder zb) {
        super(type, output, zb, AccessFlag.INTERFACE.mask()
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

    MethodDesc methodDesc(final String name, final MethodTypeDesc type) {
        return InterfaceMethodDesc.of(type(), name, type);
    }

    public MethodDesc defaultMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new DefaultMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        instanceMethods.add(desc);
        return desc;
    }

    public MethodDesc privateMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new PrivateInterfaceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        instanceMethods.add(desc);
        return desc;
    }

    public MethodDesc method(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new InterfaceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        instanceMethods.add(desc);
        return desc;
    }

    void accept(final Consumer<InterfaceCreator> builder) {
        preAccept();
        builder.accept(this);
        postAccept();
    }
}
