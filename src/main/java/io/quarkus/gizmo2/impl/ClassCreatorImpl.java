package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.ConstructorDesc;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.MethodDesc;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;

public final class ClassCreatorImpl extends TypeCreatorImpl implements ClassCreator {
    public ClassCreatorImpl(final ClassDesc type, final ClassBuilder zb) {
        super(type, zb);
    }

    public void withFlag(final AccessFlag flag) {
        if (flag == AccessFlag.INTERFACE) {
            throw new IllegalArgumentException("Flag " + flag + " not allowed here");
        }
        super.withFlag(flag);
    }

    public void withFlags(final Set<AccessFlag> flags) {
        if (flags.contains(AccessFlag.INTERFACE)) {
            throw new IllegalArgumentException("Flag " + AccessFlag.INTERFACE + " not allowed here");
        }
        super.withFlags(flags);
    }

    public void extends_(final Signature.ClassTypeSig genericType) {
        super.extends_(genericType);
    }

    public void extends_(final ClassDesc desc) {
        super.extends_(desc);
    }

    public FieldDesc field(final String name, final Consumer<InstanceFieldCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var fc = new InstanceFieldCreatorImpl(this, type(), name);
        fc.accept(builder);
        return fc.desc();
    }

    public MethodDesc method(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new InstanceMethodCreatorImpl(this, type(), name);
        return mc.desc();
    }

    public MethodDesc abstractMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        return null;
    }

    public MethodDesc nativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        return null;
    }

    public MethodDesc staticNativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        return null;
    }

    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        return null;
    }

    void accept(final Consumer<ClassCreator> builder) {
        builder.accept(this);
        postAccept();
    }
}
