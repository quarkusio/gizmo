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
    ClassDesc type = CD_int;
    GenericType genericType;
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
        if (Util.isVoid(genericType.desc())) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        this.genericType = genericType;
        this.type = genericType.desc();
        desc = null;
    }

    public void setType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (Util.isVoid(type)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        this.type = type;
        genericType = null;
        desc = null;
    }

    public ClassDesc owner() {
        return owner;
    }

    public String name() {
        return name;
    }

    public GenericType genericType() {
        GenericType genericType = this.genericType;
        if (genericType == null) {
            return this.genericType = GenericType.of(type());
        }
        return genericType;
    }

    public boolean hasGenericType() {
        return genericType != null;
    }

    public ClassDesc type() {
        ClassDesc type = this.type;
        if (type != null) {
            return type;
        }
        GenericType genericType = this.genericType;
        if (genericType != null) {
            return this.type = genericType.desc();
        }
        throw new IllegalStateException("Field type is not yet set");
    }

    public ElementType annotationTargetType() {
        return ElementType.FIELD;
    }

    void addTypeAnnotations(final FieldBuilder fb) {
        ArrayList<TypeAnnotation> visible = new ArrayList<>();
        ArrayList<TypeAnnotation> invisible = new ArrayList<>();
        ArrayDeque<TypeAnnotation.TypePathComponent> pathStack = new ArrayDeque<>();
        if (genericType != null) {
            Util.computeAnnotations(genericType, RetentionPolicy.RUNTIME, TypeAnnotation.TargetInfo.ofField(),
                    visible, pathStack);
            assert pathStack.isEmpty();
            Util.computeAnnotations(genericType, RetentionPolicy.CLASS, TypeAnnotation.TargetInfo.ofField(),
                    invisible, pathStack);
            assert pathStack.isEmpty();
        }
        if (!visible.isEmpty()) {
            fb.with(RuntimeVisibleTypeAnnotationsAttribute.of(visible));
        }
        if (!invisible.isEmpty()) {
            fb.with(RuntimeInvisibleTypeAnnotationsAttribute.of(invisible));
        }
    }
}
