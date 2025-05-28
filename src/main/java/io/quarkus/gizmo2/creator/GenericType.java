package io.quarkus.gizmo2.creator;

import static io.smallrye.common.constraint.Assert.checkNotNullParam;
import static io.smallrye.common.constraint.Assert.impossibleSwitchCase;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.Optional;

import io.quarkus.gizmo2.impl.Util;

/**
 * Used to express types when building a generic signature of some declaration.
 * <p>
 * Implementations are created via factory methods such as {@link #intType()} or {@link #classType(Class)}.
 *
 * @see ClassSignatureCreator
 */
public sealed interface GenericType {
    /**
     * {@return an erasure of this generic type}
     */
    ClassDesc erasure();

    /**
     * Appends the string representation of this generic type to the given {@code builder}
     * and returns the {@code builder}.
     *
     * @param builder the {@link StringBuilder} to append the string representation of this type to
     * @return the given {@code builder}
     */
    StringBuilder toString(StringBuilder builder);

    // ---

    static GenericType of(ClassDesc desc) {
        if (desc.isPrimitive()) {
            return switch (desc.descriptorString()) {
                case "Z" -> booleanType();
                case "B" -> byteType();
                case "S" -> shortType();
                case "I" -> intType();
                case "J" -> longType();
                case "F" -> floatType();
                case "D" -> doubleType();
                case "C" -> charType();
                case "V" -> voidType();
                default -> throw impossibleSwitchCase(desc.descriptorString());
            };
        } else if (desc.isArray()) {
            ClassDesc elementType = desc;
            int dimensions = 0;
            while (elementType.isArray()) {
                elementType = elementType.componentType();
                dimensions++;
            }
            return arrayType(of(elementType), dimensions);
        } else {
            return classType(desc);
        }
    }

    static GenericType of(Class<?> clazz) {
        return of(Util.classDesc(clazz));
    }

    /**
     * {@return the {@code void} pseudo-type}
     */
    static PrimitiveType voidType() {
        return PrimitiveType.VOID;
    }

    /**
     * {@return the {@code boolean} primitive type}
     */
    static PrimitiveType booleanType() {
        return PrimitiveType.BOOLEAN;
    }

    /**
     * {@return the {@code byte} primitive type}
     */
    static PrimitiveType byteType() {
        return PrimitiveType.BYTE;
    }

    /**
     * {@return the {@code short} primitive type}
     */
    static PrimitiveType shortType() {
        return PrimitiveType.SHORT;
    }

    /**
     * {@return the {@code int} primitive type}
     */
    static PrimitiveType intType() {
        return PrimitiveType.INT;
    }

    /**
     * {@return the {@code long} primitive type}
     */
    static PrimitiveType longType() {
        return PrimitiveType.LONG;
    }

    /**
     * {@return the {@code float} primitive type}
     */
    static PrimitiveType floatType() {
        return PrimitiveType.FLOAT;
    }

    /**
     * {@return the {@code double} primitive type}
     */
    static PrimitiveType doubleType() {
        return PrimitiveType.DOUBLE;
    }

    /**
     * {@return the {@code char} primitive type}
     */
    static PrimitiveType charType() {
        return PrimitiveType.CHAR;
    }

    /**
     * {@return the class type for the given {@code desc}}
     *
     * @param desc descriptor of the class
     */
    static ClassType classType(ClassDesc desc) {
        return new ClassType(desc, Optional.empty());
    }

    /**
     * {@return the class type for the given {@code clazz}}
     *
     * @param clazz the class
     */
    static ClassType classType(Class<?> clazz) {
        return classType(Util.classDesc(clazz));
    }

    /**
     * {@return the parameterized type for the given {@code genericClass} and {@code typeArguments}}
     * In other words, the result of an application of the given type arguments to the generic class.
     *
     * @param genericClass the generic class
     * @param typeArguments the type arguments
     */
    static ParameterizedType parameterizedType(Class<?> genericClass, ReferenceType... typeArguments) {
        if (genericClass.getTypeParameters().length == 0) {
            throw new IllegalArgumentException("No type parameters declared by " + genericClass);
        }
        return parameterizedType(classType(genericClass), typeArguments);
    }

