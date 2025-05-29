package io.quarkus.gizmo2.creator;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.quarkus.gizmo2.impl.AnnotatableCreatorImpl;

/**
 * An element that can be annotated.
 */
public sealed interface AnnotatableCreator permits ModifiableCreator, AnnotatableCreatorImpl {
    /**
     * Copy all of the annotations from the given annotated element.
     *
     * @param element the annotated element (must not be {@code null})
     * @return a consumer which copies the annotations (not {@code null})
     */
    static Consumer<AnnotatableCreator> from(AnnotatedElement element) {
        return ac -> Stream.of(element.getAnnotations())
                .filter(a -> Set.of(Objects.requireNonNull(a.annotationType().getAnnotation(Target.class)).value())
                        .contains(((AnnotatableCreatorImpl) ac).annotationTargetType()))
                .forEach(ac::addAnnotation);
    }

    /**
     * Add the given annotation object to this creator.
     *
     * @param annotation the annotation object (must not be {@code null})
     * @param <A> the annotation type
     */
    default <A extends Annotation> void addAnnotation(A annotation) {
        checkNotNullParam("annotation", annotation);
        @SuppressWarnings("unchecked")
        Class<A> annotationType = (Class<A>) annotation.annotationType();
        addAnnotation(annotationType, AnnotationCreator.from(annotation));
    }

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
    default void addAnnotation(Class<? extends Annotation> annotationClass) {
        checkNotNullParam("annotationClass", annotationClass);
        addAnnotation(annotationClass, builder -> {
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
    <A extends Annotation> void addAnnotation(Class<A> annotationClass, Consumer<AnnotationCreator<A>> builder);

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
    void addAnnotation(ClassDesc annotationClass, RetentionPolicy retentionPolicy,
            Consumer<AnnotationCreator<Annotation>> builder);
}
