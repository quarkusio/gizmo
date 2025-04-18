package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.ClassBuilder;
import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.ClassOutput;
import io.quarkus.gizmo2.creator.AbstractMethodCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.ConstructorCreator;
import io.quarkus.gizmo2.creator.InstanceFieldCreator;
import io.quarkus.gizmo2.creator.InstanceMethodCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public sealed class ClassCreatorImpl extends TypeCreatorImpl implements ClassCreator permits AnonymousClassCreatorImpl {
    public ClassCreatorImpl(final ClassDesc type, final ClassOutput output, final ClassBuilder zb) {
        super(type, output, zb, AccessFlag.SYNTHETIC.mask() | AccessFlag.PUBLIC.mask());
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
        FieldDesc desc = fc.desc();
        if (fields.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate field added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc method(final String name, final Consumer<InstanceMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new InstanceMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc abstractMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new AbstractMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc nativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new NativeMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.FALSE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public MethodDesc staticNativeMethod(final String name, final Consumer<AbstractMethodCreator> builder) {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(builder, "builder");
        var mc = new StaticNativeMethodCreatorImpl(this, name);
        mc.accept(builder);
        MethodDesc desc = mc.desc();
        if (methods.putIfAbsent(desc, Boolean.TRUE) != null) {
            throw new IllegalArgumentException("Duplicate method added: %s".formatted(desc));
        }
        return desc;
    }

    public ConstructorDesc constructor(final Consumer<ConstructorCreator> builder) {
        Objects.requireNonNull(builder, "builder");
        var mc = new ConstructorCreatorImpl(this, preInits, postInits);
        mc.accept(builder);
        ConstructorDesc desc = mc.desc();
        if (!constructors.add(desc)) {
            throw new IllegalArgumentException("Duplicate constructor added: %s".formatted(desc));
        }
        return desc;
    }

    @Override
    public void abstract_() {
        withFlag(AccessFlag.ABSTRACT);
    }

    @Override
    public void final_() {
        withFlag(AccessFlag.FINAL);
    }
}
