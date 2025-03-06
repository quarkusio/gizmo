package io.quarkus.gizmo2;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.AnnotationElement;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.creator.MemberCreator;
import io.quarkus.gizmo2.creator.ParamCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.impl.AnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * An element that can be annotated.
 */
public sealed interface Annotatable permits MemberCreator, ParamCreator, TypeCreator, AnnotatableCreatorImpl {

    /**
     * Add an annotation.
     *
     * @param retention  the retention policy for the annotation (must not be {@code null})
     * @param annotation the annotation (must not be {@code null})
     */
    void withAnnotation(RetentionPolicy retention, Annotation annotation);

    /**
     * Add an annotation.
     *
     * @param retention  the retention policy for the annotation (must not be {@code null})
     * @param type  the annotation type (must not be {@code null})
     * @param entries the annotation value entries (must not be {@code null})
     */
    default void withAnnotation(RetentionPolicy retention, ClassDesc type, List<AnnotationElement> entries) {
        withAnnotation(retention, Annotation.of(type, entries));
    }

    /**
     * Add an annotation with no values.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     */
    default void withAnnotation(Class<? extends java.lang.annotation.Annotation> annClazz) {
        withAnnotation(annClazz, List.of());
    }

    /**
     * Add an annotation.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     * @param entries the annotation value entries (must not be {@code null})
     */
    default void withAnnotation(Class<? extends java.lang.annotation.Annotation> annClazz, List<AnnotationElement> entries) {
        Retention ret = annClazz.getAnnotation(Retention.class);
        RetentionPolicy retention = ret == null ? RetentionPolicy.RUNTIME : ret.value();
        withAnnotation(retention, Util.classDesc(annClazz), entries);
    }

    /**
     * Add an annotation with arbitrary content.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     * @param builder the builder which adds annotation values (must not be {@code null})
     * @param <A> the annotation type
     */
    default <A extends java.lang.annotation.Annotation> void withAnnotation(Class<A> annClazz, Consumer<AnnotationCreator<A>> builder) {
        Retention ret = annClazz.getAnnotation(Retention.class);
        RetentionPolicy retention = ret == null ? RetentionPolicy.RUNTIME : ret.value();
        withAnnotation(retention, AnnotationCreator.makeAnnotation(annClazz, builder));
    }
}
