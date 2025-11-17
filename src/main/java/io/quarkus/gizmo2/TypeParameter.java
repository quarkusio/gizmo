package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A type parameter on a class, interface, or method.
 * The difference between type parameters and {@linkplain GenericType.OfTypeVariable type variables}
 * is that type parameters may declare bounds.
 */
public sealed abstract class TypeParameter implements GenericTyped {
    // these fields contribute to equality
    private final String name;
    private final Optional<GenericType.OfThrows> firstBound;
    private final List<GenericType.OfThrows> otherBounds;
    private final List<Annotation> visible;
    private final List<Annotation> invisible;

    GenericType.OfTypeVariable genericType;

    TypeParameter(final List<Annotation> visible, final List<Annotation> invisible, final String name,
            final Optional<GenericType.OfThrows> firstBound, final List<GenericType.OfThrows> otherBounds) {
        this.name = name;
        this.firstBound = firstBound;
        this.otherBounds = List.copyOf(otherBounds);
        this.visible = visible;
        this.invisible = invisible;
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

    /**
     * {@return the generic type corresponding to this type variable (not {@code null})}
     */
    public GenericType.OfTypeVariable genericType() {
        GenericType.OfTypeVariable genericType = this.genericType;
        if (genericType != null) {
            return genericType;
        }
        return this.genericType = GenericType.ofTypeVariable(name(), type());
    }

    public boolean hasGenericType() {
        return true;
    }

    /**
     * {@return the type of this type variable, which is equal to its erasure (not {@code null})}
     */
    public ClassDesc type() {
        return erasure();
    }

    /**
     * {@return {@code true} if this type variable is visible throughout the given class, or {@code false} if it is not}
     */
    public abstract boolean visibleIn(ClassDesc desc);

    /**
     * {@return {@code true} if this type variable is visible throughout the given method, or {@code false} if it is not}
     */
    public abstract boolean visibleIn(MethodDesc desc);

    /**
     * {@return {@code true} if this type variable is visible throughout the given constructor, or {@code false} if it is not}
     */
    public abstract boolean visibleIn(ConstructorDesc desc);

    /**
     * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
     */
    public final boolean equals(final Object obj) {
        return obj instanceof TypeParameter tp && equals(tp);
    }

    /**
     * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
     */
    public boolean equals(final TypeParameter other) {
        return other != null && name.equals(other.name) && firstBound.equals(other.firstBound)
                && otherBounds.equals(other.otherBounds)
                && visible.equals(other.visible) && invisible.equals(other.invisible);
    }

    /**
     * {@return the hash code for this type variable}
     */
    public int hashCode() {
        return Objects.hash(getClass(), name, firstBound, otherBounds, visible, invisible);
    }

    /**
     * {@return the name of this type variable}
     */
    public String toString() {
        return name();
    }

    /**
     * {@return the erased type of this type variable (not {@code null})}
     */
    public ClassDesc erasure() {
        return firstBound.map(GenericType::desc)
                .orElseGet(() -> otherBounds.stream().map(GenericType::desc).findFirst().orElse(CD_Object));
    }

    @SuppressWarnings("unused") // called from Util reflectively
    List<TypeAnnotation> computeAnnotations(RetentionPolicy retention, TypeAnnotation.TargetInfo targetInfo,
            ArrayList<TypeAnnotation> list, ArrayDeque<TypeAnnotation.TypePathComponent> path) {
        List<TypeAnnotation.TypePathComponent> pathSnapshot = List.copyOf(path);
        for (Annotation annotation : switch (retention) {
            case RUNTIME -> visible;
            case CLASS -> invisible;
            default -> throw Assert.impossibleSwitchCase(retention);
        }) {
            list.add(TypeAnnotation.of(targetInfo, pathSnapshot, annotation));
        }
        if (firstBound.isPresent() || !otherBounds.isEmpty()) {
            IntFunction<TypeAnnotation.TargetInfo> targetFn;
            if (targetInfo instanceof TypeAnnotation.TypeParameterTarget tpt) {
                targetFn = switch (targetInfo.targetType()) {
                    case METHOD_TYPE_PARAMETER ->
                        idx -> TypeAnnotation.TargetInfo.ofMethodTypeParameterBound(tpt.typeParameterIndex(), idx);
                    case CLASS_TYPE_PARAMETER ->
                        idx -> TypeAnnotation.TargetInfo.ofClassTypeParameterBound(tpt.typeParameterIndex(), idx);
                    default -> throw Assert.impossibleSwitchCase(targetInfo.targetType());
                };
            } else {
                throw new IllegalStateException();
            }
            firstBound.ifPresent(b -> {
                b.computeAnnotations(retention, targetFn.apply(0), list, path);
            });
            for (int i = 0; i < otherBounds.size(); i++) {
                otherBounds.get(i).computeAnnotations(retention, targetFn.apply(i + 1), list, path);
            }
        }
        return list;
    }

    /**
     * A type parameter on a class or interface.
     */
    public static final class OfType extends TypeParameter {
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
            return Util.equals(owner, desc);
        }

        public boolean visibleIn(final MethodDesc desc) {
            return visibleIn(desc.owner());
        }

        public boolean visibleIn(final ConstructorDesc desc) {
            return visibleIn(desc.owner());
        }

        public boolean equals(final TypeParameter other) {
            return other instanceof OfType ot && equals(ot);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfType other) {
            return this == other || super.equals(other) && Util.equals(owner, other.owner);
        }

        public int hashCode() {
            return super.hashCode() * 19 + owner.hashCode();
        }
    }

    /**
     * A type parameter on a method.
     */
    public static final class OfMethod extends TypeParameter {
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

        public boolean equals(final TypeParameter other) {
            return other instanceof OfMethod om && equals(om);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfMethod other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode() * 19 + owner.hashCode();
        }
    }

    /**
     * A type parameter on a constructor.
     */
    public static final class OfConstructor extends TypeParameter {
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

        public boolean equals(final TypeParameter other) {
            return other instanceof OfConstructor oc && equals(oc);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfConstructor other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode() * 19 + owner.hashCode();
        }
    }
}
