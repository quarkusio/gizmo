package io.quarkus.gizmo;

import org.jboss.jandex.AnnotationValue;
import org.objectweb.asm.AnnotationVisitor;

final class AnnotationUtils {

    static void visitAnnotationValue(AnnotationVisitor visitor, String key, Object value) {
        if (value.getClass().isArray()) {
            AnnotationVisitor arrayVisitor = visitor.visitArray(key);
            for (Object arrayValue : (Object[]) value) {
                // Default key is 'value'. It can be changed by using AnnotationValue type.
                visitAnnotationValue(arrayVisitor, "value", arrayValue);
            }
            arrayVisitor.visitEnd();
        } else if (value instanceof AnnotationValue) {
            AnnotationValue annotationValue = (AnnotationValue) value;
            visitor.visit(annotationValue.name(), annotationValue.value());
        } else if (value instanceof Enum) {
            visitor.visitEnum(key, DescriptorUtils.objectToDescriptor(value.getClass()), ((Enum) value).name());
        } else {
            visitor.visit(key, value);
        }
    }
}
