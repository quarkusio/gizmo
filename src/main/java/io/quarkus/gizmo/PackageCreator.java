package io.quarkus.gizmo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PackageCreator implements AutoCloseable, AnnotatedElement {

    public static PackageCreator.Builder builder() {
        return new PackageCreator.Builder();
    }

    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();
    private final BytecodeCreatorImpl enclosing;
    private final ClassOutput classOutput;
    private final String packageName;

    PackageCreator(BytecodeCreatorImpl enclosing, ClassOutput classOutput, String packageName) {
        this.enclosing = enclosing;
        this.classOutput = classOutput;
        this.packageName = packageName.replace('.', '/') + "/package-info";
    }

    @Override
    public AnnotationCreator addAnnotation(String annotationType, RetentionPolicy retentionPolicy) {
        AnnotationCreatorImpl ac = new AnnotationCreatorImpl(annotationType, retentionPolicy);
        annotations.add(ac);
        return ac;
    }

    /**
     * Write the class bytes to the given class output.
     *
     * @param classOutput the class output (must not be {@code null})
     */
    public void writeTo(ClassOutput classOutput) {
        Objects.requireNonNull(classOutput);
        ClassWriter file = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final GizmoClassVisitor cv = new GizmoClassVisitor(Gizmo.ASM_API_VERSION, file, classOutput.getSourceWriter(packageName));
        cv.visit(Opcodes.V1_5, Opcodes.ACC_ABSTRACT + Opcodes.ACC_INTERFACE, packageName, null,
                "java/lang/Object", null);

        for(AnnotationCreatorImpl annotation : annotations) {
            AnnotationVisitor av = cv.visitAnnotation(DescriptorUtils.extToInt(annotation.getAnnotationType()), annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
            for(Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
                AnnotationUtils.visitAnnotationValue(av, e.getKey(), e.getValue());
            }
            av.visitEnd();
        }

        cv.visitEnd();

        classOutput.write(packageName, file.toByteArray());
    }

    @Override
    public void close() throws Exception {
        final ClassOutput classOutput = this.classOutput;
        if (classOutput != null) {
            writeTo(classOutput);
        }
    }
    public static class Builder {

        private ClassOutput classOutput;

        private BytecodeCreatorImpl enclosing;

        private String packageName;

        Builder() {
        }

        PackageCreator.Builder enclosing(BytecodeCreatorImpl enclosing) {
            this.enclosing = enclosing;
            return this;
        }

        public PackageCreator.Builder classOutput(ClassOutput classOutput) {
            this.classOutput = classOutput;
            return this;
        }

        public PackageCreator.Builder packageName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public PackageCreator build() {
            Objects.requireNonNull(packageName);
            return new PackageCreator(enclosing, classOutput, packageName);
        }

    }
}
