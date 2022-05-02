package io.quarkus.gizmo;

import java.lang.reflect.Array;
import java.util.Map;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.objectweb.asm.AnnotationVisitor;

final class AnnotationUtils {

    static void visitAnnotationValue(AnnotationVisitor visitor, String key, Object value) {
        if (value.getClass().isArray()) {
            AnnotationVisitor arrayVisitor = visitor.visitArray(key);
            int length = Array.getLength(value);
            for (int i = 0; i < length; i++) {
                // Default key is 'value'. It can be changed by using AnnotationValue type
                visitAnnotationValue(arrayVisitor, "value", Array.get(value, i));
            }
            arrayVisitor.visitEnd();
        } else if (value instanceof AnnotationInstance) {
            AnnotationInstance annotationInstance = (AnnotationInstance) value;
            String descriptor = DescriptorUtils.objectToDescriptor(annotationInstance.name().toString());
            AnnotationVisitor nestedVisitor = visitor.visitAnnotation(key, descriptor);
            for (AnnotationValue annotationValue : annotationInstance.values()) {
                visitAnnotationValue(nestedVisitor, annotationValue.name(), annotationValue);
            }
            nestedVisitor.visitEnd();
        } else if (value instanceof Map) {
            Map<String, Object> annotationValueMap = (Map<String, Object>) value;
            if (!annotationValueMap.containsKey("annotationType")) {
                throw new IllegalStateException("The annotationValueMap (" + annotationValueMap + ") does not have entry for " +
                        "required value \"annotationType\".");
            }
            String descriptor = DescriptorUtils.objectToDescriptor(annotationValueMap.get("annotationType"));
            AnnotationVisitor nestedVisitor = visitor.visitAnnotation(key, descriptor);
            for (Map.Entry<String, Object> annotationInstanceValueEntry : annotationValueMap.entrySet()) {
                final String parameterName = annotationInstanceValueEntry.getKey();
                final Object parameterValue = annotationInstanceValueEntry.getValue();

                if (parameterName.equals("annotationType")) {
                    continue;
                }

                visitAnnotationValue(nestedVisitor, parameterName, parameterValue);
            }
            nestedVisitor.visitEnd();
        } else if (value instanceof AnnotationValue) {
            AnnotationValue annotationValue = (AnnotationValue) value;
            if (annotationValue.kind() == AnnotationValue.Kind.NESTED) {
                visitAnnotationValue(visitor, annotationValue.name(), annotationValue.asNested());
            } else if (annotationValue.kind() == AnnotationValue.Kind.CLASS) {
                String descriptor = DescriptorUtils.typeToString(annotationValue.asClass());
                visitor.visit(annotationValue.name(), org.objectweb.asm.Type.getType(descriptor));
            } else if (annotationValue.kind() == AnnotationValue.Kind.ENUM) {
                String descriptor = DescriptorUtils.objectToDescriptor(annotationValue.asEnumType().toString());
                visitor.visitEnum(key, descriptor, annotationValue.asEnum());
            } else if (annotationValue.kind() == AnnotationValue.Kind.ARRAY) {
                visitAnnotationValue(visitor, annotationValue.name(), annotationValue.value());
            } else {
                visitor.visit(annotationValue.name(), annotationValue.value());
            }
        } else if (value instanceof Enum) {
            visitor.visitEnum(key, DescriptorUtils.objectToDescriptor(value.getClass()), ((Enum) value).name());
        } else if (value instanceof Class) {
            String descriptor = DescriptorUtils.objectToDescriptor(value);
            visitor.visit(key, org.objectweb.asm.Type.getType(descriptor));
        } else {
            visitor.visit(key, value);
        }
    }
}
