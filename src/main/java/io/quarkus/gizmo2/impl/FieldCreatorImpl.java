package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;
import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Set;

import io.github.dmlloyd.classfile.FieldBuilder;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.GenericType;
import io.quarkus.gizmo2.creator.FieldCreator;
import io.quarkus.gizmo2.desc.FieldDesc;

public abstract sealed class FieldCreatorImpl extends AnnotatableCreatorImpl implements FieldCreator
        permits StaticFieldCreatorImpl, InstanceFieldCreatorImpl {
    final ClassDesc owner;
    final String name;
    final TypeCreatorImpl tc;
    final Set<AccessFlag> unremovableFlags;
    GenericType genericType = GenericType.of(CD_int);
    int flags;
    private FieldDesc desc;

    // `defaultFlags` are also flags that cannot be removed
    public FieldCreatorImpl(final ClassDesc owner, final String name, final TypeCreatorImpl tc,
            final Set<AccessFlag> defaultFlags) {
        this.owner = owner;
        this.name = name;
        this.tc = tc;
        this.unremovableFlags = defaultFlags;
        int flags = 0;
        for (AccessFlag defaultFlag : defaultFlags) {
            flags |= defaultFlag.mask();
        }
        this.flags = flags;
    }

    public FieldDesc desc() {
        FieldDesc desc = this.desc;
        if (desc == null) {
            desc = this.desc = FieldDesc.of(owner, name, type());
        }
        return desc;
    }

    public void withType(final GenericType genericType) {
        checkNotNullParam("genericType", genericType);
        if (genericType.desc().equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        this.genericType = genericType;
        desc = null;
    }

    public void withType(final ClassDesc type) {
        checkNotNullParam("type", type);
        if (type.equals(CD_void)) {
            throw new IllegalArgumentException("Fields cannot have void type");
        }
        genericType = GenericType.of(type);
        desc = null;
    }

    public final void withFlag(final AccessFlag flag) {
        if (flag.locations().contains(AccessFlag.Location.FIELD) && flag != AccessFlag.STATIC) {
            flags |= flag.mask();
        } else {
            throw new IllegalArgumentException("Cannot add flag " + flag);
        }
    }

    final void withoutFlag(AccessFlag flag) {
        if (unremovableFlags.contains(flag)) {
            throw new IllegalArgumentException("Cannot remove flag " + flag);
        } else {
            flags &= ~flag.mask();
        }
    }

    final void withoutFlags(AccessFlag... flags) {
        for (AccessFlag flag : flags) {
            withoutFlag(flag);
        }
    }

    @Override
    public final void public_() {
        withFlag(AccessFlag.PUBLIC);
        withoutFlags(AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    @Override
    public final void packagePrivate() {
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE, AccessFlag.PROTECTED);
    }

    @Override
    public final void private_() {
        withFlag(AccessFlag.PRIVATE);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PROTECTED);
    }

    @Override
    public final void protected_() {
        withFlag(AccessFlag.PROTECTED);
        withoutFlags(AccessFlag.PUBLIC, AccessFlag.PRIVATE);
    }

    @Override
    public final void final_() {
        withFlag(AccessFlag.FINAL);
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