    /**
     * {@return the parameterized type for the given {@code genericClass} and {@code typeArguments}}
     * In other words, the result of an application of the given type arguments to the generic class.
     *
     * @param genericClass the type of the generic class
     * @param typeArguments the type arguments
     */
    static ParameterizedType parameterizedType(ClassType genericClass, ReferenceType... typeArguments) {
        return new ParameterizedType(genericClass, List.of(typeArguments), Optional.empty());
    }

    /**
     * {@return single-dimensional array type for the given {@code elementType}}
     *
     * @param elementType the element type
     */
    static ArrayType arrayType(GenericType elementType) {
        return new ArrayType(elementType, 1);
    }

    /**
     * {@return multi-dimensional array type for the given {@code elementType} and the given number of {@code dimensions}}
     *
     * @param elementType the element type
     * @param dimensions number of dimensions
     */
    static ArrayType arrayType(GenericType elementType, int dimensions) {
        return new ArrayType(elementType, dimensions);
    }

    /**
     * {@return a type variable with given {@code name}}
     *
     * @param name the type variable name
     */
    static TypeVariable typeVariable(String name) {
        return typeVariable(name, ClassType.OBJECT);
    }

    static TypeVariable typeVariable(String name, ReferenceType classOrTypeVariableBound) {
        return new TypeVariable(name, Optional.of(classOrTypeVariableBound), List.of());
    }

    // `classBound` may be `null` if all bounds are interfaces
    static TypeVariable typeVariable(String name, ReferenceType classBound, ReferenceType... interfaceBounds) {
        return new TypeVariable(name, Optional.ofNullable(classBound), List.of(interfaceBounds));
    }

    /**
     * {@return wildcard type with the given {@code upperBound}}
     *
     * @param upperBound the upper bound
     */
    static WildcardType wildcardTypeWithUpperBound(Class<?> upperBound) {
        return wildcardTypeWithUpperBound(classType(upperBound));
    }

    /**
     * {@return wildcard type with the given {@code upperBound}}
     *
     * @param upperBound the upper bound
     */
    static WildcardType wildcardTypeWithUpperBound(ReferenceType upperBound) {
        return new WildcardType(Optional.of(upperBound), Optional.empty());
    }

    /**
     * {@return wildcard type with the given {@code lowerBound}}
     *
     * @param lowerBound the lower bound
     */
    static WildcardType wildcardTypeWithLowerBound(Class<?> lowerBound) {
        return GenericType.wildcardTypeWithLowerBound(classType(lowerBound));
    }

    /**
     * {@return wildcard type with the given {@code lowerBound}}
     *
     * @param lowerBound the lower bound
     */
    static WildcardType wildcardTypeWithLowerBound(ReferenceType lowerBound) {
        return new WildcardType(Optional.empty(), Optional.of(lowerBound));
    }

    /**
     * {@return unbounded wildcard type}
     */
    static WildcardType wildcardTypeUnbounded() {
        return new WildcardType(Optional.empty(), Optional.empty());
    }

    // ---

    enum PrimitiveType implements GenericType {
        BOOLEAN(CD_boolean),
        BYTE(CD_byte),
        SHORT(CD_short),
        INT(CD_int),
        LONG(CD_long),
        FLOAT(CD_float),
        DOUBLE(CD_double),
        CHAR(CD_char),
        VOID(CD_void),
        ;

        private final ClassDesc desc;

        PrimitiveType(ClassDesc desc) {
            this.desc = desc;
        }

        public ClassDesc desc() {
            return desc;
        }

