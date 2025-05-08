package io.quarkus.gizmo2;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.Annotation;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * An actual type argument for a formal type parameter.
 * Type arguments differ from {@linkplain GenericType generic types} in that they may have a bound
 * or may represent the {@linkplain Wildcard wildcard type}.
 */
public abstract class TypeArgument {

    TypeArgument() {
    }

    /**
     * {@return a type argument for the given generic reflection type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument of(final Type type) {
        if (type instanceof WildcardType wt) {
            return of(wt);
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
    }

    /**
     * {@return a type argument for the given annotated generic reflection type}
     * If the type is an annotated wildcard type, then the type argument will be a wildcard type
     * with any annotations attached to the given type.
     * Otherwise, the result will be an exact type argument whose {@linkplain OfExact#bound() bound type}
     * will have any annotations attached to the given type.
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument of(final AnnotatedType type) {
        if (type instanceof AnnotatedWildcardType wt) {
            return of(wt);
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
    }

    /**
     * {@return a type argument for the given reflection wildcard type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static OfAnnotated of(final WildcardType type) {
        List<Type> ub = List.of(type.getUpperBounds());
        List<Type> lb = List.of(type.getLowerBounds());
        if (!lb.isEmpty()) {
            return ofSuper((GenericType.OfReference) GenericType.of(lb.get(0)));
        } else if (ub.isEmpty() || ub.size() == 1 && ub.get(0).equals(Object.class)) {
            return ofWildcard();
        } else {
            return ofExtends((GenericType.OfReference) GenericType.of(ub.get(0)));
        }
    }

    /**
     * {@return a type argument for the given annotated reflection wildcard type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static OfAnnotated of(final AnnotatedWildcardType type) {
        List<AnnotatedType> aub = List.of(type.getAnnotatedUpperBounds());
        List<AnnotatedType> alb = List.of(type.getAnnotatedLowerBounds());
        if (!alb.isEmpty()) {
            return ofSuper((GenericType.OfReference) GenericType.of(alb.get(0))).withAnnotations(AnnotatableCreator.from(type));
        }
        if (aub.isEmpty() || aub.size() == 1 && aub.get(0).getType().equals(Object.class)) {
            return ofWildcard().withAnnotations(AnnotatableCreator.from(type));
        }
        return ofExtends((GenericType.OfReference) GenericType.of(aub.get(0))).withAnnotations(AnnotatableCreator.from(type));
    }

    /**
     * {@return a type argument representing exactly the given type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument.OfExact ofExact(GenericType.OfReference type) {
        Assert.checkNotNullParam("type", type);
        OfExact ofExact = type.exactArg;
        if (ofExact == null) {
            ofExact = type.exactArg = new OfExact(type);
        }
        return ofExact;
    }

    /**
     * {@return a type argument representing the given covariant bound}
     *
     * @param bound the covariant bound (must not be {@code null})
     */
    public static TypeArgument.OfExtends ofExtends(GenericType.OfReference bound) {
        Assert.checkNotNullParam("bound", bound);
        OfExtends ofExtends = bound.extendsArg;
        if (ofExtends == null) {
            ofExtends = bound.extendsArg = new OfExtends(List.of(), List.of(), bound);
        }
        return ofExtends;
    }

    /**
     * {@return a type argument representing the given contravariant bound}
     *
     * @param bound the contravariant bound (must not be {@code null})
     */
    public static TypeArgument.OfSuper ofSuper(GenericType.OfReference bound) {
        Assert.checkNotNullParam("bound", bound);
        OfSuper ofSuper = bound.superArg;
        if (ofSuper == null) {
            ofSuper = bound.superArg = new OfSuper(List.of(), List.of(), bound);
        }
        return ofSuper;
    }

    /**
     * {@return a type argument representing the wildcard type}
     */
    public static Wildcard ofWildcard() {
        return Wildcard.instance;
    }

    /**
     * {@return {@code true} if runtime-visible type annotations are present, or {@code false} otherwise}
     */
    public abstract boolean hasVisibleAnnotations();

    /**
     * {@return {@code true} if runtime-invisible type annotations are present, or {@code false} otherwise}
     */
    public abstract boolean hasInvisibleAnnotations();

    /**
     * {@return {@code true} if any type annotations are present, or {@code false} otherwise}
     */
    public final boolean hasAnnotations() {
        return hasVisibleAnnotations() || hasInvisibleAnnotations();
    }

    /**
     * Append the string representation of this type argument to the given builder, and return it.
     *
     * @param b the string builder (must not be {@code null})
     * @return the same string builder (not {@code null})
     */
    public abstract StringBuilder toString(StringBuilder b);

    /**
     * {@return the string representation of this type argument}
     */
    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * A type argument that has a bound.
     */
    public sealed interface OfBounded permits OfExtends, OfSuper, OfExact {
        /**
         * {@return the bound of this type argument (not {@code null})}
         */
        GenericType.OfReference bound();
    }

