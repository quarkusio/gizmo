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

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;

class FieldCreatorImpl implements FieldCreator {

    private final FieldDescriptor fieldDescriptor;
    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();
    private final boolean isOnInterface;
    private String signature;
    private int modifiers;

    FieldCreatorImpl(FieldDescriptor fieldDescriptor, boolean isOnInterface) {
        this.fieldDescriptor = fieldDescriptor;
        this.isOnInterface = isOnInterface;
        this.modifiers = isOnInterface ? (ACC_PUBLIC | ACC_STATIC | ACC_FINAL) : ACC_PRIVATE;
    }

    @Override
    public FieldDescriptor getFieldDescriptor() {
        return fieldDescriptor;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public FieldCreator setModifiers(int modifiers) {
        if (isOnInterface
                && modifiers != (ACC_PUBLIC | ACC_STATIC | ACC_FINAL)
                && modifiers != (ACC_PUBLIC | ACC_STATIC | ACC_FINAL | ACC_SYNTHETIC)) {
            throw new IllegalArgumentException("Interface field may only be public static final: " + fieldDescriptor);
        }

        this.modifiers = modifiers;
        return this;
    }

    @Override
    public void write(ClassVisitor file) {
        FieldVisitor fieldVisitor = file.visitField(modifiers, fieldDescriptor.getName(), fieldDescriptor.getType(), signature,
                null);
        for (AnnotationCreatorImpl annotation : annotations) {
            AnnotationVisitor av = fieldVisitor.visitAnnotation(DescriptorUtils.extToInt(annotation.getAnnotationType()),
                    annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
            for (Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
                AnnotationUtils.visitAnnotationValue(av, e.getKey(), e.getValue());
            }
            av.visitEnd();
        }
        fieldVisitor.visitEnd();
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
    public FieldCreator setSignature(String signature) {
        this.signature = signature;
        return this;
    }
}
