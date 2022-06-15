/*
 * Copyright 2018 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.quarkus.gizmo;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;

//TODO: should not be public
public class DescriptorUtils {

    private static final Class<?>[] PRIMITIVES = {
            byte.class,
            boolean.class,
            char.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            void.class
    };

    public static String methodTypesToSignature(Object returnType, Object[] params) {
        StringBuilder sb = new StringBuilder("(");
        for (Object i : params) {
            sb.append(objectToSignature(i));
        }
        sb.append(")");
        sb.append(objectToSignature(returnType));
        return sb.toString();
    }

    public static String methodSignatureToDescriptor(String returnType, String... params) {
        StringBuilder sb = new StringBuilder("(");
        for (String i : params) {
            sb.append(i);
        }
        sb.append(")");
        sb.append(returnType);
        return sb.toString();
    }

    /**
     * e.g. Ljava/lang/Object; to java/lang/Object
     */
    public static String getTypeStringFromDescriptorFormat(String descriptor) {
        if(descriptor.startsWith("[")) {
            return descriptor;
        }
        descriptor = descriptor.substring(1);
        descriptor = descriptor.substring(0, descriptor.length() - 1);
        return descriptor;
    }

    public static String classToStringRepresentation(Class<?> c) {
        if (void.class.equals(c)) {
            return "V";
        } else if (byte.class.equals(c)) {
            return "B";
        } else if (char.class.equals(c)) {
            return "C";
        } else if (double.class.equals(c)) {
            return "D";
        } else if (float.class.equals(c)) {
            return "F";
        } else if (int.class.equals(c)) {
            return "I";
        } else if (long.class.equals(c)) {
            return "J";
        } else if (short.class.equals(c)) {
            return "S";
        } else if (boolean.class.equals(c)) {
            return "Z";
        } else if (c.isArray()) {
            return normalizeClassName(c.getName());
        } else {
            return extToInt(c.getName());
        }
    }

    public static String parameterizedClassToStringRepresentation(ParameterizedClass c) {
        StringBuilder ret = new StringBuilder();
        ret.append("L");
        Object wrapperType = c.getType();
        if (wrapperType instanceof String) {
            ret.append(normalizeClassName((String) wrapperType));
        } else if (wrapperType instanceof Class) {
            ret.append(normalizeClassName(((Class) wrapperType).getName()));
        } else {
            throw new IllegalArgumentException("Wrapper type in the Parameterized class must be a Class, ParameterizedClass "
                    + "or String, got " + wrapperType);
        }

        if (c.getParameterTypes().length > 0) {
            ret.append("<");
            for (Object paramType : c.getParameterTypes()) {
                ret.append(objectToDescriptor(paramType));
            }
            ret.append(">");
        }
        ret.append(";");
        return ret.toString();
    }

    public static String extToInt(String className) {
        return 'L' + normalizeClassName(className) + ';';
    }

    public static boolean isPrimitive(String descriptor) {
        if (descriptor.length() == 1) {
            return true;
        }
        return false;
    }

    public static boolean isWide(String descriptor) {
        if (!isPrimitive(descriptor)) {
            return false;
        }
        char c = descriptor.charAt(0);
        if (c == 'D' || c == 'J') {
            return true;
        }
        return false;
    }

    /**
     * Coerces an object into a descriptor in the JVM internal format.
     * <p>
     * It accepts class and String parameters. If the parameter is a string it accepts:
     * - Standard JVM class names
     * - Internal Descriptors
     * - Primitive names as expressed in java (e.g. 'int')
     *
     * @param param The param
     * @return A descriptor
     */
    public static String objectToDescriptor(Object param) {
        if (param instanceof String) {
            String s = (String) param;
            if (s.length() == 1) {
                return s; //primitive
            }
            if (s.startsWith("[")) {
                return normalizeClassName(s);
            }
            if (s.endsWith(";")) {
                //jvm internal name
                return s;
            }
            for (Class<?> prim : PRIMITIVES) {
                if (s.equals(prim.getName())) {
                    return classToStringRepresentation(prim);
                }
            }
            return "L" + normalizeClassName(s) + ';';
        } else if (param instanceof Class) {
            return classToStringRepresentation((Class<?>) param);
        } else if (param instanceof ParameterizedClass) {
            return objectToDescriptor(((ParameterizedClass) param).getType());
        }
        throw new IllegalArgumentException("Must be a Class, ParameterizedClass or String, got " + param);
    }

    /**
     * Array version of {@link #objectToDescriptor(Object)}
     *
     * @param param
     * @return
     */
    public static String[] objectsToDescriptor(Object[] param) {
        String[] ret = new String[param.length];
        for (int i = 0; i < param.length; ++i) {
            ret[i] = objectToDescriptor(param[i]);
        }
        return ret;
    }

    /**
     * Similar to the {@link #objectToDescriptor(Object)} method but supporting parameterized types.
     *
     * @param param The param
     * @return A descriptor
     */
    public static String objectToSignature(Object param) {
        if (param instanceof ParameterizedClass) {
            return parameterizedClassToStringRepresentation((ParameterizedClass) param);
        } else if (param instanceof Type) {
            return typeToString((Type) param);
        }

        return objectToDescriptor(param);
    }

    public static String objectToInternalClassName(Object param) {
        if (param instanceof String) {
            return normalizeClassName((String) param);
        } else if (param instanceof Class) {
            return normalizeClassName(((Class) param).getName());
        }
        throw new IllegalArgumentException("Must be a Class or String, got " + param);
    }

    public static String typeToString(Type type) {
        if (type.kind() == Type.Kind.PRIMITIVE) {
            PrimitiveType.Primitive primitive = type.asPrimitiveType().primitive();
            switch (primitive) {
                case INT:
                    return "I";
                case BYTE:
                    return "B";
                case CHAR:
                    return "C";
                case LONG:
                    return "J";
                case FLOAT:
                    return "F";
                case SHORT:
                    return "S";
                case DOUBLE:
                    return "D";
                case BOOLEAN:
                    return "Z";
                default:
                    throw new RuntimeException("Unkown primitive type " + primitive);
            }
        } else if (type.kind() == Type.Kind.VOID) {
            return "V";
        } else if (type.kind() == Type.Kind.ARRAY) {
            ArrayType array = type.asArrayType();
            return normalizeClassName(array.name().toString());
        } else if (type.kind() == Type.Kind.PARAMETERIZED_TYPE) {
            ParameterizedType pt = type.asParameterizedType();
            StringBuilder ret = new StringBuilder();
            ret.append("L");
            ret.append(normalizeClassName(pt.name().toString()));
            ret.append(";");
            return ret.toString();
        } else if (type.kind() == Type.Kind.CLASS) {
            ClassType pt = type.asClassType();
            StringBuilder ret = new StringBuilder();
            ret.append("L");
            ret.append(normalizeClassName(pt.name().toString()));
            ret.append(";");
            return ret.toString();
        } else if (type.kind() == Type.Kind.TYPE_VARIABLE) {
            TypeVariable pt = type.asTypeVariable();
            StringBuilder ret = new StringBuilder();
            ret.append("L");
            ret.append(normalizeClassName(pt.name().toString()));
            ret.append(";");
            return ret.toString();
        } else {
            throw new RuntimeException("Invalid type for descriptor " + type);
        }
    }

    public static String normalizeClassName(String className) {
        return className.replace('.', '/');
    }
}