package io.quarkus.gizmo;

import java.util.Map;

class PrimitiveUtils {
    static final Class<?>[] PRIMITIVE_TYPES = {
            boolean.class,
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            char.class,
            void.class
    };

    static final Map<String, Class<?>> WRAPPER_CLASS_BY_PRIMITIVE_KEYWORD = Map.of(
        "boolean", Boolean.class,
        "byte", Byte.class,
        "char", Character.class,
        "short", Short.class,
        "int", Integer.class,
        "long", Long.class,
        "float", Float.class,
        "double", Double.class,
        "void", Void.class);

    // ---
    // the following code doesn't consider `void` a primitive type

    static class Boxing {
        private final org.objectweb.asm.Type wrapperClass;
        private final String unboxingMethodName;

        private Boxing(Class<?> wrapperClass, String unboxingMethodName) {
            this.wrapperClass = org.objectweb.asm.Type.getType(wrapperClass);
            this.unboxingMethodName = unboxingMethodName;
        }

        String wrapperDescriptor() {
            return wrapperClass.getDescriptor();
        }

        String wrapperInternalName() {
            return wrapperClass.getInternalName();
        }

        String unboxingMethod() {
            return unboxingMethodName;
        }

        String boxingMethod() {
            return "valueOf";
        }
    }

    private static final Map<String, Class<?>> PRIMITIVE_TYPE_BY_DESCRIPTOR = Map.of(
            "Z", boolean.class,
            "B", byte.class,
            "S", short.class,
            "I", int.class,
            "J", long.class,
            "F", float.class,
            "D", double.class,
            "C", char.class);

    private static final Map<String, Class<?>> PRIMITIVE_TYPE_BY_WRAPPER_DESCRIPTOR = Map.of(
            "Ljava/lang/Boolean;", boolean.class,
            "Ljava/lang/Byte;", byte.class,
            "Ljava/lang/Short;", short.class,
            "Ljava/lang/Integer;", int.class,
            "Ljava/lang/Long;", long.class,
            "Ljava/lang/Float;", float.class,
            "Ljava/lang/Double;", double.class,
            "Ljava/lang/Character;", char.class);

    private static final Map<String, Boxing> BOXING_CONVERSION_BY_PRIMITIVE_DESCRIPTOR = Map.of(
            "Z", new Boxing(Boolean.class, "booleanValue"),
            "B", new Boxing(Byte.class, "byteValue"),
            "S", new Boxing(Short.class, "shortValue"),
            "I", new Boxing(Integer.class, "intValue"),
            "J", new Boxing(Long.class, "longValue"),
            "F", new Boxing(Float.class, "floatValue"),
            "D", new Boxing(Double.class, "doubleValue"),
            "C", new Boxing(Character.class, "charValue"));

    static boolean isPrimitiveDescriptor(String descriptor) {
        return PRIMITIVE_TYPE_BY_DESCRIPTOR.containsKey(descriptor);
    }

    static boolean isWrapperDescriptor(String descriptor) {
        return PRIMITIVE_TYPE_BY_WRAPPER_DESCRIPTOR.containsKey(descriptor);
    }

    static boolean isPrimitiveOrWrapperDescriptor(String descriptor) {
        return PRIMITIVE_TYPE_BY_DESCRIPTOR.containsKey(descriptor)
               || PRIMITIVE_TYPE_BY_WRAPPER_DESCRIPTOR.containsKey(descriptor);
    }

    static Class<?> primitiveTypeFromDescriptor(String descriptor) {
        return PRIMITIVE_TYPE_BY_DESCRIPTOR.get(descriptor);
    }

    static Class<?> primitiveTypeFromWrapperDescriptor(String descriptor) {
        return PRIMITIVE_TYPE_BY_WRAPPER_DESCRIPTOR.get(descriptor);
    }

    static Boxing boxingConversion(String descriptor) {
        return BOXING_CONVERSION_BY_PRIMITIVE_DESCRIPTOR.get(descriptor);
    }
}
