package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jboss.jandex.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class SignatureUtils {
    public static void visitType(SignatureVisitor visitor, String type, String genericParameters) {
        if (type.length() == 1) {
             visitor.visitBaseType(type.charAt(0));
        } else if (type.startsWith("[")) {
            String remaining = type.substring(1);
            visitor.visitArrayType();
            while (remaining.startsWith("[")) {
                visitor.visitArrayType();
                remaining = type.substring(1);
            }
            visitType(visitor, remaining, genericParameters);
        } else if (type.startsWith("?")) {
            String nextKeyword = type.substring(1).trim();
            SignatureVisitor wildcardVisitor;
            if (nextKeyword.startsWith("super")) {
                wildcardVisitor = visitor.visitTypeArgument('-');
                //remove super L...;
                nextKeyword = nextKeyword.substring(5, nextKeyword.indexOf(';')).trim().substring(1);
            } else if (nextKeyword.startsWith("extends")) {
                wildcardVisitor = visitor.visitTypeArgument('+');
                nextKeyword = nextKeyword.substring(7, nextKeyword.indexOf(';')).trim().substring(1);
            } else {
                wildcardVisitor = visitor.visitTypeArgument('=');
                nextKeyword = "java/lang/Object";
            }
            wildcardVisitor.visitClassType(nextKeyword);
        } else if (type.startsWith("T") && type.contains(";")) {
            String typeVar = type.substring(1, type.indexOf(';'));
            visitor.visitTypeVariable(typeVar);
        } else if (type.startsWith("L") && type.contains(";")) {
            String classType = type.substring(1, type.indexOf(';'));
            visitor.visitClassType(classType);
            //TODO embedded generics <String, K, List<?>>
            if (genericParameters != null) {
                for (String typeArg : genericParameters.split(",")) {
                    typeArg = typeArg.trim();
                    if (typeArg.startsWith("?")) {
                        visitType(visitor, typeArg, null);
                    } else {
                        SignatureVisitor argVisitor = visitor.visitTypeArgument('=');
                        visitType(argVisitor, typeArg, null);
                    }
                }
            }
            visitor.visitEnd();
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
        private String returnType;
        private String returnTypeGenericParameters;
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

        public SignatureUtils.MethodSignature returnType(String returnType) {
            this.returnType = returnType;
            return this;
        }

        public SignatureUtils.MethodSignature returnTypeGenericParamerters(String returnTypeGenericParameters) {
            this.returnTypeGenericParameters = returnTypeGenericParameters;
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
            visitType(signature.visitReturnType(), returnType, returnTypeGenericParameters);
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
        private String genericParameters;

        public InnerClassType(String name, String genericParameters) {
            this.name = name;
            this.genericParameters = genericParameters;
        }

        public String getName() {
            return name;
        }

        public String getGenericParameters() {
            return genericParameters;
        }
    }
    /**
     *  generate signature for type with this format
     *  {@code visitBaseType} | {@code visitTypeVariable} | {@code visitArrayType}
     *  | ( {@code visitClassType} {@code visitTypeArgument}*
     *      ( {@code visitInnerClassType} {@code visitTypeArgument}* )* {@code visitEnd} ) )
     */
    public static class TypeSignature {

        private String type;
        private String genericParameters;
        private Map<String, FormalType> formalTypeParameters;
        private List<InnerClassType> innerClassTypes;


        TypeSignature() {
            this.innerClassTypes = new ArrayList<>();
            formalTypeParameters = new HashMap<>();
        }
        public SignatureUtils.TypeSignature Type(String type) {
            this.type = type;
            return this;
        }
        public SignatureUtils.TypeSignature genericParameters(String genericParameters) {
            this.genericParameters = genericParameters;
            return this;
        }
        public SignatureUtils.TypeSignature formalType(String name) {
            return formalType(name, Object.class.getName());
        }

        public SignatureUtils.TypeSignature formalType(String name, String superClass, String... interfaces) {
            formalTypeParameters.put(name, new FormalType(name, superClass, interfaces));
            return this;
        }
        public SignatureUtils.TypeSignature innerClassType(String innerClassTypeName, String genericParameters) {
            //TODO support formalTypeParameter for inner class
            this.innerClassTypes.add(new InnerClassType(innerClassTypeName, genericParameters));
            return this;
        }

        public String generate() {
            SignatureWriter signature = new SignatureWriter();
            visitType(signature, type, genericParameters);
            if (type.startsWith("L")) {
                for (InnerClassType cls : innerClassTypes) {
                    signature.visitInnerClassType(cls.getName());
                    if (cls.getGenericParameters() != null && !cls.getGenericParameters().isEmpty()) {
                        for (String typeArg : cls.getGenericParameters().split(",")) {
                            typeArg = typeArg.trim();
                            if (typeArg.startsWith("?")) {
                                visitType(signature, typeArg, null);
                            } else {
                                SignatureVisitor argVisitor = signature.visitTypeArgument('=');
                                visitType(argVisitor, typeArg, null);
                            }
                        }
                    }
                }
            }
            signature.visitEnd();
            return signature.toString();
        }
    }
}
