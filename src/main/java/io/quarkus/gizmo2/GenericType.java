package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.invoke.ConstantBootstraps;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.creator.AnnotationCreator;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A generic Java type, which may include type arguments as well as type annotations.
 * <p>
 * This class models types for the purpose of code generation, and is not generally suitable
 * for usage in any kind of type-checking system.
 * <p>
 * Generic types are {@linkplain OfPrimitive primitive types} (including {@code void})
 * and {@linkplain OfReference reference types}. Reference types are {@linkplain OfArray array types}
 * and {@linkplain OfThrows throwable types}. Throwable types are {@linkplain OfClass class types}
 * (including {@linkplain OfRootClass "root" classes} and {@linkplain OfInnerClass inner classes})
 * and {@linkplain OfTypeVariable type variables}. Note that in this model, class types represent
 * all of non-generic class types, parameterized types (generic class types with type arguments)
 * and raw types (generic class types without type arguments).
 */
public abstract class GenericType {
    final List<Annotation> visible;
    final List<Annotation> invisible;
    OfArray arrayType;

    GenericType(final List<Annotation> visible, final List<Annotation> invisible) {
        this.visible = visible;
        this.invisible = invisible;
    }

    /**
     * {@return the given type as an erased generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static GenericType of(Class<?> type) {
        Class<?> enclosingClass = type.getEnclosingClass();
        if (enclosingClass != null) {
            if (Modifier.isStatic(type.getModifiers())) {
                // "root" class (nested interfaces, annotations, enums or records are always `static`)
                return of(Util.classDesc(type));
            } else {
                return ofInnerClass(ofClass(enclosingClass), type.getSimpleName());
            }
        } else {
            return of(Util.classDesc(type));
        }
    }

    /**
     * {@return the given type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     * @param typeArguments the type arguments, if any (must not be {@code null})
     * @throws IllegalArgumentException if the nonzero number of type arguments does not match
     *         the number of type parameters on the given class
     */
    public static GenericType of(Class<?> type, List<TypeArgument> typeArguments) {
        int tpCount = type.getTypeParameters().length;
        int taSize = typeArguments.size();
        if (tpCount != taSize && taSize != 0) {
            throw new IllegalArgumentException("Invalid number of type arguments (expected %d but got %d)"
                    .formatted(tpCount, taSize));
        }
        GenericType base = of(type);
        if (base instanceof OfClass oc) {
            return oc.withArguments(typeArguments);
        }
        if (typeArguments.isEmpty()) {
            return base;
        }
        throw new IllegalArgumentException("Invalid number of type arguments (expected 0 but got %d)".formatted(taSize));
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param type the class object for the class or interface (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(Class<?> type) {
        if (type.isPrimitive() || type.isArray()) {
            throw new IllegalArgumentException("Type %s does not represent a class or interface".formatted(type));
        }
        return (OfClass) of(type);
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param desc the descriptor for the class or interface (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(ClassDesc desc) {
        if (!desc.isClassOrInterface()) {
            throw new IllegalArgumentException("Type %s does not represent a class or interface".formatted(desc.displayName()));
        }
        return (OfClass) of(desc);
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param type the class object for the class or interface (must not be {@code null})
     * @param typeArguments the type arguments for the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(Class<?> type, List<TypeArgument> typeArguments) {
        return ofClass(type).withArguments(typeArguments);
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param type the class object for the class or interface (must not be {@code null})
     * @param typeArguments the type arguments for the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(Class<?> type, TypeArgument... typeArguments) {
        return ofClass(type, List.of(typeArguments));
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param desc the descriptor for the class or interface (must not be {@code null})
     * @param typeArguments the type arguments for the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(ClassDesc desc, List<TypeArgument> typeArguments) {
        return ofClass(desc).withArguments(typeArguments);
    }

    /**
     * {@return a generic type for the given class or interface type}
     *
     * @param desc the descriptor for the class or interface (must not be {@code null})
     * @param typeArguments the type arguments for the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a class or interface
     */
    public static OfClass ofClass(ClassDesc desc, TypeArgument... typeArguments) {
        return ofClass(desc, List.of(typeArguments));
    }

    /**
     * {@return a generic type for the given array class}
     *
     * @param type the class object for the array type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent an array type
     */
    public static OfArray ofArray(Class<?> type) {
        if (!type.isArray()) {
            throw new IllegalArgumentException("Type %s does not represent an array type".formatted(type));
        }
        return (OfArray) of(type);
    }

    /**
     * {@return a generic type for the given array type}
     *
     * @param type the array type (must not be {@code null})
     * @throws IllegalArgumentException if the given type does not represent an array type
     */
    public static OfArray ofArray(ClassDesc type) {
        if (!type.isArray()) {
            throw new IllegalArgumentException("Type %s does not represent an array type".formatted(type));
        }
        return (OfArray) of(type);
    }

    /**
     * {@return a generic type for the given primitive class}
     *
     * @param type the class object for the primitive type (must not be {@code null})
     * @throws IllegalArgumentException if the given type class object does not represent a primitive type
     */
    public static OfPrimitive ofPrimitive(Class<?> type) {
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("Type %s does not represent a primitive type".formatted(type));
        }
        return (OfPrimitive) of(type);
    }

    /**
     * {@return a generic type for the given primitive type}
     *
     * @param type the primitive type (must not be {@code null})
     * @throws IllegalArgumentException if the given type does not represent a primitive type
     */
    public static OfPrimitive ofPrimitive(ClassDesc type) {
        if (!type.isPrimitive()) {
            throw new IllegalArgumentException("Type %s does not represent a primitive type".formatted(type));
        }
        return (OfPrimitive) of(type);
    }

    /**
     * {@return the given type as an erased generic type (not {@code null})}
     *
     * @param desc the type descriptor (must not be {@code null})
     */
    public static GenericType of(ClassDesc desc) {
        if (desc.isClassOrInterface()) {
            return new OfRootClass(List.of(), List.of(), desc, List.of());
        }
        if (desc.isPrimitive()) {
            return OfPrimitive.baseItems.get(desc);
        }
        assert desc.isArray();
        return new OfArray(List.of(), List.of(), of(desc.componentType()));
    }

    /**
     * {@return the given type as a generic type (not {@code null})}
     *
     * @param desc the type descriptor (must not be {@code null})
     * @param typeArguments the type arguments, if any (must not be {@code null})
     * @throws IllegalArgumentException if the nonzero number of type arguments does not match
     *         the number of type parameters on the given class
     */
    public static GenericType of(ClassDesc desc, List<TypeArgument> typeArguments) {
        GenericType genericType = of(desc);
        if (typeArguments.isEmpty()) {
            return genericType;
        } else if (genericType instanceof OfClass oc) {
            return oc.withArguments(typeArguments);
        } else {
            throw new IllegalArgumentException("Type %s cannot have type arguments".formatted(desc.displayName()));
        }
    }

    /**
     * {@return the generic type of a type variable (not {@code null})}
     *
     * @param name the type variable name (must not be {@code null})
     * @param bound the type variable's erased bound (must not be {@code null})
     */
    public static OfTypeVariable ofTypeVariable(String name, Class<?> bound) {
        return ofTypeVariable(name, Util.classDesc(bound));
    }

    /**
     * {@return the generic type of a type variable (not {@code null})}
     *
     * @param name the type variable name (must not be {@code null})
     * @param bound the type variable's erased bound (must not be {@code null})
     */
    public static OfTypeVariable ofTypeVariable(String name, ClassDesc bound) {
        return OfTypeVariable.getOrMake(name, bound);
    }

    /**
     * {@return the generic type of a type variable (not {@code null})}
     * The bound of the type variable is assumed to be {@code java.lang.Object}.
     *
     * @param name the type variable name (must not be {@code null})
     */
    public static OfTypeVariable ofTypeVariable(String name) {
        return ofTypeVariable(name, CD_Object);
    }

    /**
     * {@return a generic type representing an inner class of another class (not {@code null})}
     * Note that {@code static} member classes are <em>not</em> inner classes and are represented
     * by {@link OfRootClass}.
     *
     * @param outerClass the enclosing class generic type (must not be {@code null})
     * @param name the inner class name (must not be {@code null})
     */
    public static OfInnerClass ofInnerClass(OfClass outerClass, String name) {
        return new OfInnerClass(List.of(), List.of(),
                Assert.checkNotNullParam("outerClass", outerClass),
                Assert.checkNotNullParam("name", name),
                List.of());
    }

    /**
     * {@return the given reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type is a wildcard type or is not recognized
     */
    public static GenericType of(Type type) {
        if (type instanceof Class<?> c) {
            return of(c);
        } else if (type instanceof GenericArrayType gat) {
            return of(gat);
        } else if (type instanceof ParameterizedType pt) {
            return of(pt);
        } else if (type instanceof java.lang.reflect.TypeVariable<?> tv) {
            return of(tv);
        } else if (type instanceof WildcardType) {
            throw noWildcards();
        } else {
            throw new IllegalArgumentException("Invalid type " + type.getClass());
        }
    }

    /**
     * {@return the given array reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static OfArray of(GenericArrayType type) {
        return of(type.getGenericComponentType()).arrayType();
    }

    /**
     * {@return the given type variable reflection type as a generic type (not {@code null})}
     *
     * @param type the type variable (must not be {@code null})
     */
    public static OfTypeVariable of(java.lang.reflect.TypeVariable<?> type) {
        return ofTypeVariable(type.getName(), erase(type));
    }

    private static ClassDesc erase(Type type) {
        if (type instanceof Class<?> c) {
            return Util.classDesc(c);
        } else if (type instanceof ParameterizedType pt) {
            return Util.classDesc((Class<?>) pt.getRawType());
        } else if (type instanceof java.lang.reflect.TypeVariable<?> tv) {
            return tv.getBounds().length == 0 ? CD_Object : erase(tv.getBounds()[0]);
        } else if (type instanceof WildcardType wt) {
            return wt.getUpperBounds().length == 0 ? CD_Object : erase(wt.getUpperBounds()[0]);
        } else if (type instanceof GenericArrayType gat) {
            return erase(gat.getGenericComponentType()).arrayType();
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * {@return the given parameterized reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static OfClass of(ParameterizedType type) {
        return ((OfClass) of(type.getRawType())).withArguments(
                Stream.of(type.getActualTypeArguments()).map(TypeArgument::of).toList());
    }

    /**
     * {@return the given annotated reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type is a wildcard type or is not recognized
     */
    public static GenericType of(AnnotatedType type) {
        if (type instanceof AnnotatedArrayType aat) {
            return of(aat);
        } else if (type instanceof AnnotatedParameterizedType apt) {
            return of(apt);
        } else if (type instanceof AnnotatedTypeVariable atv) {
            return of(atv);
        } else if (type instanceof AnnotatedWildcardType) {
            throw noWildcards();
        } else {
            // annotated plain type
            return of(type.getType()).withAnnotations(AnnotatableCreator.from(type));
        }
    }

    /**
     * {@return the given annotated array reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static OfArray of(AnnotatedArrayType type) {
        return of(type.getAnnotatedGenericComponentType()).arrayType().withAnnotations(AnnotatableCreator.from(type));
    }

    /**
     * {@return the given annotated parameterized type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static GenericType of(AnnotatedParameterizedType type) {
        List<TypeArgument> typeArgs = Stream.of(type.getAnnotatedActualTypeArguments()).map(TypeArgument::of).toList();
        ParameterizedType pt = (ParameterizedType) type.getType();
        return of((Class<?>) pt.getRawType(), typeArgs).withAnnotations(AnnotatableCreator.from(type));
    }

    /**
     * {@return the given type variable annotated reflection type as a generic type (not {@code null})}
     *
     * @param type the type variable (must not be {@code null})
     */
    public static OfTypeVariable of(AnnotatedTypeVariable type) {
        java.lang.reflect.TypeVariable<?> typeVar = (java.lang.reflect.TypeVariable<?>) type.getType();
        return ofTypeVariable(typeVar.getName(), erase(typeVar)).withAnnotations(AnnotatableCreator.from(type));
    }

    /**
     * {@return this generic type with annotations added by the given builder (not {@code null})}
     *
     * @param builder the annotation builder (must not be {@code null})
     */
    public GenericType withAnnotations(Consumer<AnnotatableCreator> builder) {
        TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl(visible, invisible);
        builder.accept(tac);
        if (visible.equals(tac.visible()) && invisible.equals(tac.invisible())) {
            // no annotations added
            return this;
        }
        return copy(tac.visible(), tac.invisible());
    }

    /**
     * {@return this generic type with the given additional annotation (not {@code null})}
     *
     * @param annotationType the annotation type (must not be {@code null})
     */
    public <A extends java.lang.annotation.Annotation> GenericType withAnnotation(Class<A> annotationType) {
        return withAnnotation(annotationType, ac -> {
        });
    }

    /**
     * {@return this generic type with the given additional annotation (not {@code null})}
     *
     * @param annotationType the annotation type (must not be {@code null})
     * @param builder the builder for the given annotation type (must not be {@code null})
     */
    public <A extends java.lang.annotation.Annotation> GenericType withAnnotation(Class<A> annotationType,
            Consumer<AnnotationCreator<A>> builder) {
        return withAnnotations(ac -> ac.addAnnotation(annotationType, builder));
    }

    /**
     * {@return {@code true} if this type has no type arguments, or {@code false} if it has type arguments}
     */
    public abstract boolean isRaw();

    /**
     * {@return {@code true} if this type has type annotations with given retention policy,
     * or {@code false} if it does not}
     *
     * @param retention the retention policy (must not be {@code null})
     */
    public final boolean hasAnnotations(RetentionPolicy retention) {
        return switch (retention) {
            case RUNTIME -> hasVisibleAnnotations();
            case CLASS -> hasInvisibleAnnotations();
            case SOURCE -> false;
        };
    }

    /**
     * {@return {@code true} if this type has runtime-visible type annotations, or {@code false} if it does not}
     */
    public boolean hasVisibleAnnotations() {
        return !visible.isEmpty();
    }

    /**
     * {@return {@code true} if this type has runtime-invisible type annotations, or {@code false} if it does not}
     */
    public boolean hasInvisibleAnnotations() {
        return !invisible.isEmpty();
    }

    /**
     * {@return {@code true} if this type has any type annotations, or {@code false} if it does not}
     */
    public final boolean hasAnnotations() {
        return hasVisibleAnnotations() || hasInvisibleAnnotations();
    }

    /**
     * {@return the array type whose component type is this type}
     */
    public OfArray arrayType() {
        OfArray arrayType = this.arrayType;
        if (arrayType == null) {
            arrayType = this.arrayType = new OfArray(List.of(), List.of(), this);
        }
        return arrayType;
    }

    /**
     * {@return the erased type descriptor for this generic type (not {@code null})}
     */
    public abstract ClassDesc desc();

    /**
     * Append a string representation of this type to the given string builder.
     *
     * @param b the string builder (must not be {@code null})
     * @return the string builder that was passed in (not {@code null})
     */
    public StringBuilder toString(StringBuilder b) {
        for (Annotation annotation : visible) {
            Util.appendAnnotation(b, annotation).append(' ');
        }
        for (Annotation annotation : invisible) {
            Util.appendAnnotation(b, annotation).append(' ');
        }
        return b;
    }

    /**
     * {@return a string representation of this type}
     */
    public final String toString() {
        return toString(new StringBuilder()).toString();
    }

    /**
     * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
     */
    public final boolean equals(final Object obj) {
        return obj instanceof GenericType gt && equals(gt);
    }

    /**
     * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
     */
    public boolean equals(GenericType gt) {
        return this == gt || visible.equals(gt.visible) && invisible.equals(gt.invisible);
    }

    /**
     * {@return the hash code of this generic type}
     */
    public int hashCode() {
        return Objects.hash(getClass(), visible, invisible);
    }

    abstract GenericType copy(List<Annotation> visible, List<Annotation> invisible);

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
        return list;
    }

    /**
     * A generic type corresponding to a type variable.
     */
    public static final class OfTypeVariable extends OfThrows {
        private static final VarHandle cacheHandle = ConstantBootstraps.arrayVarHandle(MethodHandles.lookup(), "_",
                VarHandle.class, OfTypeVariable[].class);
        private static final OfTypeVariable[] cache = new OfTypeVariable[26];

        private final String name;
        private final ClassDesc desc;

        OfTypeVariable(final List<Annotation> visible, final List<Annotation> invisible, final String name,
                final ClassDesc desc) {
            super(visible, invisible);
            this.name = name;
            this.desc = desc;
        }

        static OfTypeVariable getOrMake(String name, ClassDesc desc) {
            if (name.length() == 1 && desc.equals(CD_Object)) {
                char c = name.charAt(0);
                if ('A' <= c && c <= 'Z') {
                    OfTypeVariable type = (OfTypeVariable) cacheHandle.getVolatile(cache, c - 'A');
                    if (type == null) {
                        type = new OfTypeVariable(List.of(), List.of(), name, CD_Object);
                        OfTypeVariable witness = (OfTypeVariable) cacheHandle.compareAndExchange(cache, c - 'A', null, type);
                        if (witness != null) {
                            type = witness;
                        }
                    }
                    return type;
                }
            }
            return new OfTypeVariable(List.of(), List.of(), name, desc);
        }

        public ClassDesc desc() {
            return desc;
        }

        public String name() {
            return name;
        }

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(name);
        }

        public OfTypeVariable withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfTypeVariable) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfTypeVariable withAnnotation(final Class<A> annotationType) {
            return (OfTypeVariable) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfTypeVariable withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfTypeVariable) super.withAnnotation(annotationType, builder);
        }

        public boolean isRaw() {
            return false;
        }

        OfTypeVariable copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfTypeVariable(visible, invisible, name, desc);
        }

        public boolean equals(final OfThrows other) {
            return other instanceof OfTypeVariable tv && equals(tv);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfTypeVariable tvt) {
            return this == tvt || super.equals(tvt) && name.equals(tvt.name) && desc.equals(tvt.desc);
        }

        public int hashCode() {
            return super.hashCode() * 19 + Objects.hash(name, desc);
        }
    }

    /**
     * A generic type corresponding to a reference type.
     */
    public static abstract class OfReference extends GenericType {
        TypeArgument.OfExact exactArg;
        TypeArgument.OfExtends extendsArg;
        TypeArgument.OfSuper superArg;

        OfReference(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }

        public OfReference withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfReference) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfReference withAnnotation(final Class<A> annotationType) {
            return (OfReference) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfReference withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfReference) super.withAnnotation(annotationType, builder);
        }

        public final boolean equals(final GenericType gt) {
            return gt instanceof OfReference or && equals(or);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfReference other) {
            return super.equals(other);
        }
    }

    /**
     * A generic type corresponding to a reference type that is suitable for use in a method {@code throws} clause.
     */
    public static abstract class OfThrows extends OfReference {
        OfThrows(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }

        public OfThrows withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfThrows) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfThrows withAnnotation(final Class<A> annotationType) {
            return (OfThrows) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfThrows withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfThrows) super.withAnnotation(annotationType, builder);
        }

        public final boolean equals(final OfReference other) {
            return other instanceof OfThrows ot && equals(ot);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfThrows other) {
            return super.equals(other);
        }
    }

