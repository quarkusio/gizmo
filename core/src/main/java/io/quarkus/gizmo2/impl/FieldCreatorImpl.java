package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;

import io.github.dmlloyd.classfile.FieldBuilder;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.desc.FieldDesc;

public abstract sealed class FieldCreatorImpl extends ModifiableCreatorImpl implements FieldCreator
        permits StaticFieldCreatorImpl, InstanceFieldCreatorImpl {
    final ClassDesc owner;
    final String name;
    final TypeCreatorImpl tc;
    GenericType genericType = GenericType.of(CD_int);
    private FieldDesc desc;

    public FieldCreatorImpl(final ClassDesc owner, final String name, final TypeCreatorImpl tc) {
        super(tc.gizmo);
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

    public void setType(final GenericType genericType) {
        checkNotNullParam("genericType", genericType);
        if (genericType.desc().equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        this.genericType = genericType;
        desc = null;
    }

    public void setType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        genericType = GenericType.of(type);
        desc = null;
    }

    public ClassDesc owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public GenericType genericType() {
        return genericType;
    }

    public ClassDesc type() {
        return genericType.desc();
    }

    public ElementType annotationTargetType() {
        return ElementType.FIELD;
    }

    void addTypeAnnotations(final FieldBuilder fb) {
        ArrayList<TypeAnnotation> visible = new ArrayList<>();
        ArrayList<TypeAnnotation> invisible = new ArrayList<>();
        Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofField(),
                visible, new ArrayDeque<>());
        Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofField(),
                invisible, new ArrayDeque<>());
        if (!visible.isEmpty()) {
            fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(visible));
        }
        if (!invisible.isEmpty()) {
            fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(invisible));
        }
    }
}
