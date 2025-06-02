package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.ClassFile;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class InterfaceCreatorImpl extends TypeCreatorImpl implements InterfaceCreator {

    InterfaceCreatorImpl(final GizmoImpl gizmo, final ClassDesc type, final ClassOutput output, final ClassBuilder zb) {
        super(gizmo, type, output, zb);
        // not a user-visible modifier, so set it explicitly here
        modifiers |= ClassFile.ACC_INTERFACE;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE;
    }

    @Override
    public void extends_(GenericType.OfClass genericType) {
        super.implements_(genericType);
    }

    @Override
    public void extends_(ClassDesc interface_) {
        super.implements_(interface_);
    }

    MethodDesc methodDesc(final String name, final MethodTypeDesc type) {
        return InterfaceMethodDesc.of(type(), name, type);
    }

    public MethodDesc defaultMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new DefaultMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc privateMethod(final String name, final Consumer<InstanceMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new PrivateInterfaceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc method(final String name, final Consumer<AbstractMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new InterfaceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    void accept(final Consumer<InterfaceCreator> builder) {
        preAccept();
        builder.accept(this);
        postAccept();
    }
}
