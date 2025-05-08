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
 */
public abstract class TypeArgument {

    TypeArgument() {
    }

    public static TypeArgument ofExact(final Type type) {
        if (type instanceof WildcardType wt) {
            return of(wt);
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
    }

    public static TypeArgument ofExact(final AnnotatedType type) {
        if (type instanceof AnnotatedWildcardType wt) {
            return of(wt);
        }
        return ofExact((GenericType.OfReference) GenericType.of(type));
    }

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

    public abstract boolean hasVisibleAnnotations();

    public abstract boolean hasInvisibleAnnotations();

    public final boolean hasAnnotations() {
        return hasVisibleAnnotations() || hasInvisibleAnnotations();
    }

    public abstract StringBuilder toString(StringBuilder b);

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public static TypeArgument.OfExact ofExact(GenericType.OfReference bound) {
        Assert.checkNotNullParam("bound", bound);
        OfExact ofExact = bound.exactArg;
        if (ofExact == null) {
            ofExact = bound.exactArg = new OfExact(bound);
        }
        return ofExact;
    }

    public static TypeArgument.OfExtends ofExtends(GenericType.OfReference bound) {
        Assert.checkNotNullParam("bound", bound);
        OfExtends ofExtends = bound.extendsArg;
        if (ofExtends == null) {
            ofExtends = bound.extendsArg = new OfExtends(List.of(), List.of(), bound);
        }
        return ofExtends;
    }

    public static TypeArgument.OfSuper ofSuper(GenericType.OfReference bound) {
        Assert.checkNotNullParam("bound", bound);
        OfSuper ofSuper = bound.superArg;
        if (ofSuper == null) {
            ofSuper = bound.superArg = new OfSuper(List.of(), List.of(), bound);
        }
        return ofSuper;
    }

    public static Wildcard ofWildcard() {
        return Wildcard.instance;
    }

    public sealed interface OfBounded permits OfExtends, OfSuper, OfExact {
        GenericType.OfReference bound();
    }

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

        public OfAnnotated withAnnotations(Consumer<AnnotatableCreator> builder) {
            TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl(visible, invisible);
            builder.accept(tac);
            return copy(tac.visible(), tac.invisible());
        }

        public <A extends java.lang.annotation.Annotation> OfAnnotated withAnnotation(final Class<A> annotationType) {
            return withAnnotations(ac -> ac.withAnnotation(annotationType));
        }

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
