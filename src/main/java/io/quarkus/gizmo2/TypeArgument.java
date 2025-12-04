package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.classfile.Annotation;
import io.smallrye.common.constraint.Assert;

/**
 * An actual type argument for a formal type parameter.
 * Type arguments differ from {@linkplain GenericType generic types} in that
 * they may represent {@linkplain OfWildcard wildcard types}.
 */
public abstract sealed class TypeArgument {

    TypeArgument() {
    }

    /**
     * {@return a type argument representing exactly the given type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument.OfExact of(Class<?> type) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Primitive types cannot be type arguments");
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
    }

    /**
     * {@return a type argument representing exactly the given type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument.OfExact of(ClassDesc type) {
        if (type.isPrimitive()) {
            throw new IllegalArgumentException("Primitive types cannot be type arguments");
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
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
     * {@return a type argument representing a wildcard with given upper bound}
     *
     * @param bound the bound (must not be {@code null})
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
     * {@return a type argument representing a wildcard with given lower bound}
     *
     * @param bound the bound (must not be {@code null})
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
     * {@return a type argument representing the unbounded wildcard}
     */
    public static OfUnbounded ofUnbounded() {
        return OfUnbounded.instance;
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
     * Implemented by type arguments that contain a type. That is:
     * <ul>
     * <li>{@linkplain OfExact exact type arguments}</li>
     * <li>{@linkplain OfExtends wildcard type arguments with upper bound}</li>
     * <li>{@linkplain OfSuper wildcard type arguments with lower bound}</li>
     * </ul>
     * The only kind of type argument that does <em>not</em> contain a type
     * is an {@linkplain OfUnbounded unbounded wildcard}.
     */
    public sealed interface OfTyped {
        GenericType.OfReference type();
    }

    /**
     * An exact type argument.
     */
    public static final class OfExact extends TypeArgument implements OfTyped {
        private final GenericType.OfReference type;

        OfExact(final GenericType.OfReference type) {
            this.type = type;
        }

        @Override
        public GenericType.OfReference type() {
            return type;
        }

        public boolean hasVisibleAnnotations() {
            return type.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return type.hasInvisibleAnnotations();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof OfExact oe && equals(oe);
        }

        public boolean equals(OfExact other) {
            return this == other || type.equals(other.type);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type);
        }

        public StringBuilder toString(final StringBuilder b) {
            return type.toString(b);
        }
    }

    /**
     * A wildcard type argument. Its type annotations are present on the wildcard,
     * which corresponds to the following Java syntax:
     * <ul>
     * <li>{@code @MyAnn ?}</li>
     * <li>{@code @MyAnn ? extends Bound}</li>
     * <li>{@code @MyAnn ? super Bound}</li>
     * </ul>
     * The bound, if present, may have its own type annotations.
     */
    public static abstract sealed class OfWildcard extends TypeArgument {
        final List<Annotation> visible;
        final List<Annotation> invisible;

        OfWildcard(final List<Annotation> visible, final List<Annotation> invisible) {
            this.visible = visible;
            this.invisible = invisible;
        }

        List<Annotation> visible() {
            return visible;
        }

        List<Annotation> invisible() {
            return invisible;
        }

        abstract OfWildcard copy(final List<Annotation> visible, final List<Annotation> invisible);

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
        public OfWildcard withAnnotations(Consumer<AnnotatableCreator> builder) {
            TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl(visible, invisible);
            builder.accept(tac);
            return copy(tac.visible(), tac.invisible());
        }

        /**
         * {@return a copy of this type argument with the given annotation}
         *
         * @param annotationType the annotation type
         */
        public <A extends java.lang.annotation.Annotation> OfWildcard withAnnotation(final Class<A> annotationType) {
            return withAnnotations(ac -> ac.addAnnotation(annotationType));
        }

        /**
         * {@return a copy of this type argument with the given annotation}
         *
         * @param annotationType the annotation type
         * @param builder the builder for the single annotation (must not be {@code null})
         */
        public <A extends java.lang.annotation.Annotation> OfWildcard withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return withAnnotations(ac -> ac.addAnnotation(annotationType, builder));
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof OfWildcard ow && equals(ow);
        }

        public boolean equals(OfWildcard other) {
            return this == other || visible.equals(other.visible) && invisible.equals(other.invisible);
        }

        @Override
        public int hashCode() {
            return Objects.hash(visible, invisible);
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
     * A wildcard type argument that has a bound.
     * The bound is the type {@linkplain OfTyped contained} by this type argument.
     */
    public sealed interface OfBounded extends OfTyped {
        @Override
        default GenericType.OfReference type() {
            return bound();
        }

        /**
         * {@return the bound of this wildcard (not {@code null})}
         */
        GenericType.OfReference bound();
    }

    /**
     * A wildcard type argument with an upper ({@code extends}) bound.
     */
    public static final class OfExtends extends OfWildcard implements OfBounded {
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

        @Override
        public boolean equals(OfWildcard other) {
            return other instanceof OfExtends oe && equals(oe);
        }

        public boolean equals(OfExtends other) {
            return this == other || super.equals(other) && bound.equals(other.bound);
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 19 + bound.hashCode();
        }

        public StringBuilder toString(final StringBuilder b) {
            return bound.toString(super.toString(b).append("? extends "));
        }
    }

    /**
     * A wildcard type argument with a lower ({@code super}) bound.
     */
    public static final class OfSuper extends OfWildcard implements OfBounded {
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

        @Override
        public boolean equals(OfWildcard other) {
            return other instanceof OfSuper os && equals(os);
        }

        public boolean equals(OfSuper other) {
            return this == other || super.equals(other) && bound.equals(other.bound);
        }

        @Override
        public int hashCode() {
            return super.hashCode() * 19 + bound.hashCode();
        }

        public StringBuilder toString(final StringBuilder b) {
            return bound.toString(super.toString(b).append("? super "));
        }
    }

    /**
     * An unbounded wildcard type argument.
     */
    public static final class OfUnbounded extends OfWildcard {
        private static final OfUnbounded instance = new OfUnbounded();

        private OfUnbounded() {
            super(List.of(), List.of());
        }

        private OfUnbounded(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }

        OfUnbounded copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfUnbounded(visible, invisible);
        }

        public OfUnbounded withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfUnbounded) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfUnbounded withAnnotation(final Class<A> annotationType) {
            return (OfUnbounded) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfUnbounded withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfUnbounded) super.withAnnotation(annotationType, builder);
        }

        @Override
        public boolean equals(OfWildcard other) {
            return other instanceof OfUnbounded ou && equals(ou);
        }

        public boolean equals(OfUnbounded other) {
            return this == other || super.equals(other);
        }

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append('?');
        }
    }
}