    /**
     * A generic type corresponding to an array type.
     */
    public static final class OfArray extends OfReference {
        private final GenericType componentType;
        private ClassDesc desc;

        OfArray(final List<Annotation> visible, final List<Annotation> invisible, final GenericType componentType) {
            super(visible, invisible);
            this.componentType = componentType;
        }

        /**
         * {@return the component type of this array type}
         */
        public GenericType componentType() {
            return componentType;
        }

        List<TypeAnnotation> computeAnnotations(final RetentionPolicy retention, final TypeAnnotation.TargetInfo targetInfo,
                final ArrayList<TypeAnnotation> list, final ArrayDeque<TypeAnnotation.TypePathComponent> path) {
            componentType.computeAnnotations(retention, targetInfo, list, path);
            path.addLast(TypeAnnotation.TypePathComponent.ARRAY);
            super.computeAnnotations(retention, targetInfo, list, path);
            path.removeLast();
            return list;
        }

        OfArray copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfArray(visible, invisible, componentType);
        }

        public OfArray withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfArray) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfArray withAnnotation(final Class<A> annotationType) {
            return (OfArray) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfArray withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfArray) super.withAnnotation(annotationType, builder);
        }

        public boolean isRaw() {
            return componentType.isRaw();
        }

        public boolean hasVisibleAnnotations() {
            return super.hasVisibleAnnotations() || componentType.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return super.hasInvisibleAnnotations() || componentType.hasInvisibleAnnotations();
        }

        public ClassDesc desc() {
            ClassDesc desc = this.desc;
            if (desc == null) {
                desc = this.desc = componentType.desc().arrayType();
            }
            return desc;
        }

        public StringBuilder toString(final StringBuilder b) {
            // supertype annotations []
            return super.toString(componentType.toString(b)).append("[]");
        }

        public boolean equals(final OfReference other) {
            return other instanceof OfArray oa && equals(oa);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfArray oa) {
            return this == oa || super.equals(oa) && componentType.equals(oa.componentType);
        }

        public int hashCode() {
            return super.hashCode() * 19 + componentType.hashCode();
        }
    }

    /**
     * A generic type of a class or interface (including specialized cases: enums, records, or annotations).
     * Includes all of non-generic class types, parameterized types (generic class types with type arguments)
     * and raw types (generic class types without type arguments).
     */
    public static abstract class OfClass extends OfThrows {
        final List<TypeArgument> typeArguments;

        OfClass(final List<Annotation> visible, final List<Annotation> invisible, final List<TypeArgument> typeArguments) {
            super(visible, invisible);
            this.typeArguments = typeArguments;
        }

        /**
         * {@return the list of type arguments (not {@code null})}
         */
        public List<TypeArgument> typeArguments() {
            return typeArguments;
        }

        public OfClass withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfClass) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfClass withAnnotation(final Class<A> annotationType) {
            return (OfClass) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfClass withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfClass) super.withAnnotation(annotationType, builder);
        }

        public boolean isRaw() {
            return typeArguments.isEmpty();
        }

        public boolean hasVisibleAnnotations() {
            return super.hasVisibleAnnotations() || typeArguments.stream().anyMatch(TypeArgument::hasVisibleAnnotations);
        }

        public boolean hasInvisibleAnnotations() {
            return super.hasInvisibleAnnotations() || typeArguments.stream().anyMatch(TypeArgument::hasInvisibleAnnotations);
        }

        /**
         * {@return a copy of this generic type with the given type arguments}
         *
         * @param newArguments the new type arguments (must not be {@code null})
         * @throws IllegalArgumentException if this type has type arguments and the given number of arguments
         *         does not match the current number of arguments
         */
        public OfClass withArguments(List<TypeArgument> newArguments) {
            if (typeArguments.equals(newArguments)) {
                return this;
            }
            int taSize = typeArguments.size();
            int naSize = newArguments.size();
            if (taSize == 0 || naSize == 0 || taSize == naSize) {
                return copy(visible, invisible, newArguments);
            } else {
                throw new IllegalArgumentException("Invalid number of type arguments (expected %d but got %d)"
                        .formatted(naSize, taSize));
            }
        }

        public final boolean equals(final OfThrows other) {
            return other instanceof OfClass oc && equals(oc);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfClass other) {
            return super.equals(other) && typeArguments.equals(other.typeArguments);
        }

        public int hashCode() {
            return super.hashCode() * 19 + typeArguments.hashCode();
        }

        abstract OfClass copy(final List<Annotation> visible, final List<Annotation> invisible,
                final List<TypeArgument> typeArguments);

        List<TypeAnnotation> computeAnnotations(final RetentionPolicy retention, final TypeAnnotation.TargetInfo targetInfo,
                final ArrayList<TypeAnnotation> list, final ArrayDeque<TypeAnnotation.TypePathComponent> path) {
            super.computeAnnotations(retention, targetInfo, list, path);
            List<TypeArgument> typeArguments = this.typeArguments;
            List<TypeAnnotation.TypePathComponent> pathSnapshot;
            int size = typeArguments.size();
            for (int i = 0; i < size; i++) {
                path.addLast(TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, i));
                TypeArgument arg = typeArguments.get(i);
                if (arg instanceof TypeArgument.OfWildcard wld) {
                    List<Annotation> argAnnotations = switch (retention) {
                        case RUNTIME -> wld.visible();
                        case CLASS -> wld.invisible();
                        default -> throw Assert.impossibleSwitchCase(retention);
                    };
                    pathSnapshot = List.copyOf(path);
                    for (Annotation annotation : argAnnotations) {
                        list.add(TypeAnnotation.of(targetInfo, pathSnapshot, annotation));
                    }
                    if (wld instanceof TypeArgument.OfBounded bnd) {
                        // extends or super; add the inner annotations
                        path.addLast(TypeAnnotation.TypePathComponent.WILDCARD);
                        bnd.bound().computeAnnotations(retention, targetInfo, list, path);
                        path.removeLast();
                    }
                } else if (arg instanceof TypeArgument.OfExact exact) {
                    // exact
                    exact.type().computeAnnotations(retention, targetInfo, list, path);
                } else {
                    throw Assert.unreachableCode();
                }
                path.removeLast();
            }
            return list;
        }

        StringBuilder typeArgumentsToString(StringBuilder b) {
            Iterator<TypeArgument> iter = typeArguments.iterator();
            if (iter.hasNext()) {
                b.append('<');
                iter.next().toString(b);
                while (iter.hasNext()) {
                    b.append(", ");
                    iter.next().toString(b);
                }
                b.append('>');
            }
            return b;
        }
    }

    /**
     * A generic type corresponding to a "root" (non-inner) class.
     * "Root" classes are top-level classes and {@code static} member classes.
     */
    public static final class OfRootClass extends OfClass {
        private final ClassDesc desc;

        OfRootClass(final List<Annotation> visible, final List<Annotation> invisible, final ClassDesc desc,
                final List<TypeArgument> typeArguments) {
            super(visible, invisible, typeArguments);
            this.desc = desc;
        }

        public OfRootClass withArguments(final List<TypeArgument> newArguments) {
            if (typeArguments.equals(newArguments)) {
                return this;
            }
            return (OfRootClass) super.withArguments(newArguments);
        }

        public ClassDesc desc() {
            return desc;
        }

        OfRootClass copy(final List<Annotation> visible, final List<Annotation> invisible,
                final List<TypeArgument> typeArguments) {
            return new OfRootClass(visible, invisible, desc, typeArguments);
        }

        OfRootClass copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfRootClass(visible, invisible, desc, typeArguments);
        }

        public OfRootClass withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfRootClass) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfRootClass withAnnotation(final Class<A> annotationType) {
            return (OfRootClass) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfRootClass withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfRootClass) super.withAnnotation(annotationType, builder);
        }

        public boolean equals(final OfClass other) {
            return other instanceof OfRootClass orc && equals(orc);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfRootClass other) {
            return this == other || super.equals(other) && desc.equals(other.desc);
        }

        public int hashCode() {
            return super.hashCode() * 19 + desc.hashCode();
        }

        public StringBuilder toString(final StringBuilder b) {
            return typeArgumentsToString(super.toString(b).append(Util.binaryName(desc)));
        }
    }

    /**
     * A generic type corresponding to an inner class.
     * <p>
     * Note that {@code static} member classes are <em>not</em> inner classes
     * and are represented by {@link OfRootClass}.
     */
    public static final class OfInnerClass extends OfClass {
        private final OfClass outerType;
        private final String name;
        private ClassDesc desc;

        OfInnerClass(final List<Annotation> visible, final List<Annotation> invisible, final OfClass outerType,
                final String name, final List<TypeArgument> typeArguments) {
            super(visible, invisible, typeArguments);
            this.outerType = outerType;
            this.name = name;
        }

        public ClassDesc desc() {
            ClassDesc desc = this.desc;
            if (desc == null) {
                desc = this.desc = outerType.desc().nested(name);
            }
            return desc;
        }

        /**
         * {@return the enclosing class of this inner class}
         */
        public OfClass outerType() {
            return outerType;
        }

        public String name() {
            return name;
        }

        OfInnerClass copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfInnerClass(visible, invisible, outerType, name, typeArguments);
        }

        OfInnerClass copy(final List<Annotation> visible, final List<Annotation> invisible,
                final List<TypeArgument> typeArguments) {
            return new OfInnerClass(visible, invisible, outerType, name, typeArguments);
        }

        public OfInnerClass withOuterType(OfClass outerType) {
            return new OfInnerClass(visible, invisible, Assert.checkNotNullParam("outerType", outerType), name, typeArguments);
        }

        public OfInnerClass withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfInnerClass) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfInnerClass withAnnotation(final Class<A> annotationType) {
            return (OfInnerClass) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfInnerClass withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfInnerClass) super.withAnnotation(annotationType, builder);
        }

        public boolean hasVisibleAnnotations() {
            return super.hasVisibleAnnotations() || outerType.hasVisibleAnnotations();
        }

        public boolean hasInvisibleAnnotations() {
            return super.hasInvisibleAnnotations() || outerType.hasInvisibleAnnotations();
        }

        public boolean equals(final OfClass other) {
            return other instanceof OfInnerClass oic && equals(oic);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfInnerClass other) {
            return this == other || super.equals(other) && outerType.equals(other.outerType);
        }

        public int hashCode() {
            return super.hashCode() * 19 + outerType.hashCode();
        }

        public StringBuilder toString(final StringBuilder b) {
            return typeArgumentsToString(super.toString(outerType.toString(b).append('.')).append(name));
        }

        List<TypeAnnotation> computeAnnotations(final RetentionPolicy retention, final TypeAnnotation.TargetInfo targetInfo,
                final ArrayList<TypeAnnotation> list, final ArrayDeque<TypeAnnotation.TypePathComponent> path) {
            outerType.computeAnnotations(retention, targetInfo, list, path);
            path.addLast(TypeAnnotation.TypePathComponent.INNER_TYPE);
            super.computeAnnotations(retention, targetInfo, list, path);
            path.removeLast();
            return list;
        }
    }

    /**
     * A generic type corresponding to a primitive type, including {@code void}.
     */
    public static final class OfPrimitive extends GenericType {
        private static final Map<ClassDesc, OfPrimitive> baseItems = Stream.of(
                CD_boolean,
                CD_byte,
                CD_short,
                CD_char,
                CD_int,
                CD_long,
                CD_float,
                CD_double,
                CD_void).collect(Collectors.toUnmodifiableMap(Function.identity(), OfPrimitive::new));

        private final ClassDesc type;

        private OfPrimitive(final ClassDesc type) {
            super(List.of(), List.of());
            this.type = type;
        }

        private OfPrimitive(OfPrimitive orig, final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
            this.type = orig.type;
        }

        OfPrimitive copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfPrimitive(this, visible, invisible);
        }

        public OfPrimitive withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfPrimitive) super.withAnnotations(builder);
        }

        public <A extends java.lang.annotation.Annotation> OfPrimitive withAnnotation(final Class<A> annotationType) {
            return (OfPrimitive) super.withAnnotation(annotationType);
        }

        public <A extends java.lang.annotation.Annotation> OfPrimitive withAnnotation(final Class<A> annotationType,
                final Consumer<AnnotationCreator<A>> builder) {
            return (OfPrimitive) super.withAnnotation(annotationType, builder);
        }

        public boolean isRaw() {
            return true;
        }

        public boolean equals(final GenericType other) {
            return other instanceof OfPrimitive op && equals(op);
        }

        /**
         * {@return {@code true} if this object is equal to the given object, or {@code false} if it is not}
         */
        public boolean equals(final OfPrimitive other) {
            return this == other || super.equals(other) && type.equals(other.type);
        }

        public int hashCode() {
            return super.hashCode() * 19 + type.hashCode();
        }

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(Util.binaryName(type));
        }

        public ClassDesc desc() {
            return type;
        }
    }

    private static IllegalArgumentException noWildcards() {
        return new IllegalArgumentException("Wildcard types cannot be used here (see `TypeArgument.of(AnnotatedType)`)");
    }
}
