package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class SignatureUtils {
    public static void visitType(SignatureVisitor visitor, Type type) {
        if (type.kind() == Type.Kind.PRIMITIVE) {
            PrimitiveType.Primitive primitive = type.asPrimitiveType().primitive();
            switch (primitive) {
                case INT:
                    visitor.visitBaseType('I');
                    break;
                case BYTE:
                    visitor.visitBaseType('B');
                    break;
                case CHAR:
                    visitor.visitBaseType('C');
                    break;
                case LONG:
                    visitor.visitBaseType('J');
                    break;
                case FLOAT:
                    visitor.visitBaseType('F');
                    break;
                case SHORT:
                    visitor.visitBaseType('S');
                    break;
                case DOUBLE:
                    visitor.visitBaseType('D');
                    break;
                case BOOLEAN:
                    visitor.visitBaseType('Z');
                    break;
                default:
                    throw new RuntimeException("Unkown primitive type " + primitive);
            }
        } else if (type.kind() == Type.Kind.VOID) {
            visitor.visitBaseType('V');
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            visitor.visitArrayType();
            visitType(visitor, array.component());
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType pt = type.asParameterizedType();
            visitor.visitTypeVariable(pt.name().toString().replace('.', '/'));
        } else if (type.kind() == Type.Kind.CLASS) {
            ClassType pt = type.asClassType();
            visitor.visitClassType(pt.name().toString().replace('.', '/'));
            visitor.visitEnd();
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable pt = type.asTypeVariable();
            visitor.visitTypeVariable(pt.name().toString().replace('.', '/'));
        } else {
            throw new RuntimeException("Invalid type for descriptor " + type);
        }
    }

    /**
     *  generate signature for method with this format
     *  ( {@code visitFormalTypeParameter} {@code visitClassBound}? {@code visitInterfaceBound}* )*
     *  ({@code visitParameterType}* {@code visitReturnType} {@code visitExceptionType}* )
     */
    public static class MethodSignature {

        private Map<String, FormalType> formalTypeParameters;
        private List<String> paramTypes;
        private Type returnType;
        private List<String> exceptionTypes;

        MethodSignature() {
            formalTypeParameters = new HashMap<>();
            paramTypes = new ArrayList<>();
            exceptionTypes = new ArrayList<>();
        }

        public SignatureUtils.MethodSignature exceptionTypes(List<String> exceptionTypes) {
            this.exceptionTypes.addAll(exceptionTypes);
            return this;
        }

        public SignatureUtils.MethodSignature formalType(String name) {
            return formalType(name, Object.class.getName());
        }

        public SignatureUtils.MethodSignature formalType(String name, String superClass, String... interfaces) {
            this.formalTypeParameters.put(name, new FormalType(name, superClass, interfaces));
            return this;
        }

        public SignatureUtils.MethodSignature paramTypes(String... names) {
            this.paramTypes.addAll(Arrays.asList(names));
            return this;
        }

        public SignatureUtils.MethodSignature returnType(Type returnType) {
            this.returnType = returnType;
            return this;
        }

        public String generate() {
            Objects.requireNonNull(returnType);
            SignatureWriter signature = new SignatureWriter();
            //( {@code visitFormalTypeParameter} {@code visitClassBound}? {@code visitInterfaceBound}* )*
            for (String formalTypeParameterName : formalTypeParameters.keySet()) {
                FormalType formalTypeParameter = formalTypeParameters.get(formalTypeParameterName);
                signature.visitFormalTypeParameter(formalTypeParameterName);
                // Ensure that <K> extends object
                if (formalTypeParameter.getSuperClass() != null && !formalTypeParameter.getSuperClass().isEmpty()){
                    SignatureVisitor classBound = signature.visitClassBound();
                    classBound.visitClassType(formalTypeParameter.getSuperClass());
                    classBound.visitEnd();

                }
                // Ensure that <K> implements interfaces
                for (String formalTypeParameterInterface : formalTypeParameter.getInterfaces()) {
                    SignatureVisitor interfaceBound = signature.visitInterfaceBound();
                    interfaceBound.visitClassType(formalTypeParameterInterface);
                    interfaceBound.visitEnd();
                }
            }
            if (!paramTypes.isEmpty()) {
                SignatureVisitor paramTypeVisitor = signature.visitParameterType();
                for (String paramType : paramTypes) {
                    paramTypeVisitor.visitTypeVariable(paramType);
                }
            }
            visitType(signature.visitReturnType(), returnType);
            if (!exceptionTypes.isEmpty()) {
                SignatureVisitor exceptionVisitor = signature.visitExceptionType();
                for (String exceptionType : exceptionTypes) {
                    exceptionVisitor.visitClassType(exceptionType);
                }
            }
            signature.visitEnd();
            return signature.toString();
        }
    }

    /**
     *  generate signature for class with this format
     *  ( {@code visitFormalTypeParameter} {@code visitClassBound}? {@code visitInterfaceBound}* )*
     *  ({@code visitSuperclass} {@code visitInterface}* )
     */
    public static class ClassSignature {

        private Map<String, FormalType> formalTypeParameters;
        private String superClass;
        private List<String> interfaces;

        ClassSignature() {
            formalTypeParameters = new HashMap<>();
            interfaces = new ArrayList<>();
        }

        public SignatureUtils.ClassSignature formalType(String name) {
            return formalType(name, Object.class.getName());
        }

        public SignatureUtils.ClassSignature formalType(String name, String superClass, String... interfaces) {
            formalTypeParameters.put(name, new FormalType(name, superClass, interfaces));
            return this;
        }

        public SignatureUtils.ClassSignature interfaces(List<String> interfaces) {
            this.interfaces.addAll(interfaces);
            return this;
        }
        public SignatureUtils.ClassSignature superClass(String superClass) {
            this.superClass = superClass;
            return this;
        }

        public String generate() {
            Objects.requireNonNull(superClass);
            SignatureWriter signature = new SignatureWriter();
            //( {@code visitFormalTypeParameter} {@code visitClassBound}? {@code visitInterfaceBound}* )*
            for (String formalTypeParameterName : formalTypeParameters.keySet()) {
                FormalType formalTypeParameter = formalTypeParameters.get(formalTypeParameterName);
                signature.visitFormalTypeParameter(formalTypeParameterName);
                // Ensure that <K> extends object
                if (formalTypeParameter.getSuperClass() != null && !formalTypeParameter.getSuperClass().isEmpty()){
                    SignatureVisitor classBound = signature.visitClassBound();
                    classBound.visitClassType(formalTypeParameter.getSuperClass());
                    classBound.visitEnd();

                }
                // Ensure that <K> implements interfaces
                for (String formalTypeParameterInterface : formalTypeParameter.getInterfaces()) {
                    SignatureVisitor interfaceBound = signature.visitInterfaceBound();
                    interfaceBound.visitClassType(formalTypeParameterInterface);
                    interfaceBound.visitEnd();
                }
            }

            //({@code visitSuperclass} {@code visitInterface}* )
            {
                SignatureVisitor superclassVisitor = signature.visitSuperclass();
                superclassVisitor.visitClassType(superClass);
                superclassVisitor.visitEnd();
            }

            if (!interfaces.isEmpty()) {
                SignatureVisitor interfaceVisitor = signature.visitInterface();
                for (String interfaceType : interfaces) {
                    interfaceVisitor.visitClassType(interfaceType);
                    interfaceVisitor.visitEnd();
                }
            }
            signature.visitEnd();
            return signature.toString();
        }
    }


    private static class InnerClassType {
        private final String name;
        private final List<Character> typeArguments;

        public InnerClassType(String name, List<Character> typeArguments) {
            this.name = name;
            this.typeArguments = typeArguments;
        }

        public String getName() {
            return name;
        }

        public List<Character> getTypeArguments() {
            return typeArguments;
        }
    }
    /**
     *  generate signature for type with this format
     *  {@code visitBaseType} | {@code visitTypeVariable} | {@code visitArrayType}
     *  | ( {@code visitClassType} {@code visitTypeArgument}*
     *      ( {@code visitInnerClassType} {@code visitTypeArgument}* )* {@code visitEnd} ) )
     */
    public static class TypeSignature {

        private Type type;
        private List<Character> typeArguments;
        private List<InnerClassType> innerClassTypes;


        TypeSignature() {
        }
        public void Type(Type type) {
            this.type = type;
            this.typeArguments = new ArrayList<>();
            this.innerClassTypes = new ArrayList<>();
        }
        public void typeArguments(Character... typeArguments) {
            this.typeArguments.addAll(Arrays.asList(typeArguments));
        }
        public void innerClassType(String innerClassTypeName, List<Character> typeArguments) {
            this.innerClassTypes.add(new InnerClassType(innerClassTypeName, typeArguments));
        }

        public String generate() {

            SignatureWriter signature = new SignatureWriter();
            visitType(signature, type);
            if (type.kind() == Type.Kind.CLASS) {
                // ( {@code visitInnerClassType} {@code visitTypeArgument}* )*
                for (Character c : typeArguments) {
                    signature.visitTypeArgument(c);
                }
                for (InnerClassType cls : innerClassTypes) {
                    signature.visitInnerClassType(cls.getName());
                    for (Character c : cls.getTypeArguments()) {
                        signature.visitTypeArgument(c);
                    }
                }
            }
            signature.visitEnd();
            return signature.toString();
        }
    }
}
