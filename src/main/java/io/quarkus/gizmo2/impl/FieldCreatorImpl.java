package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.ElementType;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Signature;
import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.creator.FieldSignatureCreator;
import io.quarkus.gizmo2.desc.FieldDesc;

public abstract sealed class FieldCreatorImpl extends ModifiableCreatorImpl implements FieldCreator
        permits StaticFieldCreatorImpl, InstanceFieldCreatorImpl {
    final ClassDesc owner;
    final String name;
    final TypeCreatorImpl tc;
    ClassDesc type = CD_int;
    Signature signature;
    private FieldDesc desc;

    public FieldCreatorImpl(final ClassDesc owner, final String name, final TypeCreatorImpl tc) {
        this.owner = owner;
        this.name = name;
        this.tc = tc;
    }

    public FieldDesc desc() {
        FieldDesc desc = this.desc;
        if (desc == null) {
            desc = this.desc = FieldDesc.of(owner, name, type());
        }
        return desc;
    }

    public void setType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        this.type = type;
        desc = null;
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

    public ElementType annotationTargetType() {
        return ElementType.FIELD;
    }

    @Override
    public void signature(Consumer<FieldSignatureCreator> builder) {
        FieldSignatureCreatorImpl creator = new FieldSignatureCreatorImpl();
        builder.accept(creator);
        if (!type.equals(creator.type.erasure())) {
            throw new IllegalArgumentException("Type in signature (" + creator.type
                    + ") does not match " + type.displayName());
        }
        this.signature = SignatureUtil.of(creator.type);
    }
}
