package io.quarkus.gizmo2;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.creator.MemberCreator;
import io.quarkus.gizmo2.creator.ParamCreator;
import io.quarkus.gizmo2.creator.TypeCreator;
import io.quarkus.gizmo2.impl.AnnotatableCreatorImpl;

/**
 * An element that can be annotated.
 */
public sealed interface Annotatable permits MemberCreator, ParamCreator, TypeCreator, AnnotatableCreatorImpl {
    /**
     * Add an annotation with no elements. If {@code annotationClass} has no {@link Retention}
     * annotation, {@link RetentionPolicy#RUNTIME} is assumed. This is the most commonly used
     * value, even though {@code CLASS} is the specified default.
     * <p>
     * If the annotation is {@linkplain Repeatable repeatable}, and adding this annotation would
     * cause there to be more than one instance of the given annotation on this element,
     * then the existing annotation and the given annotation are automatically wrapped in the
     * container annotation.
     *
     * @param annotationClass the class of the annotation (must not be {@code null})
     * @throws IllegalArgumentException if the annotation is not repeatable and appears more than once on this element
     */
    default void withAnnotation(Class<? extends Annotation> annotationClass) {
        checkNotNullParam("annotationClass", annotationClass);
        withAnnotation(annotationClass, builder -> {
        });
    }

    /**
     * Add an annotation with elements provided by given {@code builder}. If {@code annotationClass}
     * has no {@link Retention} annotation, {@link RetentionPolicy#RUNTIME} is assumed. This is
     * the most commonly used value, even though {@code CLASS} is the specified default.
     * <p>
     * If the annotation is {@linkplain Repeatable repeatable}, and adding this annotation would
     * cause there to be more than one instance of the given annotation on this element,
     * then the existing annotation and the given annotation are automatically wrapped in the
     * container annotation.
     *
     * @param annotationClass the class of the annotation (must not be {@code null})
     * @param builder the builder which adds annotation values (must not be {@code null})
     * @param <A> the annotation type
     * @throws IllegalArgumentException if the annotation is not repeatable and appears more than once on this element
     */
    <A extends Annotation> void withAnnotation(Class<A> annotationClass, Consumer<AnnotationCreator<A>> builder);

    /**
     * Add an annotation of given class with given retention policy and with elements provided by
     * given {@code builder}.
     * <p>
     * If the annotation is repeatable, then it is the responsibility of the caller to ensure
     * that the repeated values are appropriately wrapped in the container annotation
     * if necessary.
     *
     * @param annotationClass the class of the annotation (must not be {@code null})
     * @param retentionPolicy the retention policy of the annotation (must not be {@code null})
     * @param builder the builder which adds annotation values (must not be {@code null})
     * @throws IllegalArgumentException if the annotation appears more than once on this element
     */
    void withAnnotation(ClassDesc annotationClass, RetentionPolicy retentionPolicy,
            Consumer<AnnotationCreator<Annotation>> builder);
}
