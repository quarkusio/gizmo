package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public final class ClassCreatorImpl extends TypeCreatorImpl implements ClassCreator {
    public ClassCreatorImpl(final ClassDesc type, final ClassBuilder zb) {
        super(type, zb, AccessFlag.SYNTHETIC.mask() 
                | AccessFlag.PUBLIC.mask());
    }

    public void withFlag(final AccessFlag flag) {
        if (flag == AccessFlag.INTERFACE) {
            throw new IllegalArgumentException("Flag " + flag + " not allowed here");
        }
        super.withFlag(flag);
    }

    public void extends_(final Signature.ClassTypeSig genericType) {
        super.extends_(genericType);
    }

    public void extends_(final ClassDesc desc) {
        super.extends_(desc);
    }

    public ClassDesc superClass() {
        return super.superClass();
    }

    MethodDesc methodDesc(final String name, final MethodTypeDesc type) {
        return ClassMethodDesc.of(type(), name, type);
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
        var mc = new InstanceMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public MethodDesc abstractMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new AbstractMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public MethodDesc nativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new NativeMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public MethodDesc staticNativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new StaticNativeMethodCreatorImpl(this, name);
        mc.accept(builder);
        return mc.desc();
    }

    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        Objects.requireNonNull(builder, "builder");
        var mc = new ConstructorCreatorImpl(this);
        mc.accept(builder);
        return mc.desc();
    }

    @Override
    public void abstract_() {
        withFlag(AccessFlag.ABSTRACT);
    }

    @Override
    public void final_() {
        withFlag(AccessFlag.FINAL);
    }

    void accept(final Consumer<ClassCreator> builder) {
        preAccept();
        builder.accept(this);
        postAccept();
    }
}
