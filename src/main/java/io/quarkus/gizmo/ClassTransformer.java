package io.quarkus.gizmo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

/**
 * Transforms an existing class by wrapping an ASM {@link ClassVisitor}.
 * The {@link #applyTo(ClassVisitor)} method must be called before the wrapped
 * {@code ClassVisitor} is visited and the result should be used instead
 * of the original visitor.
 * <p>
 * In other words, this class acts like a builder. First, the class transformation
 * must be described by calling {@code addInterface()}, {@code addMethod()},
 * {@code addField()}, {@code modifyMethod()}, {@code modifyField()}, etc.
 * Then, a {@code ClassVisitor} must be created using {@code applyTo()}. Afterwards,
 * the {@code ClassTransformer} instance should be discarded.
 * <p>
 * If a <em>modification</em> of a class member and an <em>addition</em> of a class
 * member of the same kind, with the same name and the same descriptor are configured,
 * the modification is performed prior to the addition. (It follows that
 * the {@code modify*} methods may not be used to alter members created using
 * the {@code add*} methods.) No other ordering guarantees are made.
 */
public class ClassTransformer {

    private final String className;

    private int addedModifiers = 0;
    private int removedModifiers = 0;
    private final Set<String> addedInterfaces = new HashSet<>();

    private final Map<MethodDescriptor, ClassTransfromerMethodCreatorImpl> addedMethods = new HashMap<>();
    private final Map<FieldDescriptor, FieldCreatorImpl> addedFields = new HashMap<>();

    private final Map<NamedDescriptor, MethodTransformer> modifiedMethods = new HashMap<>();
    private final Map<NamedDescriptor, FieldTransformer> modifiedFields = new HashMap<>();

    /**
     * @param className the name of the transformed class
     */
    public ClassTransformer(String className) {
        this.className = DescriptorUtils.objectToInternalClassName(className);
    }

    String getClassName() {
        return className;
    }

    boolean hasAddedMethod(MethodDescriptor methodDescriptor) {
        return addedMethods.containsKey(methodDescriptor);
    }

    /**
     * Adds given {@code modifiers} to the modifiers of this class. It is a responsibility
     * of the caller to make sure the resulting set of modifiers is valid.
     *
     * @param modifiers the modifiers to add to this class
     */
    public void addModifiers(int modifiers) {
        addedModifiers |= modifiers;
    }

    /**
     * Removes given {@code modifiers} from the modifiers of this class. It is a responsibility
     * of the caller to make sure the resulting set of modifiers is valid.
     *
     * @param modifiers the modifiers to remove from this class
     */
    public void removeModifiers(int modifiers) {
        removedModifiers |= modifiers;
    }

    /**
     * Adds given {@code interfaceName} to the set of interfaces implemented by this class.
     * It is a responsibility of the caller to make sure the class in fact implements the interface,
     * typically by adding all the methods with {@code addMethod()}.
     * <p>
     * Adding an interface that has already been added is a noop, as well as adding an interface
     * that the original class already implements.
     *
     * @param interfaceName the interface type to add
     * @throws IllegalArgumentException if the {@code interfaceName} is not a {@link Class} or {@link String}
     */
    public void addInterface(Object interfaceName) {
        addedInterfaces.add(DescriptorUtils.objectToInternalClassName(interfaceName));
    }

    /**
     * Returns a {@link MethodCreator} to configure a new method that will be added to this class.
     * <p>
     * The {@link MethodCreator#createFunction(Class)} method may not be used, as creating new class
     * would be required.
     *
     * @param methodDescriptor descriptor of the new method
     * @return a new {@link MethodCreator}
     */
    public MethodCreator addMethod(MethodDescriptor methodDescriptor) {
        if (addedMethods.containsKey(methodDescriptor)) {
            throw new IllegalStateException("Method already added: " + methodDescriptor);
        }
        ClassTransfromerMethodCreatorImpl creator = new ClassTransfromerMethodCreatorImpl(methodDescriptor, this);
        addedMethods.put(methodDescriptor, creator);
        return creator;
    }

    /**
     * Returns a {@link MethodCreator} to configure a new method that will be added to this class.
     * <p>
     * The {@link MethodCreator#createFunction(Class)} method may not be used, as creating new class
     * would be required.
     *
     * @param name name of the new method
     * @param returnType return type of the new method
     * @param parameters parameter types of the new method
     * @return a new {@link MethodCreator}
     * @throws IllegalArgumentException if the {@code returnType} or any of the {@code parameters} is not a {@link Class} or {@link String}
     */
    public MethodCreator addMethod(String name, Object returnType, Object... parameters) {
        return addMethod(MethodDescriptor.ofMethod(className, name, returnType, parameters));
    }

    /**
     * Returns a {@link FieldCreator} to configure a new field that will be added to this class.
     * 
     * @param fieldDescriptor descriptor of the new field
     * @return a new {@link FieldCreator}
     */
    public FieldCreator addField(FieldDescriptor fieldDescriptor) {
        if (addedFields.containsKey(fieldDescriptor)) {
            throw new IllegalStateException("Field already added: " + fieldDescriptor);
        }
        FieldCreatorImpl fieldCreator = new FieldCreatorImpl(fieldDescriptor, false);
        addedFields.put(fieldDescriptor, fieldCreator);
        return fieldCreator;
    }

