package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.annotation.RetentionPolicy;
import java.lang.constant.ClassDesc;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.quarkus.gizmo2.impl.TypeAnnotatableCreatorImpl;
import io.quarkus.gizmo2.impl.Util;
import io.smallrye.common.constraint.Assert;

/**
 * A generic Java type, which may include type arguments as well as type annotations.
 * <p>
 * This class models types for the purpose of code generation, and is not generally suitable
 * for usage in any kind of type-checking system.
 */
public abstract class GenericType {
    /*
     *
     * JavaTypeSignature
     * +- ReferenceTypeSignature
     * | +- ThrowsSignature
     * | | +- ClassTypeSignature :: [PackageSpec] {Identifier [TypeArguments] [{Nested}]}
     * | | +- TypeVariableSignature :: Identifier
     * | +- ArrayTypeSignature :: JavaTypeSignature
     * +- BaseType (primitive)
     *
     * TypeArguments
     * +- [WildCard == +/-] ReferenceTypeSignature
     * +- `*`
     *
     * Annotations:
     *
     * - Type parameters (class or method) (top level or method info)
     * - Supertype (class or interface) (top level)
     * - Type parameter bound type (class or method) (top level or method info)
     * - Type of field (or record component) (field info, record component info)
     * - Method return type or ctor output type (OK weird) (method info)
     * - Receiver type (method or ctor) (method info)
     * - Throws clause type (method into)
     *
     * - Local variable `@xyz Foo foo;` (Code attr)
     * - Resource variable `try (@xyz Foo foo = bar()) {}` (Code attr)
     * - Catch parameter `catch (Foo | @xyz Bar e)` (special target) (Code attr)
     * - Instanceof `instanceof @xyz Foo` (bytecode offset) (Code attr)
     * - Type of `new @xyz Foo()` expr (bytecode offset) (Code attr)
     * - Type of `(@xyz Abc)::new` (bytecode offset) (Code attr)
     * - Type of `(@xyz Abc)::identifier` (bytecode offset) (Code attr)
     * - Cast type (Code attr)
     * - Generic type argument for `new` invocation e.g. `new <@xyz Foo> Bar()`
     * - Generic type argument for method invocation `<@xyz Foo> bar()`
     * - Generic type argument for `::<@xyz Foo>new`
     * - Generic type argument for `::<@xyz Foo>identifier`
     *
     *
     * GenericType
     * +- OfReference
     * | +- OfThrows
     * | | +- OfClass :: enclosing (outer) OfThrows
     * | | +- OfTypeVariable
     * | +- OfArray
     * +- OfPrimitive
     */

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
        return of(type, List.of());
    }

    public static GenericType of(Class<?> type, List<TypeArgument> typeArguments) {
        int tpCount = type.getTypeParameters().length;
        int taSize = typeArguments.size();
        if (tpCount != taSize && taSize != 0) {
            throw new IllegalArgumentException("Invalid number of type arguments (expected %d but got %d)"
                    .formatted(Integer.valueOf(tpCount), Integer.valueOf(taSize)));
        }
        if (type.isMemberClass()) {
            Class<?> enclosingClass = type.getEnclosingClass();
            if (Modifier.isStatic(type.getModifiers())) {
                return of(Util.classDesc(type), typeArguments);
            } else {
                return ofInnerClass((OfClass) of(enclosingClass), type.getSimpleName()).withArguments(typeArguments);
            }
        } else {
            return of(Util.classDesc(type), typeArguments);
        }
    }

    /**
     * {@return the given type as an erased generic type (not {@code null})}
     *
     * @param desc the type descriptor
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

    public static GenericType of(ClassDesc desc, List<TypeArgument> typeArguments) {
        GenericType genericType = of(desc);
        if (typeArguments.isEmpty()) {
            return genericType;
        } else if (genericType instanceof OfRootClass oc) {
            return oc.withArguments(typeArguments);
        } else {
            throw new IllegalArgumentException("Type %s cannot have type arguments".formatted(desc));
        }
    }

    public static OfTypeVariable ofTypeVariable(TypeVariable typeVariable) {
        OfTypeVariable type = typeVariable.genericType;
        if (type == null) {
            type = typeVariable.genericType = new OfTypeVariable(List.of(), List.of(), typeVariable);
        }
        return type;
    }

    public static OfInnerClass ofInnerClass(OfClass outerClass, String name) {
        return new OfInnerClass(List.of(), List.of(),
                Assert.checkNotNullParam("outerClass", outerClass),
                Assert.checkNotNullParam("name", name),
                List.of());
    }

    public static GenericType of(Type type) {
        if (type instanceof Class<?> c) {
            return of(c);
        } else if (type instanceof GenericArrayType gat) {
            return of(gat);
        } else if (type instanceof ParameterizedType pt) {
            return of(pt);
        } else if (type instanceof java.lang.reflect.TypeVariable<?> tv) {
            return of(tv);
        } else {
            throw new IllegalArgumentException("Invalid type " + type.getClass());
        }
    }

    public static OfArray of(GenericArrayType type) {
        return of(type.getGenericComponentType()).arrayType();
    }

    public static OfTypeVariable of(java.lang.reflect.TypeVariable<?> type) {
        return TypeVariable.of(type).genericType();
    }

    public static OfClass of(ParameterizedType type) {
        return ((OfClass) of(type.getRawType())).withArguments(
                Stream.of(type.getActualTypeArguments()).map(TypeArgument::of).toList());
    }

    public static GenericType of(AnnotatedType type) {
        if (type instanceof AnnotatedArrayType aat) {
            return of(aat);
        } else if (type instanceof AnnotatedParameterizedType apt) {
            return of(apt);
        } else if (type instanceof AnnotatedTypeVariable atv) {
            return of(atv);
        } else if (type instanceof AnnotatedWildcardType) {
            throw new IllegalArgumentException("Wildcard types cannot be used here (see `TypeArgument.of(AnnotatedType)`)");
        } else {
            // annotated plain type
            return of(type.getType()).withAnnotations(AnnotatableCreator.from(type));
        }
    }

    public static OfArray of(AnnotatedArrayType type) {
        return of(type.getAnnotatedGenericComponentType()).arrayType().withAnnotations(AnnotatableCreator.from(type));
    }

    public static GenericType of(AnnotatedParameterizedType type) {
        List<TypeArgument> typeArgs = Stream.of(type.getAnnotatedActualTypeArguments()).map(TypeArgument::of).toList();
        ParameterizedType pt = (ParameterizedType) type.getType();
        return of((Class<?>) pt.getRawType(), typeArgs).withAnnotations(AnnotatableCreator.from(type));
    }

    public static OfTypeVariable of(AnnotatedTypeVariable type) {
        TypeVariable tv = TypeVariable.of((java.lang.reflect.TypeVariable<?>) type.getType());
        return tv.genericType().withAnnotations(AnnotatableCreator.from(type));
    }

    abstract GenericType copy(List<Annotation> visible, List<Annotation> invisible);

    public GenericType withAnnotations(Consumer<AnnotatableCreator> builder) {
        TypeAnnotatableCreatorImpl tac = new TypeAnnotatableCreatorImpl(visible, invisible);
        builder.accept(tac);
        if (visible.equals(tac.visible()) && invisible.equals(tac.invisible())) {
            // no annotations added
            return this;
        }
        return copy(tac.visible(), tac.invisible());
    }

    public OfArray arrayType() {
        OfArray arrayType = this.arrayType;
        if (arrayType == null) {
            arrayType = this.arrayType = new OfArray(List.of(), List.of(), this);
        }
        return arrayType;
    }

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

    public final boolean equals(final Object obj) {
        return obj instanceof GenericType gt && equals(gt);
    }

    public boolean equals(GenericType gt) {
        return this == gt || visible.equals(gt.visible) && invisible.equals(gt.invisible);
    }

    public int hashCode() {
        return visible.hashCode() * 19 + invisible.hashCode();
    }

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

    public static final class OfTypeVariable extends OfThrows {
        private final TypeVariable typeVariable;

        OfTypeVariable(final List<Annotation> visible, final List<Annotation> invisible, final TypeVariable typeVariable) {
            super(visible, invisible);
            this.typeVariable = typeVariable;
        }

        public ClassDesc desc() {
            return typeVariable.erasure();
        }

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(typeVariable);
        }

        public TypeVariable typeVariable() {
            return typeVariable;
        }

        public OfTypeVariable withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfTypeVariable) super.withAnnotations(builder);
        }

        OfTypeVariable copy(final List<Annotation> visible, final List<Annotation> invisible) {
            return new OfTypeVariable(visible, invisible, typeVariable);
        }

        public boolean equals(final GenericType gt) {
            return gt instanceof OfTypeVariable tv && equals(tv);
        }

        public boolean equals(final OfTypeVariable tvt) {
            return this == tvt || super.equals(tvt) && typeVariable().equals(tvt.typeVariable());
        }

        public int hashCode() {
            return super.hashCode() * 19 + typeVariable().hashCode();
        }
    }

    public static abstract class OfReference extends GenericType {
        TypeArgument.OfExact exactArg;
        TypeArgument.OfExtends extendsArg;
        TypeArgument.OfSuper superArg;

        OfReference(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }
    }

    public static abstract class OfThrows extends OfReference {
        OfThrows(final List<Annotation> visible, final List<Annotation> invisible) {
            super(visible, invisible);
        }
    }

    public static final class OfArray extends OfReference {
        private final GenericType componentType;
        private ClassDesc desc;

        OfArray(final List<Annotation> visible, final List<Annotation> invisible, final GenericType componentType) {
            super(visible, invisible);
            this.componentType = componentType;
        }

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
    }

    /**
     * A generic type of a class or interface.
     * There is no separate type for non-{@code class} object types.
     */
    public static abstract class OfClass extends OfThrows {
        final List<TypeArgument> typeArguments;

        OfClass(final List<Annotation> visible, final List<Annotation> invisible, final List<TypeArgument> typeArguments) {
            super(visible, invisible);
            this.typeArguments = typeArguments;
        }

        public abstract ClassDesc desc();

        public List<TypeArgument> typeArguments() {
            return typeArguments;
        }

        public OfClass withAnnotations(final Consumer<AnnotatableCreator> builder) {
            return (OfClass) super.withAnnotations(builder);
        }

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
                        .formatted(Integer.valueOf(naSize), Integer.valueOf(taSize)));
            }
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
                if (arg instanceof TypeArgument.OfAnnotated ann) {
                    List<Annotation> argAnnotations = switch (retention) {
                        case RUNTIME -> ann.visible();
                        case CLASS -> ann.invisible();
                        default -> throw Assert.impossibleSwitchCase(retention);
                    };
                    pathSnapshot = List.copyOf(path);
                    for (Annotation annotation : argAnnotations) {
                        list.add(TypeAnnotation.of(targetInfo, pathSnapshot, annotation));
                    }
                    if (ann instanceof TypeArgument.OfBounded bnd) {
                        // extends or super; add the inner annotations
                        path.addLast(TypeAnnotation.TypePathComponent.WILDCARD);
                        bnd.bound().computeAnnotations(retention, targetInfo, list, path);
                        path.removeLast();
                    }
                } else if (arg instanceof TypeArgument.OfExact exact) {
                    // exact
                    exact.bound().computeAnnotations(retention, targetInfo, list, path);
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
                    b.append(',');
                    iter.next().toString(b);
                }
                b.append('>');
            }
            return b;
        }
    }

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

        public StringBuilder toString(final StringBuilder b) {
            return typeArgumentsToString(super.toString(b).append(Util.binaryName(desc)));
        }
    }

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

        public StringBuilder toString(final StringBuilder b) {
            return super.toString(b).append(Util.binaryName(type));
        }

        public ClassDesc desc() {
            return type;
        }
    }
}
