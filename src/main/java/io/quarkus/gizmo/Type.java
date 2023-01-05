package io.quarkus.gizmo;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType.Primitive;

/**
 * This interface can be used to build a JVM signature for classes, methods and fields.
 * <p>
 * Implementations are instantiated via factory methods; for example {@link #voidType()} and {@link #classType(Class)}.
 * 
 * @see SignatureBuilder
 */
public interface Type {

    // Factory methods

    public static VoidType voidType() {
        return VoidType.INSTANCE;
    }

    public static PrimitiveType byteType() {
        return PrimitiveType.BYTE;
    }

    public static PrimitiveType booleanType() {
        return PrimitiveType.BOOLEAN;
    }

    public static PrimitiveType intType() {
        return PrimitiveType.INT;
    }

    public static PrimitiveType longType() {
        return PrimitiveType.LONG;
    }

    public static PrimitiveType shortType() {
        return PrimitiveType.SHORT;
    }

    public static PrimitiveType doubleType() {
        return PrimitiveType.DOUBLE;
    }

    public static PrimitiveType floatType() {
        return PrimitiveType.FLOAT;
    }

    public static PrimitiveType charType() {
        return PrimitiveType.CHAR;
    }

    public static ClassType classType(DotName name) {
        return classType(Objects.requireNonNull(name).toString().replace('.', '/'));
    }

    public static ClassType classType(String name) {
        return new ClassType(name, null);
    }

    public static ClassType classType(Class<?> classType) {
        return classType(Objects.requireNonNull(classType).getName().replace('.', '/'));
    }