    /**
     * Returns a {@link FieldCreator} to configure a new field that will be added to this class.
     * 
     * @param name name of the new field
     * @param type type of the new field
     * @return a new {@link FieldCreator}
     * @throws IllegalArgumentException if the {@code type} is not a {@link Class} or {@link String}
     */
    public FieldCreator addField(String name, Object type) {
        return addField(FieldDescriptor.of(className, name, DescriptorUtils.objectToDescriptor(type)));
    }

    /**
     * Returns a {@link MethodTransformer} to configure a transformation of given {@code method}.
     *
     * @param method descriptor of the method to transform
     * @return a {@link MethodTransformer}
     */
    public MethodTransformer modifyMethod(MethodDescriptor method) {
        NamedDescriptor key = new NamedDescriptor(method.getName(), method.getDescriptor());
        return modifiedMethods.computeIfAbsent(key, ignored -> new MethodTransformer());
    }

    /**
     * Returns a {@link MethodTransformer} to configure a transformation of a method with given
     * {@code name}, {@code returnType} and {@code parameters}.
     *
     * @param name name of the method
     * @param returnType return type of the method
     * @param parameters parameter types of the method
     * @return a {@link MethodTransformer}
     * @throws IllegalArgumentException if the {@code returnType} or any of the {@code parameters} is not a {@link Class} or {@link String}
     */
    public MethodTransformer modifyMethod(String name, Object returnType, Object... parameters) {
        return modifyMethod(MethodDescriptor.ofMethod(className, name, returnType, parameters));
    }

    /**
     * Returns a {@link FieldTransformer} to configure a transformation of given {@code field}.
     *
     * @param field descriptor of the field to transform
     * @return a {@link FieldTransformer}
     */
    public FieldTransformer modifyField(FieldDescriptor field) {
        NamedDescriptor key = new NamedDescriptor(field.getName(), field.getType());
        return modifiedFields.computeIfAbsent(key, ignored -> new FieldTransformer());
    }

    /**
     * Returns a {@link FieldTransformer} to configure a transformation of a field with given
     * {@code name} and {@code type}.
     *
     * @param name name of the field
     * @param type type of the field
     * @return a {@link FieldTransformer}
     * @throws IllegalArgumentException if the {@code returnType} or any of the {@code parameters} is not a {@link Class} or {@link String}
     */
    public FieldTransformer modifyField(String name, Object type) {
        return modifyField(FieldDescriptor.of(className, name, DescriptorUtils.objectToDescriptor(type)));
    }

    /**
     * Returns a {@link ClassVisitor} that applies the class transformation to given {@code visitor}.
     * At the moment this method is called, no {@code visit*} method must have been called on given {@code visitor}.
     * The transformation is not finished until {@code visitEnd()} is called on the resulting visitor.
     *
     * @param visitor the {@code ClassVisitor} to which the transformation is applied
     * @return the transforming {@code ClassVisitor}
     */
    public ClassVisitor applyTo(ClassVisitor visitor) {
        return new ClassVisitor(Gizmo.ASM_API_VERSION, visitor) {
            private boolean seenInitialVisit = false;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                access |= addedModifiers;
                access &= ~removedModifiers;

                String[] newInterfaces = interfaces;
                if (!addedInterfaces.isEmpty()) {
                    // preserve original order and avoid possible duplicates
                    LinkedHashSet<String> names = new LinkedHashSet<>(Arrays.asList(interfaces));
                    names.addAll(addedInterfaces);
                    newInterfaces = names.toArray(String[]::new);
                }

                super.visit(version, access, name, signature, superName, newInterfaces);
                seenInitialVisit = true;
            }

            @Override
            public void visitInnerClass(String name, String outerName, String innerName, int access) {
                // just in case the transformed class is a nested class, to make sure its `InnerClasses`
                // attribute is consistent with the class definition itself
                if (className.equals(name)) {
                    access |= addedModifiers;
                    access &= ~removedModifiers;
                }
                super.visitInnerClass(name, outerName, innerName, access);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                FieldTransformer fieldTransformer = modifiedFields.get(new NamedDescriptor(name, descriptor));
                if (fieldTransformer != null) {
                    access |= fieldTransformer.addedModifiers;
                    access &= ~fieldTransformer.removedModifiers;
                    name = fieldTransformer.newName != null ? fieldTransformer.newName : name;
                }

                return super.visitField(access, name, descriptor, signature, value);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodTransformer methodTransformer = modifiedMethods.get(new NamedDescriptor(name, descriptor));
                if (methodTransformer != null) {
                    access |= methodTransformer.addedModifiers;
                    access &= ~methodTransformer.removedModifiers;
                    name = methodTransformer.newName != null ? methodTransformer.newName : name;
                }

                return super.visitMethod(access, name, descriptor, signature, exceptions);
            }

            @Override
            public void visitEnd() {
                if (!seenInitialVisit) {
                    throw new IllegalStateException("The ClassTransformer was applied to a ClassVisitor that was already visited");
                }

                for (FieldCreator fieldCreator : addedFields.values()) {
                    fieldCreator.write(visitor);
                }
                for (MethodCreator methodCreator : addedMethods.values()) {
                    methodCreator.write(visitor);
                }
                super.visitEnd();
            }
        };
    }

    private static class NamedDescriptor {
        final String name;
        final String descriptor;

        NamedDescriptor(String name, String descriptor) {
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof NamedDescriptor)) {
                return false;
            }
            NamedDescriptor that = (NamedDescriptor) o;
            return Objects.equals(name, that.name) && Objects.equals(descriptor, that.descriptor);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, descriptor);
        }
    }

}
