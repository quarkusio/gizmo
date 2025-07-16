package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.CD_Object;

import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.quarkus.gizmo2.creator.AnnotatableCreator;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.AnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;

/**
 * Bridge methods from {@code java.lang.reflect} types to the Gizmo API.
 */
public final class Reflection2Gizmo {
    /**
     * {@return a {@link ClassDesc} for the given {@code clazz}}
     *
     * @param clazz the class (must not be {@code null})
     * @return the {@code ClassDesc} (not {@code null})
     */
    public static ClassDesc classDescOf(Class<?> clazz) {
        return Util.classDesc(clazz);
    }

    /**
     * {@return an erasure of the given {@code type}}
     *
     * @param type the type (must not be {@code null})
     */
    public static ClassDesc erasureOf(Type type) {
        if (type instanceof Class<?> c) {
            return Util.classDesc(c);
        } else if (type instanceof ParameterizedType pt) {
            return Util.classDesc((Class<?>) pt.getRawType());
        } else if (type instanceof TypeVariable<?> tv) {
            return tv.getBounds().length == 0 ? CD_Object : erasureOf(tv.getBounds()[0]);
        } else if (type instanceof WildcardType wt) {
            return wt.getUpperBounds().length == 0 ? CD_Object : erasureOf(wt.getUpperBounds()[0]);
        } else if (type instanceof GenericArrayType gat) {
            return erasureOf(gat.getGenericComponentType()).arrayType();
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }
    }

