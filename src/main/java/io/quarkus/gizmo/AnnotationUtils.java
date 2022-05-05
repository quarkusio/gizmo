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
        } else if (value instanceof AnnotationCreator) {
            if (!(value instanceof AnnotationCreatorImpl)) {
                throw new IllegalArgumentException("Custom implementations of AnnotationCreator are not accepted");
            }
            AnnotationCreatorImpl nested = (AnnotationCreatorImpl) value;
            String descriptor = DescriptorUtils.objectToDescriptor(nested.getAnnotationType());
            AnnotationVisitor nestedVisitor = visitor.visitAnnotation(key, descriptor);
            for (Map.Entry<String, Object> member : nested.getValues().entrySet()) {
                visitAnnotationValue(nestedVisitor, member.getKey(), member.getValue());
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
