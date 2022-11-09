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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.Writer;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassCreator implements AutoCloseable, AnnotatedElement, SignatureElement<ClassCreator> {

    public static Builder builder() {
        return new Builder();
    }

    private final BytecodeCreatorImpl enclosing;
    private final ClassOutput classOutput;
    private final String superClass;
    private final int access;
    private final String[] interfaces;
    private final Map<MethodDescriptor, MethodCreatorImpl> methods = new LinkedHashMap<>();
    private final Map<FieldDescriptor, FieldCreatorImpl> fields = new LinkedHashMap<>();
    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();
    private final String className;
    private String signature;
    private final Map<MethodDescriptor, MethodDescriptor> superclassAccessors = new LinkedHashMap<>();
    private final AtomicInteger accessorCount = new AtomicInteger();

    ClassCreator(BytecodeCreatorImpl enclosing, ClassOutput classOutput, String name, String signature, String superClass, int access, String... interfaces) {
        this.enclosing = enclosing;
        this.classOutput = classOutput;
        this.superClass = superClass.replace('.', '/');
        this.access = access;
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < interfaces.length; ++i) {
            this.interfaces[i] = interfaces[i].replace('.', '/');
        }
        this.className = name.replace('.', '/');
        this.signature = signature;
    }

    public ClassCreator(ClassOutput classOutput, String name, String signature, String superClass, String... interfaces) {
        this(null, classOutput, name, signature, superClass, 0, interfaces);
    }

    public MethodCreator getMethodCreator(MethodDescriptor methodDescriptor) {
        if (methods.containsKey(methodDescriptor)) {
            return methods.get(methodDescriptor);
        }
        MethodCreatorImpl creator = new MethodCreatorImpl(enclosing, methodDescriptor, className, this);
        methods.put(methodDescriptor, creator);
        return creator;
    }

    public MethodCreator getMethodCreator(String name, String returnType, String... parameters) {
        return getMethodCreator(MethodDescriptor.ofMethod(className, name, returnType, parameters));
    }

    public MethodCreator getMethodCreator(String name, Class<?> returnType, Class<?>... parameters) {
        String[] params = new String[parameters.length];
        for (int i = 0; i < parameters.length; ++i) {
            params[i] = DescriptorUtils.classToStringRepresentation(parameters[i]);
        }
        return getMethodCreator(name, DescriptorUtils.classToStringRepresentation(returnType), params);
    }

    public MethodCreator getMethodCreator(String name, Object returnType, Object... parameters) {
        return getMethodCreator(MethodDescriptor.ofMethod(className, name, returnType, parameters));
    }

    public FieldCreator getFieldCreator(String name, String type) {
        return getFieldCreator(FieldDescriptor.of(className, name, type));
    }

    public FieldCreator getFieldCreator(String name, Object type) {
        return getFieldCreator(FieldDescriptor.of(className, name, DescriptorUtils.objectToDescriptor(type)));
    }

    public FieldCreator getFieldCreator(FieldDescriptor fieldDescriptor) {
        FieldCreatorImpl field = fields.get(fieldDescriptor);
        if (field == null) {
            field = new FieldCreatorImpl(fieldDescriptor);
            fields.put(fieldDescriptor, field);
        }
        return field;
    }

    public String getSuperClass() {
        return superClass;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public String getClassName() {
        return className;
    }

    public String getSimpleClassName() {
        int index = className.lastIndexOf('/');
        return index < 0 ? className : className.substring(index + 1);
    }

    MethodDescriptor getSupertypeAccessor(MethodDescriptor descriptor, String supertype, boolean isInterface) {
        if (superclassAccessors.containsKey(descriptor)) {
            return superclassAccessors.get(descriptor);
        }
        String name = descriptor.getName() + "$$superaccessor" + accessorCount.incrementAndGet();
        MethodCreator ctor = getMethodCreator(name, descriptor.getReturnType(), descriptor.getParameterTypes());
        ResultHandle[] params = new ResultHandle[descriptor.getParameterTypes().length];
        for (int i = 0; i < params.length; ++i) {
            params[i] = ctor.getMethodParam(i);
        }
        MethodDescriptor superDescriptor = MethodDescriptor.ofMethod(supertype, descriptor.getName(), descriptor.getReturnType(), descriptor.getParameterTypes());
        ResultHandle ret;
        if (isInterface) {
            ret = ctor.invokeSpecialInterfaceMethod(superDescriptor, ctor.getThis(), params);
        } else {
            ret = ctor.invokeSpecialMethod(superDescriptor, ctor.getThis(), params);
        }
        ctor.returnValue(ret);
        superclassAccessors.put(descriptor, ctor.getMethodDescriptor());
        return ctor.getMethodDescriptor();
    }

    /**
     * Write the class bytes to the given class output.
     *
     * @param classOutput the class output (must not be {@code null})
     */
    public void writeTo(ClassOutput classOutput) {
        Objects.requireNonNull(classOutput);
        ClassWriter file = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        Writer sourceWriter = classOutput.getSourceWriter(className);
        ClassVisitor cv;
        if (sourceWriter != null) {
             cv = new GizmoClassVisitor(Gizmo.ASM_API_VERSION, file, sourceWriter);
        } else {
            cv = file;
        }
        String[] interfaces = this.interfaces.clone();
        cv.visit(Opcodes.V11, access, className, signature, superClass, interfaces);
        cv.visitSource(null, null);

        boolean requiresCtor = (access & Opcodes.ACC_INTERFACE) == 0;
        for (MethodDescriptor m : methods.keySet()) {
            if (m.getName().equals("<init>")) {
                requiresCtor = false;
                break;
            }
        }

        if (requiresCtor) {
            // constructor
            if (cv instanceof GizmoClassVisitor) {
                ((GizmoClassVisitor)cv).append("// Auto-generated constructor").newLine();
            }
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
            mv.visitVarInsn(ALOAD, 0); // push `this` to the operand stack
            mv.visitMethodInsn(INVOKESPECIAL, superClass, "<init>", "()V", false); // call the constructor of super class
            mv.visitInsn(RETURN);
            mv.visitMaxs(0, 1);
            mv.visitEnd();
        }

        //now add the fields
        for (Map.Entry<FieldDescriptor, FieldCreatorImpl> field : fields.entrySet()) {
            field.getValue().write(cv);
        }

        for (Map.Entry<MethodDescriptor, MethodCreatorImpl> method : methods.entrySet()) {
            method.getValue().write(cv);
        }
        for(AnnotationCreatorImpl annotation : annotations) {
            AnnotationVisitor av = cv.visitAnnotation(DescriptorUtils.extToInt(annotation.getAnnotationType()), annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
            for(Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
                AnnotationUtils.visitAnnotationValue(av, e.getKey(), e.getValue());
            }
            av.visitEnd();
        }

        cv.visitEnd();

        classOutput.write(className, file.toByteArray());
    }

    /**
     * Finish the class creator.  If a class output was configured for this class creator, the class bytes
     * will immediately be written there.
     */
    @Override
    public void close() {
        final ClassOutput classOutput = this.classOutput;
        if (classOutput != null) {
            writeTo(classOutput);
        }
    }

    @Override
    public AnnotationCreator addAnnotation(String annotationType, RetentionPolicy retentionPolicy) {
        AnnotationCreatorImpl ac = new AnnotationCreatorImpl(annotationType, retentionPolicy);
        annotations.add(ac);
        return ac;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public ClassCreator setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    public Set<MethodDescriptor> getExistingMethods() {
        return methods.keySet();
    }

    public Set<FieldDescriptor> getExistingFields() {
        return fields.keySet();
    }

    ClassOutput getClassOutput() {
        return classOutput;
    }

    public static class Builder {

        private ClassOutput classOutput;

        private String className;

        private String signature;

        private String superClass;

        private final List<String> interfaces;

        private BytecodeCreatorImpl enclosing;

        private int access = ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC;

        Builder() {
            superClass(Object.class);
            this.interfaces = new ArrayList<>();
        }

        Builder enclosing(BytecodeCreatorImpl enclosing) {
            this.enclosing = enclosing;
            return this;
        }

        public Builder classOutput(ClassOutput classOutput) {
            this.classOutput = classOutput;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder signature(String signature) {
            this.signature = signature;
            return this;
        }

        public Builder superClass(String superClass) {
            this.superClass = superClass;
            return this;
        }

        public Builder superClass(Class<?> superClass) {
            return superClass(superClass.getName());
        }

        public Builder setFinal(boolean isFinal) {
            if (isFinal) {
                access |= Opcodes.ACC_FINAL;
            } else {
                access &= ~Opcodes.ACC_FINAL;
            }
            return this;
        }

        public Builder setInterface(boolean isInterface) {
            if (isInterface) {
                access &= ~Opcodes.ACC_SUPER;
                access |= Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
            } else {
                access &= ~Opcodes.ACC_INTERFACE & ~Opcodes.ACC_ABSTRACT;
                access |= Opcodes.ACC_SUPER;
            }
            return this;
        }

        public Builder interfaces(String... interfaces) {
            Collections.addAll(this.interfaces, interfaces);
            return this;
        }

        public Builder interfaces(Class<?>... interfaces) {
            for (Class<?> val : interfaces) {
                this.interfaces.add(val.getName());
            }
            return this;
        }

        public ClassCreator build() {
            Objects.requireNonNull(className);
            Objects.requireNonNull(superClass);
            return new ClassCreator(enclosing, classOutput, className, signature, superClass, access, interfaces.toArray(new String[0]));
        }

    }

}
