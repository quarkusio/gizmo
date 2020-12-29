package io.quarkus.gizmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

public class SignatureUtils {

    // Map<java.util.List<java.lang.String>, Function<K, boolean>>
    // format accepted is not jvm format... so need refactor to use java code format
    // and translate to signature
    public static int visitType(SignatureVisitor visitor, String signature, int offset) {
        int start = offset;
        char currentChar = signature.charAt(start);
        while (true) {
            currentChar = signature.charAt(offset++);
            switch(currentChar) {
                case '?': {
                    offset = skipBlank(signature, offset);
                    String nextKeyword = signature.substring(offset);
                    if (nextKeyword.startsWith("super")) {
                        offset = skipBlank(signature, offset + 5);
                        return visitType(visitor.visitTypeArgument('-'), signature, offset);
                    } else if (nextKeyword.startsWith("extends")) {
                        offset = skipBlank(signature, offset + 7);
                        return visitType(visitor.visitTypeArgument('+'), signature, offset);
                    } else {
                        visitType(visitor.visitTypeArgument('='), "java/lang/Object", offset);
                        return offset + 1;
                    }
                }
                case '<': {
                    String name = signature.substring(start, offset - 1);
                    visitor.visitClassType(name.replace('.', '/'));
                    while (currentChar != '>') {
                        offset = visitType(visitor.visitTypeArgument('='), signature, offset);
                        offset = skipBlank(signature, offset);
                        currentChar = signature.charAt(offset);
                        if (currentChar == ',') {
                            offset++;
                            offset = skipBlank(signature, offset);
                        }
                        currentChar = signature.charAt(offset);
                    }
                    visitor.visitEnd();
                    return offset;
                }
                case '[': {
                    //TODO multiple dim array
                    SignatureVisitor arrayVisitor = visitor.visitArrayType();
                    visitType(arrayVisitor, signature.substring(start, offset - 1), start);
                    while (currentChar != ']') {
                        offset = skipBlank(signature, offset);
                        currentChar = signature.charAt(offset++);
                    }
                    return offset;
                }
            }
            if (currentChar == ' ' || currentChar == '>' || currentChar == ',' || offset >= signature.length()) {
                String name;
                if (currentChar == '>' || currentChar == ',' || currentChar == ' ') {
                    name = signature.substring(start, offset - 1);
                } else {
                    name = signature.substring(start);
                }
                if (name.contains(".")) {
                    visitor.visitClassType(name.replace('.', '/'));
                    visitor.visitEnd();
                } else if (name.equals("void")) {
                    visitor.visitBaseType('V');
                } else if (name.equals("byte")) {
                    visitor.visitBaseType('B');
                } else if (name.equals("char")) {
                    visitor.visitBaseType('C');
                } else if (name.equals("double")) {
                    visitor.visitBaseType('D');
                } else if (name.equals("float")) {
                    visitor.visitBaseType('F');
                } else if (name.equals("int")) {
                    visitor.visitBaseType('I');
                } else if (name.equals("long")) {
                    visitor.visitBaseType('J');
                } else if (name.equals("short")) {
                    visitor.visitBaseType('S');
                } else if (name.equals("boolean")) {
                    visitor.visitBaseType('Z');
                } else if (name.length() == 1) {
                    visitor.visitTypeVariable(name);
                } else {
                    throw new IllegalArgumentException("Unknown type:" + name);
                }
                if (currentChar ==  '>') {
                    return offset - 1;
                }
                return offset;
            }
        }
    }
    public static int visitJVMType(SignatureVisitor visitor, String signature, int offset) {
        int start = offset;
        char currentChar = signature.charAt(start);
        switch(currentChar) {
            case 'B':
            case 'C':
            case 'D':
            case 'F':
            case 'I':
            case 'J':
            case 'S':
            case 'V':
            case 'Z':
                visitor.visitBaseType(currentChar);
                return offset;
            case 'E':
            case 'G':
            case 'H':
            case 'K':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'U':
            case 'W':
            case 'X':
            case 'Y':
            default:
               //TODO formaltype
            case 'L':
                while (true) {
                    currentChar = signature.charAt(offset++);
                    if (currentChar == '?') { //wildcard
                        offset = skipBlank(signature, offset);
                        String nextKeyword = signature.substring(offset);
                        if (nextKeyword.startsWith("super")) {
                            offset = skipBlank(signature, offset + 5);
                            return visitJVMType(visitor.visitTypeArgument('-'), signature, offset);
                        } else if (nextKeyword.startsWith("extends")) {
                            offset = skipBlank(signature, offset + 7);
                            return visitJVMType(visitor.visitTypeArgument('+'), signature, offset);
                        } else {
                            visitJVMType(visitor.visitTypeArgument('='), "java/lang/Object", offset);
                            return offset + 1;
                        }
                    } else if (currentChar == ';') { //jvm format
                        String name = signature.substring(start, offset - 1);
                        name = name.trim();
                        if (name.startsWith("[")) {
                            String remaining = name.substring(1);
                            visitor.visitArrayType();
                            while (remaining.startsWith("[")) {
                                visitor.visitArrayType();
                                remaining = name.substring(1);
                            }
                            visitJVMType(visitor, remaining, 0);
                        }
                        name = name.substring(1);
                        visitor.visitClassType(name);
                        visitor.visitEnd();
                        return offset;
                    } else if (currentChar == '<') {
                        String name = signature.substring(start, offset - 1);
                        visitor.visitClassType(name);
                        while (currentChar != '>') {
                            offset = visitJVMType(visitor.visitTypeArgument('='), signature, offset + 1);
                            currentChar = signature.charAt(offset);
                        }
                        visitor.visitEnd();
                        return offset;
                    }
                }
            case 'T':
                int endOffset = signature.indexOf(';', offset);
                visitor.visitTypeVariable(signature.substring(offset, endOffset));
                return endOffset + 1;
            case '[':
                return visitJVMType(visitor.visitArrayType(), signature, offset);
        }
    }