        @Override
        public ClassDesc erasure() {
            return desc;
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            return builder.append(desc.displayName());
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    sealed interface ReferenceType extends GenericType {
    }

    /**
     * An array type with given number of {@code dimensions} and given {@code elementType}.
     *
     * @param elementType the element type of the array
     * @param dimensions the number of dimensions
     */
    record ArrayType(GenericType elementType, int dimensions) implements ReferenceType {
        public ArrayType {
            checkNotNullParam("elementType", elementType);

            if (dimensions < 1) {
                throw new IllegalArgumentException("Number of array dimensions must be at least 1");
            }
            if (dimensions > 255) {
                throw new IllegalArgumentException("Number of array dimensions must be at most 255");
            }
        }

        @Override
        public ClassDesc erasure() {
            return elementType.erasure().arrayType(dimensions);
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            elementType.toString(builder);
            for (int i = 0; i < dimensions; i++) {
                builder.append("[]");
            }
            return builder;
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    /**
     * A class type of given {@code desc}.
     * The {@code owner}, if present, is either {@code ClassType} or {@code ParameterizedType}.
     *
     * @param desc the class descriptor
     * @param owner the owner type, if this class type is nested
     */
    // `owner` is either `ClassType` or `ParameterizedType`
    record ClassType(ClassDesc desc, Optional<ReferenceType> owner) implements ReferenceType {
        public static final ClassType OBJECT = new ClassType(CD_Object, Optional.empty());

        public ClassType {
            checkNotNullParam("desc", desc);
            checkNotNullParam("owner", owner);

            if (!desc.isClassOrInterface()) {
                throw new IllegalArgumentException("Given type is not a class: " + desc.displayName());
            }
        }

        /**
         * Allows building a class type of {@code com.example.Outer.Inner}. This is usually
         * unnecessary, because class type of {@code com.example.Outer$Inner} is fine too,
         * but it's occasionally useful.
         *
         * @param simpleName simple name of the member class nested in this class
         * @return the inner class type
         */
        public ClassType innerClass(String simpleName) {
            checkNotNullParam("simpleName", simpleName);
            return new ClassType(ClassDesc.of(simpleName), Optional.of(this));
        }

        /**
         * Allows building a parameterized type of {@code com.example.Outer.Inner<T>}. This is usually
         * unnecessary, because parameterized type of {@code com.example.Outer$Inner<T>} is fine too,
         * but it's occasionally useful.
         *
         * @param simpleName simple name of the generic member class nested in this class
         * @param typeArguments the type arguments
         * @return the inner parameterized type
         */
        public ParameterizedType innerParameterizedType(String simpleName, ReferenceType... typeArguments) {
            checkNotNullParam("simpleName", simpleName);
            return new ParameterizedType(new ClassType(ClassDesc.of(simpleName), Optional.empty()),
                    List.of(typeArguments), Optional.of(this));
        }

        @Override
        public ClassDesc erasure() {
            if (owner.isPresent()) {
                return owner.get().erasure().nested(desc.displayName());
            }
            return desc;
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            if (owner.isPresent()) {
                owner.get().toString(builder);
                return builder.append('.').append(desc.displayName());
            }
            String pkg = desc.packageName();
            if (!pkg.isEmpty()) {
                builder.append(pkg).append('.');
            }
            return builder.append(desc.displayName());
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    /**
     * A parameterized type, or an application of {@code typeArguments} to the {@code genericClass}.
     * The {@code owner}, if present, is either {@code ClassType} or {@code ParameterizedType}.
     *
     * @param genericClass the generic class
     * @param typeArguments the type arguments
     * @param owner the owner type, if this parameterized type is nested
     */
    record ParameterizedType(ClassType genericClass, List<ReferenceType> typeArguments,
            Optional<ReferenceType> owner) implements ReferenceType {
        public ParameterizedType {
            checkNotNullParam("genericClass", genericClass);
            checkNotNullParam("typeArguments", typeArguments);

            if (typeArguments.isEmpty()) {
                throw new IllegalArgumentException("No type arguments given");
            }
        }

        /**
         * Allows building a class type of {@code com.example.Outer<T>.Inner}.
         *
         * @param simpleName simple name of the member class nested in this parameterized type
         * @return the inner class type
         */
        public ClassType innerClass(String simpleName) {
            checkNotNullParam("simpleName", simpleName);
            return new ClassType(ClassDesc.of(simpleName), Optional.of(this));
        }

        /**
         * Allows building a parameterized type of {@code com.example.Outer<T>.Inner<U>}.
         *
         * @param simpleName simple name of the generic member class nested in this parameterized type
         * @param typeArguments the type arguments
         * @return the inner parameterized type
         */
        public ParameterizedType innerParameterizedType(String simpleName, ReferenceType... typeArguments) {
            checkNotNullParam("simpleName", simpleName);
            return new ParameterizedType(new ClassType(ClassDesc.of(simpleName), Optional.empty()),
                    List.of(typeArguments), Optional.of(this));
        }

        @Override
        public ClassDesc erasure() {
            if (owner.isPresent()) {
                return owner.get().erasure().nested(genericClass.desc().displayName());
            }
            return genericClass.erasure();
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            if (owner.isPresent()) {
                owner.get().toString(builder);
                builder.append('.');
            }
            genericClass.toString(builder);
            builder.append('<');
            for (ReferenceType typeArgument : typeArguments) {
                typeArgument.toString(builder);
                builder.append(", ");
            }
            builder.delete(builder.length() - 2, builder.length());
            builder.append('>');
            return builder;
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    /**
     * A type variable with given {@code name} and optional bounds.
     * The bounds are only used when the type variable is used to introduce a type parameter;
     * they are ignored otherwise.
     * The first bound may be a {@code TypeVariable} or a class type ({@code ClassType} or {@code ParameterizedType}).
     * If the first bound is a {@code TypeVariable}, there are no next bounds.
     * Otherwise, the next bounds must be all interface types ({@code ClassType} or {@code ParameterizedType}).
     * The first bound may be empty if all bounds are interface types.
     *
     * @param name the name of the type variable
     * @param firstBound the first bound
     * @param nextBounds the next bounds
     */
    // maybe split this into `TypeVariable` and `TypeParameter`?
    record TypeVariable(String name, Optional<ReferenceType> firstBound,
            List<ReferenceType> nextBounds) implements ReferenceType {
        public TypeVariable {
            checkNotNullParam("name", name);
            checkNotNullParam("firstBound", firstBound);
            checkNotNullParam("nextBounds", nextBounds);

            if (firstBound.isPresent()) {
                ReferenceType bound = firstBound.get();
                if (bound instanceof ClassType || bound instanceof ParameterizedType) {
                    for (GenericType interfaceBound : nextBounds) {
                        if (!(interfaceBound instanceof ClassType) && !(interfaceBound instanceof ParameterizedType)) {
                            throw new IllegalArgumentException("Next type variable bounds must all be interfaces");
                        }
                    }
                } else if (bound instanceof TypeVariable) {
                    if (!nextBounds.isEmpty()) {
                        throw new IllegalArgumentException("Next bounds must be empty when the first bound is a type variable");
                    }
                } else {
                    throw new IllegalArgumentException("Type variable bound must be a class or a type variable");
                }
            }
        }

        @Override
        public ClassDesc erasure() {
            return firstBound.map(ReferenceType::erasure)
                    .orElseGet(() -> nextBounds.stream().map(ReferenceType::erasure).findFirst().orElse(CD_Object));
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            return builder.append(name);
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }

    /**
     * A wildcard type, optionally with an upper or lower bound.
     * Both bounds are empty if the wildcard is unbounded.
     * Otherwise, exactly one bound is present and the other is empty.
     *
     * @param upperBound the upper bound
     * @param lowerBound the lower bound
     */
    record WildcardType(Optional<ReferenceType> upperBound, Optional<ReferenceType> lowerBound) implements ReferenceType {
        public WildcardType {
            checkNotNullParam("upperBound", upperBound);
            checkNotNullParam("lowerBound", lowerBound);

            if (upperBound.isPresent() && lowerBound.isPresent()) {
                throw new IllegalArgumentException("Wildcard must have at most one bound");
            }
        }

        @Override
        public ClassDesc erasure() {
            return upperBound.map(ReferenceType::erasure).orElse(CD_Object);
        }

        @Override
        public StringBuilder toString(StringBuilder builder) {
            builder.append('?');
            if (upperBound.isPresent()) {
                builder.append(" extends ");
                upperBound.get().toString(builder);
            } else if (lowerBound.isPresent()) {
                builder.append(" super ");
                lowerBound.get().toString(builder);
            }
            return builder;
        }

        @Override
        public String toString() {
            return toString(new StringBuilder()).toString();
        }
    }
}
