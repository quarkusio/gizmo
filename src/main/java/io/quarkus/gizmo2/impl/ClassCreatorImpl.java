package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Modifier;
import java.util.function.Consumer;

import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.creator.ModifierFlag;
import io.quarkus.gizmo2.creator.ModifierLocation;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.classfile.ClassBuilder;

public sealed class ClassCreatorImpl extends TypeCreatorImpl implements ClassCreator permits AnonymousClassCreatorImpl {
    public ClassCreatorImpl(final GizmoImpl gizmo, final ClassDesc type, final ClassOutput output, final ClassBuilder zb) {
        super(gizmo, type, output, zb);
    }

    public ModifierLocation modifierLocation() {
        return ModifierLocation.CLASS;
    }

    public void extends_(final GenericType.OfClass genericType) {
        super.extends_(genericType);
    }

    public void extends_(final ClassDesc desc) {
        super.extends_(desc);
    }

    @Override
    public void implements_(GenericType.OfClass genericType) {
        super.implements_(genericType);
    }

    @Override
    public void implements_(ClassDesc interface_) {
        super.implements_(interface_);
    }

    public ClassDesc superClass() {
        return super.superClass();
    }

    MethodDesc methodDesc(final String name, final MethodTypeDesc type) {
        return ClassMethodDesc.of(type(), name, type);
    }

    public FieldDesc field(final String name, final Consumer<InstanceFieldCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var fc = new InstanceFieldCreatorImpl(this, type(), name);
        fc.accept(builder);
        FieldDesc desc = fc.desc();
        if (fields.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate field added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc method(final String name, final Consumer<InstanceMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new InstanceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc abstractMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new AbstractMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc nativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        checkNotNullParam("name", name);
        checkNotNullParam("builder", builder);
        var mc = new NativeMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.valueOf(Modifier.isStatic(mc.modifiers))) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc staticNativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        return nativeMethod(name, mc -> {
            mc.addFlag(ModifierFlag.STATIC);
            builder.accept(mc);
        });
    }

    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        checkNotNullParam("builder", builder);
        var mc = new ConstructorCreatorImpl(this, preInits, postInits);
        mc.accept(builder);
        ConstructorDesc desc = mc.desc();
        if (!constructors.add(desc)) {
            throw new IllegalArgumentException("Duplicate constructor added: %s".formatted(desc));
        }
        return desc;
    }
}
