package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.Annotation;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;

/**
 * A type variable on a class, interface, or method.
 */
public sealed abstract class TypeVariable implements GenericTyped {
    // TODO arguments for classes we do not define
    private final String name;
    private final List<GenericType.OfThrows> bounds;
    private final List<Annotation> visible;
    private final List<Annotation> invisible;

    GenericType.OfTypeVariable genericType;

    TypeVariable(final List<Annotation> visible, final List<Annotation> invisible, final String name,
            final List<GenericType.OfThrows> bounds) {
        this.name = name;
        this.bounds = bounds;
        this.visible = visible;
        this.invisible = invisible;
    }

    static TypeVariable of(final java.lang.reflect.TypeVariable<?> typeVar) {
        GenericDeclaration decl = typeVar.getGenericDeclaration();
        List<GenericType.OfThrows> bounds = Stream.of(typeVar.getAnnotatedBounds())
                .map(GenericType::of)
                .map(GenericType.OfThrows.class::cast)
                .toList();
        // TODO: populate annotations
        if (decl instanceof Class<?> c) {
            return new OfType(List.of(), List.of(), typeVar.getName(), bounds, Util.classDesc(c));
        } else if (decl instanceof Method m) {
            return new OfMethod(List.of(), List.of(), typeVar.getName(), bounds, MethodDesc.of(m));
        } else if (decl instanceof Constructor<?> c) {
            return new OfConstructor(List.of(), List.of(), typeVar.getName(), bounds, ConstructorDesc.of(c));
        } else {
            // should be impossible, actually
            throw new IllegalStateException("Unexpected declaration " + decl);
        }
    }

    public List<GenericType.OfThrows> bounds() {
        return bounds;
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
        return other != null && name.equals(other.name) && bounds.equals(other.bounds);
    }

    public int hashCode() {
        return Objects.hash(name, bounds);
    }

    public String toString() {
        return name();
    }

    public ClassDesc erasure() {
        return bounds().stream().findFirst().map(GenericType::desc).orElse(CD_Object);
    }

    public static final class OfType extends TypeVariable {
        private final ClassDesc owner;

        OfType(final List<Annotation> visible, final List<Annotation> invisible, final String name,
                final List<GenericType.OfThrows> bounds, final ClassDesc owner) {
            super(visible, invisible, name, bounds);
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
                final List<GenericType.OfThrows> bounds, final MethodDesc owner) {
            super(visible, invisible, name, bounds);
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
                final List<GenericType.OfThrows> bounds, final ConstructorDesc owner) {
            super(visible, invisible, name, bounds);
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
