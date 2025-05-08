package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * A type variable on a class, interface, or method.
 */
public sealed abstract class TypeVariable implements GenericTyped {
    private final String name;
    private final Optional<GenericType.OfThrows> firstBound;
    private final List<GenericType.OfThrows> otherBounds;
    private final List<Annotation> visible;
    private final List<Annotation> invisible;

    GenericType.OfTypeVariable genericType;

    TypeVariable(final List<Annotation> visible, final List<Annotation> invisible, final String name,
            final Optional<GenericType.OfThrows> firstBound, final List<GenericType.OfThrows> otherBounds) {
        this.name = name;
        this.firstBound = firstBound;
        this.otherBounds = List.copyOf(otherBounds);
        this.visible = visible;
        this.invisible = invisible;
    }

    static TypeVariable of(final java.lang.reflect.TypeVariable<?> typeVar) {
        GenericDeclaration decl = typeVar.getGenericDeclaration();
        List<GenericType.OfThrows> allBounds = Stream.of(typeVar.getAnnotatedBounds())
                .map(GenericType::of)
                .map(GenericType.OfThrows.class::cast)
                .toList();
        Optional<GenericType.OfThrows> firstBound;
        List<GenericType.OfThrows> otherBounds;
        // make a best-effort guess to populate this stuff as correctly as possible
        if (allBounds.isEmpty() || allBounds.size() == 1 && allBounds.get(0).equals(GenericType.ofClass(Object.class))) {
            firstBound = Optional.empty();
            otherBounds = List.of();
        } else if (typeVar.getBounds()[0] instanceof Class<?> c && !c.isInterface()) {
            firstBound = Optional.of(allBounds.get(0));
            otherBounds = allBounds.subList(1, allBounds.size());
        } else if (allBounds.size() == 1 && allBounds.get(0) instanceof GenericType.OfTypeVariable) {
            firstBound = Optional.of(allBounds.get(0));
            otherBounds = List.of();
        } else {
            firstBound = Optional.empty();
            otherBounds = allBounds;
        }
        TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl();
        AnnotatableCreator.from(typeVar).accept(tac);
        if (decl instanceof Class<?> c) {
            return new OfType(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds, Util.classDesc(c));
        } else if (decl instanceof Method m) {
            return new OfMethod(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds, MethodDesc.of(m));
        } else if (decl instanceof Constructor<?> c) {
            return new OfConstructor(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds,
                    ConstructorDesc.of(c));
        } else {
            // should be impossible, actually
            throw new IllegalStateException("Unexpected declaration " + decl);
        }
    }

    /**
     * {@return the optional first (primary) bound (not null)}
     * This should be a class or type variable bound.
     */
    public Optional<GenericType.OfThrows> firstBound() {
        return firstBound;
    }

    /**
     * {@return the other (secondary) bounds (not {@code null})}
     * This should be a list of interface types, or empty if the first bound is a type variable.
     */
    public List<GenericType.OfThrows> otherBounds() {
        return otherBounds;
    }

    /**
     * {@return the type variable name}
     */
    public String name() {
        return name;
    }

    public GenericType.OfTypeVariable genericType() {
        return GenericType.ofTypeVariable(this);
    }

    public ClassDesc type() {
        return genericType.desc();
    }

    List<Annotation> visible() {
        return visible;
    }

    List<Annotation> invisible() {
        return invisible;
    }

    public abstract boolean visibleIn(ClassDesc desc);

    public abstract boolean visibleIn(MethodDesc desc);

    public abstract boolean visibleIn(ConstructorDesc desc);

    public final boolean equals(final Object obj) {
        return obj instanceof TypeVariable tv && equals(tv);
    }

    public boolean equals(final TypeVariable other) {
        return other != null && name.equals(other.name) && otherBounds.equals(other.otherBounds);
    }

    public int hashCode() {
        return Objects.hash(name, otherBounds);
    }

    public String toString() {
        return name();
    }

    public ClassDesc erasure() {
        return otherBounds().stream().findFirst().map(GenericType::desc).orElse(CD_Object);
    }

    public static final class OfType extends TypeVariable {
        private final ClassDesc owner;

        OfType(final List<Annotation> visible, final List<Annotation> invisible, final String name,
                final Optional<GenericType.OfThrows> firstBound, final List<GenericType.OfThrows> otherBounds,
                final ClassDesc owner) {
            super(visible, invisible, name, firstBound, otherBounds);
            this.owner = owner;
        }

        /**
         * {@return the owner of the type variable}
         */
        public ClassDesc owner() {
            return owner;
        }

        public boolean visibleIn(final ClassDesc desc) {
            return owner.equals(desc);
        }

        public boolean visibleIn(final MethodDesc desc) {
            return visibleIn(desc.owner());
        }

        public boolean visibleIn(final ConstructorDesc desc) {
            return visibleIn(desc.owner());
        }

        public boolean equals(final TypeVariable other) {
            return other instanceof OfType ot && equals(ot);
        }

        public boolean equals(final OfType other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode() * 19 + owner.hashCode();
        }
    }

    public static final class OfMethod extends TypeVariable {
        private final MethodDesc owner;

        OfMethod(final List<Annotation> visible, final List<Annotation> invisible, final String name,
                final Optional<GenericType.OfThrows> firstBound, final List<GenericType.OfThrows> otherBounds,
                final MethodDesc owner) {
            super(visible, invisible, name, firstBound, otherBounds);
            this.owner = owner;
        }

        /**
         * {@return the owner of the type variable}
         */
        public MethodDesc owner() {
            return owner;
        }

        public boolean visibleIn(final ClassDesc desc) {
            // only visible in methods
            return false;
        }

        public boolean visibleIn(final MethodDesc desc) {
            return desc.equals(owner);
        }

        public boolean visibleIn(final ConstructorDesc desc) {
            return false;
        }

        public boolean equals(final TypeVariable other) {
            return other instanceof OfMethod om && equals(om);
        }

        public boolean equals(final OfMethod other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }

    public static final class OfConstructor extends TypeVariable {
        private final ConstructorDesc owner;

        OfConstructor(final List<Annotation> visible, final List<Annotation> invisible, final String name,
                final Optional<GenericType.OfThrows> firstBound, final List<GenericType.OfThrows> otherBounds,
                final ConstructorDesc owner) {
            super(visible, invisible, name, firstBound, otherBounds);
            this.owner = owner;
        }

        /**
         * {@return the owner of the type variable}
         */
        public ConstructorDesc owner() {
            return owner;
        }

        public boolean visibleIn(final ClassDesc desc) {
            // only visible in constructors
            return false;
        }

        public boolean visibleIn(final MethodDesc desc) {
            return false;
        }

        public boolean visibleIn(final ConstructorDesc desc) {
            return desc.equals(owner);
        }

        public boolean equals(final TypeVariable other) {
            return other instanceof OfConstructor om && equals(om);
        }

        public boolean equals(final OfConstructor other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode();
        }
    }
}