    /**
     * A type argument that may be annotated.
     */
    public static abstract class OfAnnotated extends TypeArgument {
        final List<Annotation> visible;
        final List<Annotation> invisible;

        OfAnnotated(final List<Annotation> visible, final List<Annotation> invisible) {
            this.visible = visible;
            this.invisible = invisible;
        }

        List<Annotation> visible() {
            return visible;
        }

        List<Annotation> invisible() {
            return invisible;
        }

        OfAnnotated copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return null;
        }

        public boolean hasVisibleAnnotations() {
            return !visible.isEmpty();
        }

        public boolean hasInvisibleAnnotations() {
            return !invisible.isEmpty();
        }

        /**
         * {@return a copy of this type argument with annotations defined by the given builder}
         *
         * @param builder the annotation builder (must not be {@code null})
         */
        public OfAnnotated withAnnotations(Consumer<AnnotatableCreator> builder) {
            TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl(visible, invisible);
            builder.accept(tac);
            return copy(tac.visible(), tac.invisible());
        }

        /**
         * {@return a copy of this type argument with the given annotation}
         *
         * @param annotationType the annotation type
         */
        public <A extends java.lang.annotation.Annotation> OfAnnotated withAnnotation(final Class<A> annotationType) {
            return withAnnotations(ac -> ac.withAnnotation(annotationType));
        }

        /**
         * {@return a copy of this type argument with the given annotation}
         *
         * @param annotationType the annotation type
         * @param builder the builder for the single annotation (must not be {@code null})
         */
        public <A extends java.lang.annotation.Annotation> OfAnnotated withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return withAnnotations(ac -> ac.withAnnotation(annotationType, builder));
        }

        public StringBuilder toString(StringBuilder b) {
            for (Annotation annotation : visible) {
                Util.appendAnnotation(b, annotation).append(' ');
            }
            for (Annotation annotation : invisible) {
                Util.appendAnnotation(b, annotation).append(' ');
            }
            return b;
        }
    }

    /**
     * A bound type argument with a covariant ("extends") bound.
     */
    public static final class OfExtends extends OfAnnotated implements OfBounded {
        private final GenericType.OfReference bound;

        OfExtends(final List<Annotation> visible, final List<Annotation> invisible, final GenericType.OfReference bound) {
            super(visible, invisible);
            this.bound = bound;
        }

        public GenericType.OfReference bound() {
            return bound;
        }

        public boolean hasVisibleAnnotations() {
            return super.hasVisibleAnnotations() || bound.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return super.hasInvisibleAnnotations() || bound.hasInvisibleAnnotations();
        }

        OfExtends copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfExtends(visible, invisible, bound());
        }

        public OfExtends withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfExtends) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfExtends withAnnotation(final Class<A> annotationType) {
            return (OfExtends) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfExtends withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfExtends) super.withAnnotation(annotationType, builder);
        }

        public StringBuilder toString(final StringBuilder b) {
            return bound.toString(super.toString(b).append("? extends "));
        }
    }

    /**
     * A bound type argument with an exact bound.
     */
    public static final class OfExact extends TypeArgument implements OfBounded {
        private final GenericType.OfReference bound;

        OfExact(final GenericType.OfReference bound) {
            this.bound = bound;
        }

        public GenericType.OfReference bound() {
            return bound;
        }

        public boolean hasVisibleAnnotations() {
            return bound.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return bound.hasInvisibleAnnotations();
        }

        public StringBuilder toString(final StringBuilder b) {
            return bound.toString(b);
        }
    }

    /**
     * A bound type argument with a contravariant ("super") bound.
     */
    public static final class OfSuper extends OfAnnotated implements OfBounded {
        private final GenericType.OfReference bound;

        OfSuper(final List<Annotation> visible, final List<Annotation> invisible, final GenericType.OfReference bound) {
            super(visible, invisible);
            this.bound = bound;
        }

        public GenericType.OfReference bound() {
            return bound;
        }

        public boolean hasVisibleAnnotations() {
            return super.hasVisibleAnnotations() || bound.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return super.hasInvisibleAnnotations() || bound.hasInvisibleAnnotations();
        }

        OfSuper copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfSuper(visible, invisible, bound());
        }

        public OfSuper withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfSuper) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfSuper withAnnotation(final Class<A> annotationType) {
            return (OfSuper) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfSuper withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfSuper) super.withAnnotation(annotationType, builder);
        }

        public StringBuilder toString(final StringBuilder b) {
            return bound.toString(super.toString(b).append("? super "));
        }
    }

    /**
     * A wildcard type argument.
     */
    public static final class Wildcard extends OfAnnotated {
        private Wildcard() {
            super(List.of(), List.of());
        }

        private Wildcard(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }

        Wildcard copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new Wildcard(visible, invisible);
        }

        public Wildcard withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (Wildcard) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> Wildcard withAnnotation(final Class<A> annotationType) {
            return (Wildcard) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> Wildcard withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (Wildcard) super.withAnnotation(annotationType, builder);
        }

        private static final Wildcard instance = new Wildcard();

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append('*');
        }
    }
}
