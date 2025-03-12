package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.util.Objects;

import io.github.dmlloyd.classfile.Signature;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.FieldDesc;
import io.quarkus.gizmo2.creator.FieldCreator;

public abstract sealed class FieldCreatorImpl extends AnnotatableCreatorImpl implements FieldCreator
        permits StaticFieldCreatorImpl, InstanceFieldCreatorImpl {
    final ClassDesc owner;
    final String name;
    final TypeCreatorImpl tc;
    Signature genericType = Signature.of(CD_int);
    ClassDesc type = CD_int;
    int flags = 0;
    private FieldDesc desc;

    public FieldCreatorImpl(final ClassDesc owner, final String name, final TypeCreatorImpl tc, final int flags) {
        this.owner = owner;
        this.name = name;
        this.tc = tc;
        this.flags = flags;
    }

    public FieldDesc desc() {
        FieldDesc desc = this.desc;
        if (desc == null) {
            desc = this.desc = FieldDesc.of(owner, name, type);
        }
        return desc;
    }

    public void withTypeSignature(final Signature type) {
        Objects.requireNonNull(type, "type");
        withType(Util.erased(type));
        genericType = type;
    }

    public void withType(final ClassDesc type) {
        this.type = Objects.requireNonNull(type, "type");
        if (type.equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        genericType = Signature.ClassTypeSig.of(type);
        desc = null;
    }

    public void withFlag(final AccessFlag flag) {
        if (flag.locations().contains(AccessFlag.Location.FIELD) && flag != AccessFlag.STATIC) {
            flags |= flag.mask();
        } else {
            throw new IllegalArgumentException("Invalid flag for instance field: " + flag);
        }
    }

    void withoutFlag(AccessFlag flag) {
        flags &= ~flag.mask();
    }

    void withoutFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withoutFlag(flag);
        }
    }

    @Override
    public void public_() {
        withFlag(AccessFlag.PUBLIC);
        withoutFlags(AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    @Override
    public void packagePrivate() {
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    @Override
    public void private_() {
        withFlag(AccessFlag.PRIVATE);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PROTECTED);
    }

    @Override
    public void protected_() {
        withFlag(AccessFlag.PROTECTED);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE);
    }

    @Override
    public void final_() {
        withFlag(AccessFlag.FINAL);
    }

    public ClassDesc owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public ClassDesc type() {
        return type;
    }
}
