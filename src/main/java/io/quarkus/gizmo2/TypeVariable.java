package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A type variable on a class, interface, or method.
 */
public sealed abstract class TypeVariable implements GenericTyped {
    // these fields contribute to equality
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
        GenericDeclaration decl = typeVar.getGenericDeclaration();
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

    /**
     * {@return the generic type corresponding to this type variable (not {@code null})}
     */
    public GenericType.OfTypeVariable genericType() {
        return GenericType.ofTypeVariable(name(), type());
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
        return obj instanceof TypeVariable tv && equals(tv);
    }

    /**
     * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
     */
    public boolean equals(final TypeVariable other) {
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
     * A type variable on a class or interface.
     */
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

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfType other) {
            return this == other || super.equals(other) && owner.equals(other.owner);
        }

        public int hashCode() {
            return super.hashCode() * 19 + owner.hashCode();
        }
    }

    /**
     * A type variable on a method.
     */
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
     * A type variable on a constructor.
     */
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