    /**
     * {@return the given reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type is a wildcard type or is not recognized
     */
    public static GenericType genericTypeOf(Type type) {
        if (type instanceof Class<?> c) {
            return GenericType.of(c);
        } else if (type instanceof GenericArrayType gat) {
            return genericTypeOf(gat);
        } else if (type instanceof ParameterizedType pt) {
            return genericTypeOf(pt);
        } else if (type instanceof TypeVariable<?> tv) {
            return genericTypeOf(tv);
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
    public static GenericType.OfArray genericTypeOf(GenericArrayType type) {
        return genericTypeOf(type.getGenericComponentType()).arrayType();
    }

    /**
     * {@return the given type variable reflection type as a generic type (not {@code null})}
     *
     * @param type the type variable (must not be {@code null})
     */
    public static GenericType.OfTypeVariable genericTypeOf(TypeVariable<?> type) {
        return GenericType.ofTypeVariable(type.getName(), erasureOf(type));
    }

    /**
     * {@return the given parameterized reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static GenericType.OfClass genericTypeOf(ParameterizedType type) {
        return ((GenericType.OfClass) genericTypeOf(type.getRawType()))
                .withArguments(Stream.of(type.getActualTypeArguments()).map(Reflection2Gizmo::typeArgumentOf).toList());
    }

    /**
     * {@return the given annotated reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     * @throws IllegalArgumentException if the given type is a wildcard type or is not recognized
     */
    public static GenericType genericTypeOf(AnnotatedType type) {
        if (type instanceof AnnotatedArrayType aat) {
            return genericTypeOf(aat);
        } else if (type instanceof AnnotatedParameterizedType apt) {
            return genericTypeOf(apt);
        } else if (type instanceof AnnotatedTypeVariable atv) {
            return genericTypeOf(atv);
        } else if (type instanceof AnnotatedWildcardType) {
            throw noWildcards();
        } else {
            // annotated plain type
            return genericTypeOf(type.getType()).withAnnotations(copyAnnotations(type));
        }
    }

    /**
     * {@return the given annotated array reflection type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static GenericType.OfArray genericTypeOf(AnnotatedArrayType type) {
        return genericTypeOf(type.getAnnotatedGenericComponentType())
                .arrayType()
                .withAnnotations(copyAnnotations(type));
    }

    /**
     * {@return the given annotated parameterized type as a generic type (not {@code null})}
     *
     * @param type the type (must not be {@code null})
     */
    public static GenericType genericTypeOf(AnnotatedParameterizedType type) {
        List<TypeArgument> typeArgs = Stream.of(type.getAnnotatedActualTypeArguments())
                .map(Reflection2Gizmo::typeArgumentOf)
                .toList();
        ParameterizedType pt = (ParameterizedType) type.getType();
        return GenericType.of((Class<?>) pt.getRawType(), typeArgs)
                .withAnnotations(copyAnnotations(type));
    }

    /**
     * {@return the given type variable annotated reflection type as a generic type (not {@code null})}
     *
     * @param type the type variable (must not be {@code null})
     */
    public static GenericType.OfTypeVariable genericTypeOf(AnnotatedTypeVariable type) {
        TypeVariable<?> typeVar = (TypeVariable<?>) type.getType();
        return GenericType.ofTypeVariable(typeVar.getName(), erasureOf(typeVar))
                .withAnnotations(copyAnnotations(type));
    }

    /**
     * {@return a {@linkplain TypeParameter type parameter} for the given type variable}
     *
     * @param typeVar the type variable (must not be {@code null})
     */
    public static TypeParameter typeParameterOf(final TypeVariable<?> typeVar) {
        List<GenericType.OfThrows> allBounds = Stream.of(typeVar.getAnnotatedBounds())
                .map(Reflection2Gizmo::genericTypeOf)
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
        copyAnnotations(typeVar).accept(tac);
        GenericDeclaration decl = typeVar.getGenericDeclaration();
        if (decl instanceof Class<?> c) {
            return new TypeParameter.OfType(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds,
                    Util.classDesc(c));
        } else if (decl instanceof Method m) {
            return new TypeParameter.OfMethod(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds,
                    MethodDesc.of(m));
        } else if (decl instanceof Constructor<?> c) {
            return new TypeParameter.OfConstructor(tac.visible(), List.of(), typeVar.getName(), firstBound, otherBounds,
                    ConstructorDesc.of(c));
        } else {
            // should be impossible, actually
            throw new IllegalStateException("Unexpected declaration " + decl);
        }
    }

    /**
     * {@return a type argument for the given generic reflection type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument typeArgumentOf(final Type type) {
        if (type instanceof WildcardType wt) {
            return typeArgumentOf(wt);
        }
        return TypeArgument.ofExact((GenericType.OfReference) genericTypeOf(type));
    }

    /**
     * {@return a type argument for the given reflection wildcard type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument.OfWildcard typeArgumentOf(final WildcardType type) {
        Type[] ub = type.getUpperBounds();
        Type[] lb = type.getLowerBounds();
        if (lb.length > 0) {
            return TypeArgument.ofSuper((GenericType.OfReference) genericTypeOf(lb[0]));
        } else if (ub.length == 0 || ub.length == 1 && ub[0].equals(Object.class)) {
            return TypeArgument.ofUnbounded();
        } else {
            return TypeArgument.ofExtends((GenericType.OfReference) genericTypeOf(ub[0]));
        }
    }

    /**
     * {@return a type argument for the given annotated generic reflection type}
     * If the type is an annotated wildcard type, then the type argument will be a wildcard type
     * with any annotations attached to the given type.
     * Otherwise, the result will be an exact type argument whose {@linkplain TypeArgument.OfExact#type() type}
     * will have any annotations attached to the given type.
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument typeArgumentOf(final AnnotatedType type) {
        if (type instanceof AnnotatedWildcardType wt) {
            return typeArgumentOf(wt);
        }
        return TypeArgument.ofExact((GenericType.OfReference) genericTypeOf(type));
    }

    /**
     * {@return a type argument for the given annotated reflection wildcard type}
     *
     * @param type the argument type (must not be {@code null})
     */
    public static TypeArgument.OfWildcard typeArgumentOf(final AnnotatedWildcardType type) {
        AnnotatedType[] aub = type.getAnnotatedUpperBounds();
        AnnotatedType[] alb = type.getAnnotatedLowerBounds();
        if (alb.length > 0) {
            return TypeArgument.ofSuper((GenericType.OfReference) genericTypeOf(alb[0]))
                    .withAnnotations(copyAnnotations(type));
        } else if (aub.length == 0 || aub.length == 1 && aub[0].getType().equals(Object.class)
                && aub[0].getAnnotations().length == 0) {
            return TypeArgument.ofUnbounded()
                    .withAnnotations(copyAnnotations(type));
        } else {
            return TypeArgument.ofExtends((GenericType.OfReference) genericTypeOf(aub[0]))
                    .withAnnotations(copyAnnotations(type));
        }
    }

    /**
     * Copy all of the annotations from the given annotated element.
     *
     * @param element the annotated element (must not be {@code null})
     * @return a consumer which copies the annotations (not {@code null})
     */
    public static Consumer<AnnotatableCreator> copyAnnotations(AnnotatedElement element) {
        return ac -> {
            Stream.of(element.getAnnotations())
                    .filter(a -> Set.of(Objects.requireNonNull(a.annotationType().getAnnotation(Target.class)).value())
                            .contains(((AnnotatableCreatorImpl) ac).annotationTargetType()))
                    .forEach(ac::addAnnotation);
        };
    }

    private static IllegalArgumentException noWildcards() {
        return new IllegalArgumentException("Wildcard types can only be used as type arguments (use `typeArgumentOf()`)");
    }
}