    public static ParameterizedType parameterizedType(ClassType classType, Type... typeArguments) {
        if (typeArguments.length == 0) {
            throw new IllegalArgumentException("No type arguments");
        }
        return new ParameterizedType(classType, Arrays.asList(typeArguments), null);
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

    public static TypeVariable typeVariable(String name, Type classBound, Type... interfaceBounds) {
        return new TypeVariable(name, classBound, Arrays.asList(interfaceBounds));
    }

    public static WildcardType wildcardTypeWithUpperBound(Type bound) {
        return new WildcardType(bound, null);
    }

    public static WildcardType wildcardTypeWithLowerBound(Type bound) {
        return new WildcardType(null, bound);
    }

    public static WildcardType wildcardTypeUnbounded() {
        return new WildcardType(ClassType.OBJECT, null);
    }

    /**
     * 
     * @param signature
     */
    void appendToSignature(StringBuilder signature);

    /**
     * 
     * @return the signature as defined in JVMS 17, chapter "4.7.9.1. Signatures"
     */
    default String toSignature() {
        StringBuilder sb = new StringBuilder();
        appendToSignature(sb);
        return sb.toString();
    }

    default boolean isVoid() {
        return false;
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean isClass() {
        return false;
    }

    default boolean isArray() {
        return false;
    }

    default boolean isParameterizedType() {
        return false;
    }

    default boolean isTypeVariable() {
        return false;
    }

    default boolean isWildcard() {
        return false;
    }

    default VoidType asVoid() {
        throw new IllegalStateException("Not a void");
    }

    default PrimitiveType asPrimitive() {
        throw new IllegalStateException("Not a primitive");
    }

    default ClassType asClass() {
        throw new IllegalStateException("Not a class");
    }

    default ArrayType asArray() {
        throw new IllegalStateException("Not an array");
    }

    default ParameterizedType asParameterizedType() {
        throw new IllegalStateException("Not a parameterized type");
    }

    default TypeVariable asTypeVariable() {
        throw new IllegalStateException("Not a type variable");
    }

    default WildcardType asWildcard() {
        throw new IllegalStateException("Not a wildcard type");
    }

    // Implementations

    public static class WildcardType implements Type {

        final Type lowerBound;
        final Type upperBound;

        WildcardType(Type upperBound, Type lowerBound) {
            if (upperBound == null && lowerBound == null) {
                throw new NullPointerException();
            }
            this.upperBound = upperBound;
            this.lowerBound = lowerBound;
        }

        @Override
        public boolean isWildcard() {
            return true;
        }

        @Override
        public WildcardType asWildcard() {
            return this;
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            if (lowerBound != null) {
                signature.append('-').append(lowerBound.toSignature());
            } else if (upperBound.isClass() && upperBound.asClass().name.equals(ClassType.OBJECT.name)) {
                signature.append('*');
            } else {
                signature.append('+').append(upperBound.toSignature());
            }
        }

    }

    public static class TypeVariable implements Type {

        final String name;
        final Type classBound;
        final List<Type> interfaceBounds;

        TypeVariable(String name, Type classBound, List<Type> interfaceBounds) {
            this.name = Objects.requireNonNull(name);
            this.classBound = classBound;
            this.interfaceBounds = interfaceBounds;
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            signature.append('T').append(name).append(';').toString();
        }

        public void appendTypeParameterToSignature(StringBuilder signature) {
            signature.append(name).append(":");
            if (classBound != null) {
                signature.append(classBound.toSignature());
            }
            for (Type bound : interfaceBounds) {
                signature.append(":").append(bound.toSignature());
            }
        }

        @Override
        public boolean isTypeVariable() {
            return true;
        }

        @Override
        public TypeVariable asTypeVariable() {
            return this;
        }

    }

    public static class ArrayType implements Type {

        final Type elementType;
        final int dimensions;

        ArrayType(Type elementType, int dimensions) {
            this.elementType = Objects.requireNonNull(elementType);
            this.dimensions = dimensions;
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            for (int i = 0; i < dimensions; i++) {
                signature.append('[');
            }
            signature.append(elementType.toSignature());
        }

        @Override
        public boolean isArray() {
            return true;
        }

        @Override
        public ArrayType asArray() {
            return this;
        }

    }

    public static class ParameterizedType implements Type {

        final ClassType classType;
        final List<Type> typeArguments;
        final Type declaringClassType;

        ParameterizedType(ClassType classType, List<Type> typeArguments, Type declaringClassType) {
            this.classType = Objects.requireNonNull(classType);
            this.typeArguments = typeArguments;
            this.declaringClassType = declaringClassType;
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            if (declaringClassType != null) {
                // Append the declaring class and remove the last semicolon
                declaringClassType.appendToSignature(signature);
                signature.deleteCharAt(signature.length() - 1);
                signature.append('.');
            } else {
                signature.append('L');
            }
            signature.append(classType.name);
            if (!typeArguments.isEmpty()) {
                signature.append('<');
                for (Type argument : typeArguments) {
                    signature.append(argument.toSignature());
                }
                signature.append('>');
            }
            signature.append(';');
        }

        @Override
        public boolean isParameterizedType() {
            return true;
        }

        @Override
        public ParameterizedType asParameterizedType() {
            return this;
        }

        /**
         * Build a signature like <code>Lorg/acme/Parent<TT;>.Inner;</code>.
         * 
         * @param simpleName
         * @return the nested class
         */
        public ClassType nestedClassType(String simpleName) {
            return new ClassType(simpleName, this);
        }

        /**
         * Build a signature like <code>Lorg/acme/Parent<TT;>.Inner<TU;>;</code>.
         * 
         * @param simpleName
         * @return the nested class
         */
        public ParameterizedType nestedParameterizedType(String simpleName, Type... typeArguments) {
            return new ParameterizedType(Type.classType(simpleName), Arrays.asList(typeArguments), this);
        }

    }

    public static class ClassType implements Type {

        public static ClassType OBJECT = classType(DotName.OBJECT_NAME);

        final String name;
        final Type declaringClassType;

        ClassType(String name, Type declaringClassType) {
            this.name = Objects.requireNonNull(name);
            this.declaringClassType = declaringClassType;
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            if (declaringClassType != null) {
                // Append the declaring class and remove the last semicolon
                declaringClassType.appendToSignature(signature);
                signature.deleteCharAt(signature.length() - 1);
                signature.append('.');
            } else {
                signature.append('L');
            }
            signature.append(name).append(';');
        }

        @Override
        public boolean isClass() {
            return true;
        }

        @Override
        public ClassType asClass() {
            return this;
        }

    }

    public static class VoidType implements Type {

        public static final VoidType INSTANCE = new VoidType();

        @Override
        public void appendToSignature(StringBuilder signature) {
            signature.append("V");
        }

        @Override
        public boolean isVoid() {
            return true;
        }

        @Override
        public VoidType asVoid() {
            return this;
        }

    }

    public static class PrimitiveType implements Type {

        public static final PrimitiveType BYTE = new PrimitiveType(Primitive.BYTE);
        public static final PrimitiveType CHAR = new PrimitiveType(Primitive.CHAR);
        public static final PrimitiveType DOUBLE = new PrimitiveType(Primitive.DOUBLE);
        public static final PrimitiveType FLOAT = new PrimitiveType(Primitive.FLOAT);
        public static final PrimitiveType INT = new PrimitiveType(Primitive.INT);
        public static final PrimitiveType LONG = new PrimitiveType(Primitive.LONG);
        public static final PrimitiveType SHORT = new PrimitiveType(Primitive.SHORT);
        public static final PrimitiveType BOOLEAN = new PrimitiveType(Primitive.BOOLEAN);

        final Primitive primitive;

        PrimitiveType(Primitive primitive) {
            this.primitive = Objects.requireNonNull(primitive);
        }

        @Override
        public void appendToSignature(StringBuilder signature) {
            signature.append(toSignature());
        }

        @Override
        public String toSignature() {
            switch (primitive) {
                case BOOLEAN:
                    return "Z";
                case BYTE:
                    return "B";
                case CHAR:
                    return "C";
                case DOUBLE:
                    return "D";
                case FLOAT:
                    return "F";
                case INT:
                    return "I";
                case LONG:
                    return "J";
                case SHORT:
                    return "S";
                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public boolean isPrimitive() {
            return true;
        }

        @Override
        public PrimitiveType asPrimitive() {
            return this;
        }

    }

}
