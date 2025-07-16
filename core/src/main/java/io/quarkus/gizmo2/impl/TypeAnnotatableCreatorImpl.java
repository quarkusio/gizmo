package io.quarkus.gizmo2.impl;

import java.lang.annotation.ElementType;
import java.util.List;

import io.github.dmlloyd.classfile.Annotation;

public final class TypeAnnotatableCreatorImpl extends AnnotatableCreatorImpl {

    public TypeAnnotatableCreatorImpl() {
    }

    public TypeAnnotatableCreatorImpl(final List<Annotation> visible, final List<Annotation> invisible) {
        super(visible, invisible);
    }

    public ElementType annotationTargetType() {
        return ElementType.TYPE_USE;
    }
}
