package io.quarkus.gizmo2.jandex;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.ArrayList;
import java.util.List;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;

import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

/**
 * Bridge methods from {@code org.jboss.jandex} types to the Gizmo API.
 */
public class Jandex2Gizmo {
    /**
     * {@return the {@link ClassDesc} corresponding to the given Jandex {@link DotName}}
     * See {@link Type#name()} for the description of the format this method recognizes.
     *
     * @param name the Jandex {@code DotName} (must not be {@code null})
     */
    public static ClassDesc classDescOf(DotName name) {
        if (name.prefix() == null) {
            String local = name.local();
            return switch (local) {
                case "void" -> ConstantDescs.CD_void;
                case "boolean" -> ConstantDescs.CD_boolean;
                case "byte" -> ConstantDescs.CD_byte;
                case "short" -> ConstantDescs.CD_short;
                case "int" -> ConstantDescs.CD_int;
                case "long" -> ConstantDescs.CD_long;
                case "float" -> ConstantDescs.CD_float;
                case "double" -> ConstantDescs.CD_double;
                case "char" -> ConstantDescs.CD_char;
                default -> ofClassOrArray(local);
            };
        }
        return ofClassOrArray(name.toString());
    }

    private static ClassDesc ofClassOrArray(String name) {
        int dimensions = 0;
        while (name.charAt(dimensions) == '[') {
            dimensions++;
        }
        if (dimensions == 0) {
            // `name` must be a binary name of a class
            return ClassDesc.of(name);
        }

        ClassDesc elementType = name.charAt(dimensions) == 'L'
                // class type, need to skip `L` at the beginning and `;` at the end
                ? ClassDesc.of(name.substring(dimensions + 1, name.length() - 1))
                // primitive type, the rest of `name` is just the primitive descriptor
                : ClassDesc.ofDescriptor(name.substring(dimensions));
        return elementType.arrayType(dimensions);
    }

    /**
     * {@return the {@link ClassDesc} corresponding to the erasure of given Jandex {@link Type}}
     *
     * @param type the Jandex type (must not be {@code null})
     */
    public static ClassDesc classDescOf(Type type) {
        return switch (type.kind()) {
            case VOID -> ConstantDescs.CD_void;
            case PRIMITIVE -> switch (type.asPrimitiveType().primitive()) {
                case BOOLEAN -> ConstantDescs.CD_boolean;
                case BYTE -> ConstantDescs.CD_byte;
                case SHORT -> ConstantDescs.CD_short;
                case INT -> ConstantDescs.CD_int;
                case LONG -> ConstantDescs.CD_long;
                case FLOAT -> ConstantDescs.CD_float;
                case DOUBLE -> ConstantDescs.CD_double;
                case CHAR -> ConstantDescs.CD_char;
            };
            case ARRAY -> {
                ArrayType arrayType = type.asArrayType();
                ClassDesc element = classDescOf(arrayType.elementType());
                yield element.arrayType(arrayType.deepDimensions());
            }
            default -> classDescOf(type.name());
        };
    }

    /**
     * {@return the {@link ClassDesc} corresponding to the given Jandex {@link ClassInfo}}
     *
     * @param clazz the Jandex class (must not be {@code null})
     */
    public static ClassDesc classDescOf(ClassInfo clazz) {
        return classDescOf(clazz.name());
    }

    /**
     * {@return the {@link FieldDesc} corresponding to the given Jandex {@link FieldInfo}}
     *
     * @param field the Jandex field (must not be {@code null})
     */
    public static FieldDesc fieldDescOf(FieldInfo field) {
        return FieldDesc.of(classDescOf(field.declaringClass()), field.name(), classDescOf(field.type()));
    }

    /**
     * {@return the {@link MethodDesc} corresponding to the given Jandex {@link MethodInfo}}
     *
     * @param method the Jandex method (must not be {@code null})
     * @throws IllegalArgumentException if the {@code method} is a static initializer or constructor
     */
    public static MethodDesc methodDescOf(MethodInfo method) {
        if (method.isConstructor()) {
            throw new IllegalArgumentException("Cannot create MethodDesc for constructor: " + method);
        }
        if (method.isStaticInitializer()) {
            throw new IllegalArgumentException("Cannot create MethodDesc for static initializer: " + method);
        }

        ClassDesc owner = classDescOf(method.declaringClass());
        ClassDesc returnType = classDescOf(method.returnType());
        ClassDesc[] paramTypes = new ClassDesc[method.parametersCount()];
        for (int i = 0; i < method.parametersCount(); i++) {
            paramTypes[i] = classDescOf(method.parameterType(i));
        }
        MethodTypeDesc methodTypeDesc = MethodTypeDesc.of(returnType, paramTypes);
        return method.declaringClass().isInterface()
                ? InterfaceMethodDesc.of(owner, method.name(), methodTypeDesc)
                : ClassMethodDesc.of(owner, method.name(), methodTypeDesc);
    }

    /**
     * {@return the {@link ConstructorDesc} corresponding to the given Jandex {@link MethodInfo}}
     *
     * @param ctor the Jandex constructor (must not be {@code null})
     * @throws IllegalArgumentException if the {@code ctor} is not a constructor
     */
    public static ConstructorDesc constructorDescOf(MethodInfo ctor) {
        if (ctor.isStaticInitializer()) {
            throw new IllegalArgumentException("Cannot create ConstructorDesc for static initializer: " + ctor);
        }
        if (!ctor.isConstructor()) {
            throw new IllegalArgumentException("Cannot create ConstructorDesc for regular method: " + ctor);
        }

        List<ClassDesc> paramTypes = new ArrayList<>(ctor.parametersCount());
        for (int i = 0; i < ctor.parametersCount(); i++) {
            paramTypes.add(classDescOf(ctor.parameterType(i)));
        }
        return ConstructorDesc.of(classDescOf(ctor.declaringClass()), paramTypes);
    }
}
