package io.quarkus.gizmo2.impl;

import static io.github.dmlloyd.classfile.ClassFile.*;
import static io.smallrye.common.constraint.Assert.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.ClassSignature;
import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.GenericType;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.InterfaceCreator;
import io.quarkus.gizmo2.creator.InterfaceSignatureCreator;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class InterfaceCreatorImpl extends TypeCreatorImpl implements InterfaceCreator {
    InterfaceCreatorImpl(final ClassDesc type, final ClassOutput output, final ClassBuilder zb) {
        super(type, output, zb);
        modifiers |= ACC_INTERFACE | ACC_ABSTRACT | ACC_SYNTHETIC | ACC_PUBLIC;
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.INTERFACE;
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

    @Override
    public void signature(Consumer<InterfaceSignatureCreator> builder) {
        InterfaceSignatureCreatorImpl creator = new InterfaceSignatureCreatorImpl();
        builder.accept(creator);
        // TODO validate interfaces
        this.signature = ClassSignature.of(
                creator.typeParameters.isEmpty()
                        ? List.of()
                        : creator.typeParameters.stream().map(SignatureUtil::ofTypeParam).toList(),
                SignatureUtil.ofClass(GenericType.ClassType.OBJECT),
                creator.interfaceTypes.stream()
                        .map(SignatureUtil::ofClassOrParameterized)
                        .toArray(Signature.ClassTypeSig[]::new));
    }

    void accept(final Consumer<InterfaceCreator> builder) {
        preAccept();
        builder.accept(this);
        postAccept();
    }
}