    private static int skipBlank(String signature, int offset) {
        while (signature.charAt(offset) == ' ') {
            offset++;
        }
        return offset;
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
            return formalType(name, "java.lang.Object");
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
                    visitType(classBound, formalTypeParameter.getSuperClass(), 0);
                }
                // Ensure that <K> implements interfaces
                for (String formalTypeParameterInterface : formalTypeParameter.getInterfaces()) {
                    SignatureVisitor interfaceBound = signature.visitInterfaceBound();
                    visitType(interfaceBound, formalTypeParameterInterface, 0);
                }
            }
            if (!paramTypes.isEmpty()) {
                SignatureVisitor paramTypeVisitor = signature.visitParameterType();
                for (String paramType : paramTypes) {
                    if (formalTypeParameters.keySet().contains(paramType)) {
                        paramTypeVisitor.visitTypeVariable(paramType);
                    } else {
                        //todo handle type variable for params
                        visitType(paramTypeVisitor, paramType, 0);
                    }
                }
            }
            visitType(signature.visitReturnType(), returnType, 0);
            if (!exceptionTypes.isEmpty()) {
                SignatureVisitor exceptionVisitor = signature.visitExceptionType();
                for (String exceptionType : exceptionTypes) {
                    //exception can not contain generics
                    visitType(exceptionVisitor, exceptionType, 0);
                }
            }
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
            this.superClass = "java.lang.Object";
        }

        public SignatureUtils.ClassSignature formalType(String name) {
            return formalType(name, "java.lang.Object");
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
                    visitType(classBound, formalTypeParameter.getSuperClass(), 0);
                }
                // Ensure that <K> implements interfaces
                for (String formalTypeParameterInterface : formalTypeParameter.getInterfaces()) {
                    SignatureVisitor interfaceBound = signature.visitInterfaceBound();
                    visitType(interfaceBound, formalTypeParameterInterface, 0);
                }
            }

            //({@code visitSuperclass} {@code visitInterface}* )
            {
                SignatureVisitor superclassVisitor = signature.visitSuperclass();
                visitType(superclassVisitor, superClass, 0);
            }

            if (!interfaces.isEmpty()) {
                for (String interfaceType : interfaces) {
                    SignatureVisitor interfaceVisitor = signature.visitInterface();
                    visitType(interfaceVisitor, interfaceType, 0);
                }
            }
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
            return formalType(name, "java.lang.Object");
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
            visitType(signature, type, 0);
            if (type.startsWith("L")) {
                for (InnerClassType cls : innerClassTypes) {
                    signature.visitInnerClassType(cls.getName());
                    if (cls.getGenericParameters() != null && !cls.getGenericParameters().isEmpty()) {
                        for (String typeArg : cls.getGenericParameters().split(",")) {
                            typeArg = typeArg.trim();
                            if (typeArg.startsWith("?")) {
                                visitType(signature, typeArg, 0);
                            } else {
                                SignatureVisitor argVisitor = signature.visitTypeArgument('=');
                                visitType(argVisitor, typeArg, 0);
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