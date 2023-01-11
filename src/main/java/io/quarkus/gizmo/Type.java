package io.quarkus.gizmo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType.Primitive;

/**
 * Used to express types when {@linkplain SignatureBuilder building} a generic signature of some declaration.
 * <p>
 * Implementations are created via factory methods such as {@link #voidType()} and {@link #classType(Class)}.
 * 
 * @see SignatureBuilder
 */
public abstract class Type {
    // Factory methods

    public static VoidType voidType() {
        return VoidType.INSTANCE;
    }

    public static PrimitiveType booleanType() {
        return PrimitiveType.BOOLEAN;
    }

    public static PrimitiveType byteType() {
        return PrimitiveType.BYTE;
    }

    public static PrimitiveType shortType() {
        return PrimitiveType.SHORT;
    }

    public static PrimitiveType intType() {
        return PrimitiveType.INT;
    }

    public static PrimitiveType longType() {
        return PrimitiveType.LONG;
    }

    public static PrimitiveType floatType() {
        return PrimitiveType.FLOAT;
    }

    public static PrimitiveType doubleType() {
        return PrimitiveType.DOUBLE;
    }

    public static PrimitiveType charType() {
        return PrimitiveType.CHAR;
    }

    public static ClassType classType(DotName className) {
        return classType(className.toString());
    }

    public static ClassType classType(String className) {
        return new ClassType(className.replace('.', '/'), null);
    }

    public static ClassType classType(Class<?> clazz) {
        return classType(clazz.getName());
    }

