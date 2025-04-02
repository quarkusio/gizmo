package io.quarkus.gizmo2;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Consumer;

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
    void withAnnotation(RetentionPolicy retention, io.github.dmlloyd.classfile.Annotation annotation);

    /**
     * Add an annotation.
     *
     * @param retention  the retention policy for the annotation (must not be {@code null})
     * @param type  the annotation type (must not be {@code null})
     * @param elements the annotation elements (must not be {@code null})
     */
    default void withAnnotation(RetentionPolicy retention, ClassDesc type, List<AnnotationElement> elements) {
        withAnnotation(retention, io.github.dmlloyd.classfile.Annotation.of(type, elements));
    }

    /**
     * Add an annotation with no elements. If {@code annClazz} has no {@link Retention}
     * annotation, {@link RetentionPolicy#RUNTIME} is assumed. This is the most commonly
     * used value, even though {@code CLASS} is the specified default.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     */
    default void withAnnotation(Class<? extends Annotation> annClazz) {
        withAnnotation(annClazz, List.of());
    }

    /**
     * Add an annotation with given elements. If {@code annClazz} has no {@link Retention}
     * annotation, {@link RetentionPolicy#RUNTIME} is assumed. This is the most commonly
     * used value, even though {@code CLASS} is the specified default.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     * @param elements the annotation elements (must not be {@code null})
     */
    default void withAnnotation(Class<? extends Annotation> annClazz, List<AnnotationElement> elements) {
        Retention ret = annClazz.getAnnotation(Retention.class);
        RetentionPolicy retention = ret == null ? RetentionPolicy.RUNTIME : ret.value();
        withAnnotation(retention, Util.classDesc(annClazz), elements);
    }

    /**
     * Add an annotation with elements provided by given {@code builder}. If {@code annClazz}
     * has no {@link Retention} annotation, {@link RetentionPolicy#RUNTIME} is assumed.
     * This is the most commonly used value, even though {@code CLASS} is the specified default.
     *
     * @param annClazz the class of the annotation (must not be {@code null})
     * @param builder the builder which adds annotation values (must not be {@code null})
     * @param <A> the annotation type
     */
    default <A extends Annotation> void withAnnotation(Class<A> annClazz, Consumer<AnnotationCreator<A>> builder) {
        Retention ret = annClazz.getAnnotation(Retention.class);
        RetentionPolicy retention = ret == null ? RetentionPolicy.RUNTIME : ret.value();
        withAnnotation(retention, AnnotationCreator.makeAnnotation(annClazz, builder));
    }
}
