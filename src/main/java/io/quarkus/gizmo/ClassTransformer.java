package io.quarkus.gizmo;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.objectweb.asm.ClassVisitor;

/**
 * Transform an existing class represented as an ASM {@link ClassVisitor}.
 * <p>
 * Note that the {@link ClassVisitor#close()} method must be called before {@link ClassVisitor#visitEnd()}.
 */
public class ClassTransformer implements AutoCloseable {

    private final String className;
    private final ClassVisitor classVisitor;
    private final Map<MethodDescriptor, ClassTransfromerMethodCreatorImpl> methods;
    private final Map<FieldDescriptor, FieldCreatorImpl> fields;

    /**
     * 
     * @param className
     * @param classVisitor
     */
    public ClassTransformer(String className, ClassVisitor classVisitor) {
        this.className = DescriptorUtils.objectToInternalClassName(className);
        this.classVisitor = Objects.requireNonNull(classVisitor);
        this.methods = new HashMap<>();
        this.fields = new HashMap<>();
    }

    /**
     * Returns a {@link MethodCreator} to configure a new method that will be added to this class.
     * <p>
     * {@link MethodCreator#createFunction(Class)} and {@link MethodCreator#enumSwitch(ResultHandle, Class)}
     * methods may not be used, as creating new classes would be required.
     *
     * @param methodDescriptor
     * @return a new {@link MethodCreator}
     */
    public MethodCreator addMethod(MethodDescriptor methodDescriptor) {
        if (methods.containsKey(methodDescriptor)) {
            throw new IllegalStateException("Method already added: " + methodDescriptor);
        }
        ClassTransfromerMethodCreatorImpl creator = new ClassTransfromerMethodCreatorImpl(methodDescriptor);
        methods.put(methodDescriptor, creator);
        return creator;
    }

    /**
     * Returns a {@link MethodCreator} to configure a new method that will be added to this class.
     * <p>
     * {@link MethodCreator#createFunction(Class)} and {@link MethodCreator#enumSwitch(ResultHandle, Class)}
     * methods may not be used, as creating new classes would be required.
     * 
     * @param name
     * @param returnType
     * @param parameters
     * @return a new {@link MethodCreator}
     * @throws IllegalArgumentException If the {@code returnType} or a parameter argument is not {@link Class} or {@link String}
     */
    public MethodCreator addMethod(String name, Object returnType, Object... parameters) {
        return addMethod(MethodDescriptor.ofMethod(className, name, returnType, parameters));
    }

    /**
     * Returns a {@link FieldCreator} to configure a new field that will be added to this class.
     * 
     * @param fieldDescriptor
     * @return a new {@link FieldCreator}
     */
    public FieldCreator addField(FieldDescriptor fieldDescriptor) {
        if (fields.containsKey(fieldDescriptor)) {
            throw new IllegalStateException("Field already added: " + fieldDescriptor);
        }
        FieldCreatorImpl fieldCreator = new FieldCreatorImpl(fieldDescriptor, false);
        fields.put(fieldDescriptor, fieldCreator);
        return fieldCreator;
    }

    /**
     * Returns a {@link FieldCreator} to configure a new field that will be added to this class.
     * 
     * @param name
     * @param type
     * @return a new {@link FieldCreator}
     * @throws IllegalArgumentException If the {@code type} argument is not {@link Class} or {@link String}
     */
    public FieldCreator addField(String name, Object type) {
        return addField(FieldDescriptor.of(className, name, DescriptorUtils.objectToDescriptor(type)));
    }

    @Override
    public void close() {
        for (FieldCreator fieldCreator : fields.values()) {
            fieldCreator.write(classVisitor);
        }
        for (MethodCreator methodCreator : methods.values()) {
            methodCreator.write(classVisitor);
        }
    }

}
