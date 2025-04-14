package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import io.quarkus.gizmo2.Annotatable;
import io.quarkus.gizmo2.creator.AnnotationCreator;

public abstract sealed class AnnotatableCreatorImpl implements Annotatable
        permits ExecutableCreatorImpl, FieldCreatorImpl, ParamCreatorImpl, TypeCreatorImpl {
    List<Annotation> invisible = List.of();
    List<Annotation> visible = List.of();

    private List<Annotation> visible() {
        List<Annotation> list = visible;
        if (list.isEmpty()) {
            list = visible = new ArrayList<>(4);
        }
        return list;
    }

    private List<Annotation> invisible() {
        List<Annotation> list = invisible;
        if (list.isEmpty()) {
            list = invisible = new ArrayList<>(4);
        }
        return list;
    }

    abstract ElementType annotationTargetType();

    @Override
    public <A extends java.lang.annotation.Annotation> void withAnnotation(Class<A> annotationClass,
            Consumer<AnnotationCreator<A>> builder) {
        checkNotNullParam("annotationClass", annotationClass);
        checkNotNullParam("builder", builder);

        Target target = annotationClass.getAnnotation(Target.class);
        if (target != null) {
            Set<ElementType> elementTypes = Set.of(target.value());
            if (!elementTypes.contains(annotationTargetType())) {
                throw new IllegalArgumentException("Annotation %s is not allowed on element type `%s` (the allowed set is %s)"
                        .formatted(annotationClass, annotationTargetType(), elementTypes));
            }
        }
        Retention retention = annotationClass.getAnnotation(Retention.class);
        RetentionPolicy retentionPolicy = retention == null ? RetentionPolicy.RUNTIME : retention.value();
        Annotation annotation = AnnotationCreatorImpl.makeAnnotation(annotationClass, builder);
        // ignore SOURCE for now (though we could record it for posterity, maybe put it in a custom attribute)
        switch (retentionPolicy) {
            case CLASS -> invisible().add(annotation);
            case RUNTIME -> visible().add(annotation);
        }
    }

    @Override
    public void withAnnotation(ClassDesc annotationClass, RetentionPolicy retentionPolicy,
            Consumer<AnnotationCreator<java.lang.annotation.Annotation>> builder) {
        checkNotNullParam("annotationClass", annotationClass);
        checkNotNullParam("retentionPolicy", retentionPolicy);
        checkNotNullParam("builder", builder);

        Annotation annotation = AnnotationCreatorImpl.makeAnnotation(annotationClass, builder);
        // ignore SOURCE for now (though we could record it for posterity, maybe put it in a custom attribute)
        switch (retentionPolicy) {
            case CLASS -> invisible().add(annotation);
            case RUNTIME -> visible().add(annotation);
        }
    }

    void addInvisible(Consumer<? super RuntimeInvisibleAnnotationsAttribute> consumer) {
        if (!invisible.isEmpty()) {
            consumer.accept(RuntimeInvisibleAnnotationsAttribute.of(visible));
        }
    }

    void addVisible(Consumer<? super RuntimeVisibleAnnotationsAttribute> consumer) {
        if (!visible.isEmpty()) {
            consumer.accept(RuntimeVisibleAnnotationsAttribute.of(visible));
        }
    }
}