    public static ParameterizedType parameterizedType(ClassType genericClass, Type... typeArguments) {
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException("No type arguments");
        }
        return new ParameterizedType(genericClass, Arrays.asList(typeArguments), null);
    }

    public static ArrayType arrayType(Type elementType) {
        return new ArrayType(elementType, 1);
    }

    public static ArrayType arrayType(Type elementType, int dimensions) {
        return new ArrayType(elementType, dimensions);
    }

    public static TypeVariable typeVariable(String name) {
        return typeVariable(name, ClassType.OBJECT);
    }

    public static TypeVariable typeVariable(String name, Type classOrTypeVariableBound) {
        Type bound = Objects.requireNonNull(classOrTypeVariableBound);
        if (!bound.isClass() && !bound.isParameterizedType() && !bound.isTypeVariable()) {
            throw new IllegalArgumentException("Type variable bound must be a class or a type variable");
        }
        return new TypeVariable(name, bound, Collections.emptyList());
    }

    public static TypeVariable typeVariable(String name, Type classBound, Type... interfaceBounds) {
        if (classBound != null && !classBound.isClass() && !classBound.isParameterizedType()) {
            throw new IllegalArgumentException("First type variable bound must be a class");
        }
        for (Type interfaceBound : interfaceBounds) {
            if (!interfaceBound.isClass() && !interfaceBound.isParameterizedType()) {
                throw new IllegalArgumentException("Next type variable bounds must all be interfaces");
            }
        }

        return new TypeVariable(name, classBound, Arrays.asList(interfaceBounds));
    }

    public static WildcardType wildcardTypeWithUpperBound(Type bound) {
        return new WildcardType(Objects.requireNonNull(bound), null);
    }

    public static WildcardType wildcardTypeWithLowerBound(Type bound) {
        return new WildcardType(null, Objects.requireNonNull(bound));
    }

    public static WildcardType wildcardTypeUnbounded() {
        return new WildcardType(ClassType.OBJECT, null);
    }

    // implementation details

    abstract void appendToSignature(StringBuilder signature);

    boolean isVoid() {
        return false;
    }

    boolean isPrimitive() {
        return false;
    }

    boolean isClass() {
        return false;
    }

    boolean isArray() {
        return false;
    }

    boolean isParameterizedType() {
        return false;
    }

    boolean isTypeVariable() {
        return false;
    }

    boolean isWildcard() {
        return false;
    }

    VoidType asVoid() {
        throw new IllegalStateException("Not a void");
    }

    PrimitiveType asPrimitive() {
        throw new IllegalStateException("Not a primitive");
    }

    ClassType asClass() {
        throw new IllegalStateException("Not a class");
    }

    ArrayType asArray() {
        throw new IllegalStateException("Not an array");
    }

    ParameterizedType asParameterizedType() {
        throw new IllegalStateException("Not a parameterized type");
    }

    TypeVariable asTypeVariable() {
        throw new IllegalStateException("Not a type variable");
    }

    WildcardType asWildcard() {
        throw new IllegalStateException("Not a wildcard type");
    }

    // Types

    public static final class VoidType extends Type {
        public static final VoidType INSTANCE = new VoidType();

        @Override
        void appendToSignature(StringBuilder signature) {
            signature.append("V");
        }

        @Override
        boolean isVoid() {
            return true;
        }

        @Override
        VoidType asVoid() {
            return this;
        }
    }

    public static final class PrimitiveType extends Type {
        public static final PrimitiveType BOOLEAN = new PrimitiveType(Primitive.BOOLEAN);
        public static final PrimitiveType BYTE = new PrimitiveType(Primitive.BYTE);
        public static final PrimitiveType SHORT = new PrimitiveType(Primitive.SHORT);
        public static final PrimitiveType INT = new PrimitiveType(Primitive.INT);
        public static final PrimitiveType LONG = new PrimitiveType(Primitive.LONG);
        public static final PrimitiveType FLOAT = new PrimitiveType(Primitive.FLOAT);
        public static final PrimitiveType DOUBLE = new PrimitiveType(Primitive.DOUBLE);
        public static final PrimitiveType CHAR = new PrimitiveType(Primitive.CHAR);

        private final Primitive primitive;

        PrimitiveType(Primitive primitive) {
            this.primitive = Objects.requireNonNull(primitive);
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            switch (primitive) {
                case BOOLEAN:
                    signature.append("Z");
                    break;
                case BYTE:
                    signature.append("B");
                    break;
                case SHORT:
                    signature.append("S");
                case INT:
                    signature.append("I");
                    break;
                case LONG:
                    signature.append("J");
                    break;
                case FLOAT:
                    signature.append("F");
                    break;
                case DOUBLE:
                    signature.append("D");
                    break;
                case CHAR:
                    signature.append("C");
                    break;
                default:
                    throw new IllegalStateException("Unknown primitive type: " + primitive.toString());
            }
        }

        @Override
        boolean isPrimitive() {
            return true;
        }

        @Override
        PrimitiveType asPrimitive() {
            return this;
        }
    }

    public static final class ClassType extends Type {
        public static final ClassType OBJECT = new ClassType("java/lang/Object", null);

        final String name; // always slash-delimited
        final Type owner;

        ClassType(String name, Type owner) {
            this.name = Objects.requireNonNull(name);
            this.owner = owner;
        }

        /**
         * Allows building a signature like {@code Lcom/example/Outer.Inner;}. This is usually
         * unnecessary, because {@code Lcom/example/Outer$Inner} is also a valid signature,
         * but it's occasionally useful to build a signature of more complex inner types.
         *
         * @param simpleName simple name of the member class nested in this class
         * @return the inner class
         */
        public ClassType innerClass(String simpleName) {
            return new ClassType(simpleName, this);
        }

        /**
         * Allows build a signature like {@code Lcom/example/Outer.Inner<TU;>;}. This is usually
         * unnecessary, because {@code Lcom/example/Outer$Inner<TU;>;} is also a valid signature,
         * but it's occasionally useful to build a signature of more complex inner types.
         *
         * @param simpleName simple name of the generic member class nested in this class
         * @return the inner parameterized type
         */
        public ParameterizedType innerParameterizedType(String simpleName, Type... typeArguments) {
            return new ParameterizedType(new ClassType(simpleName, null), Arrays.asList(typeArguments), this);
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            if (owner != null) {
                // Append the owner class and replace the last semicolon with a period
                owner.appendToSignature(signature);
                signature.setCharAt(signature.length() - 1, '.');
            } else {
                signature.append('L');
            }
            signature.append(name).append(';');
        }

        @Override
        boolean isClass() {
            return true;
        }

        @Override
        ClassType asClass() {
            return this;
        }
    }

    public static final class ArrayType extends Type {
        private final Type elementType;
        private final int dimensions;

        ArrayType(Type elementType, int dimensions) {
            this.elementType = Objects.requireNonNull(elementType);
            this.dimensions = dimensions;
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            for (int i = 0; i < dimensions; i++) {
                signature.append('[');
            }
            elementType.appendToSignature(signature);
        }

        @Override
        boolean isArray() {
            return true;
        }

        @Override
        ArrayType asArray() {
            return this;
        }
    }

    public static final class ParameterizedType extends Type {
        
        final ClassType genericClass;
        final List<Type> typeArguments;
        final Type owner;

        ParameterizedType(ClassType genericClass, List<Type> typeArguments, Type owner) {
            this.genericClass = Objects.requireNonNull(genericClass);
            this.typeArguments = Objects.requireNonNull(typeArguments);
            this.owner = owner;
        }

        /**
         * Allows build a signature like {@code Lcom/example/Outer<TT;>.Inner;}.
         *
         * @param simpleName simple name of the member class nested in this parameterized type
         * @return the inner class
         */
        public ClassType innerClass(String simpleName) {
            return new ClassType(simpleName, this);
        }

        /**
         * Allows building a signature like {@code Lcom/example/Outer<TT;>.Inner<TU;>;}.
         *
         * @param simpleName simple name of the generic member class nested in this parameterized type
         * @return the inner parameterized type
         */
        public ParameterizedType innerParameterizedType(String simpleName, Type... typeArguments) {
            return new ParameterizedType(new ClassType(simpleName, null), Arrays.asList(typeArguments), this);
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            if (owner != null) {
                // Append the owner class and replace the last semicolon with a period
                owner.appendToSignature(signature);
                signature.setCharAt(signature.length() - 1, '.');
            } else {
                signature.append('L');
            }
            signature.append(genericClass.name);
            if (!typeArguments.isEmpty()) {
                signature.append('<');
                for (Type typeArgument : typeArguments) {
                    typeArgument.appendToSignature(signature);
                }
                signature.append('>');
            }
            signature.append(';');
        }

        @Override
        boolean isParameterizedType() {
            return true;
        }

        @Override
        ParameterizedType asParameterizedType() {
            return this;
        }

        List<Type> getTypeArguments() {
            return Collections.unmodifiableList(typeArguments);
        }
    }

    public static final class TypeVariable extends Type {
        private final String name;
        private final Type firstBound; // may be null if all bounds are interfaces
        private final List<Type> nextBounds;

        TypeVariable(String name, Type firstBound, List<Type> nextBounds) {
            this.name = Objects.requireNonNull(name);
            this.firstBound = firstBound;
            this.nextBounds = Objects.requireNonNull(nextBounds);
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            signature.append('T').append(name).append(';');
        }

        void appendTypeParameterToSignature(StringBuilder signature) {
            signature.append(name).append(":");
            if (firstBound != null) {
                firstBound.appendToSignature(signature);
            }
            for (Type bound : nextBounds) {
                signature.append(":");
                bound.appendToSignature(signature);
            }
        }

        @Override
        boolean isTypeVariable() {
            return true;
        }

        @Override
        TypeVariable asTypeVariable() {
            return this;
        }
    }

    public static final class WildcardType extends Type {
        private final Type upperBound;
        private final Type lowerBound;

        WildcardType(Type upperBound, Type lowerBound) {
            if (upperBound == null && lowerBound == null) {
                throw new NullPointerException();
            }
            if (upperBound != null && lowerBound != null) {
                throw new IllegalArgumentException();
            }
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        @Override
        boolean isWildcard() {
            return true;
        }

        @Override
        WildcardType asWildcard() {
            return this;
        }

        @Override
        void appendToSignature(StringBuilder signature) {
            if (lowerBound != null) {
                signature.append('-');
                lowerBound.appendToSignature(signature);
            } else if (upperBound.isClass() && upperBound.asClass().name.equals(ClassType.OBJECT.name)) {
                signature.append('*');
            } else {
                signature.append('+');
                upperBound.appendToSignature(signature);
            }
        }
    }
}
