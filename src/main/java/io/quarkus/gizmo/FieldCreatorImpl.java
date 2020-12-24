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

import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jdk.internal.access.JavaSecurityAccess;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;

class FieldCreatorImpl implements FieldCreator, SignatureElement<FieldCreatorImpl> {

    private final FieldDescriptor fieldDescriptor;
    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();
    private String signature;
    private int modifiers;
    private final Map<String, FormalType> formalTypeParameters;

    public FieldCreatorImpl(FieldDescriptor fieldDescriptor) {
        this.fieldDescriptor = fieldDescriptor;
        this.modifiers = Opcodes.ACC_PRIVATE;
        this.formalTypeParameters = new HashMap<>();
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
        this.modifiers = modifiers;
        return this;
    }

    public FieldCreator formalType(String name) {
        return formalType(name, Object.class.getName());
    }

    public FieldCreator formalType(String name, String superClass, String... interfaces) {
        formalTypeParameters.put(name, new FormalType(name, superClass, interfaces));
        return this;
    }

    @Override
    public void write(ClassVisitor file) {
        if (!formalTypeParameters.isEmpty()) {
            SignatureUtils.TypeSignature SignatureGen = new SignatureUtils.TypeSignature();
            SignatureGen.Type(fieldDescriptor.getType());
            SignatureGen.genericParameters(fieldDescriptor.getGenericParameters());
            for(FormalType formalType : formalTypeParameters.values()) {
                SignatureGen.formalType(formalType.getName(), formalType.getSuperClass(), formalType.getInterfaces());
            }
            signature = SignatureGen.generate();
        }
        FieldVisitor fieldVisitor = file.visitField(modifiers, fieldDescriptor.getName(), fieldDescriptor.getType(), signature, null);
        for(AnnotationCreatorImpl annotation : annotations) {
            AnnotationVisitor av = fieldVisitor.visitAnnotation(DescriptorUtils.extToInt(annotation.getAnnotationType()), annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
            for(Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
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
    @Deprecated
    public FieldCreatorImpl setSignature(String signature) {
        this.signature = signature;
        return this;
    }
}
