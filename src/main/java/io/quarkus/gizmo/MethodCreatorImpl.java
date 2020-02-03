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
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodCreatorImpl extends BytecodeCreatorImpl implements MethodCreator {

    private int modifiers = Opcodes.ACC_PUBLIC;
    private final List<String> exceptions = new ArrayList<>();
    private final List<AnnotationCreatorImpl> annotations = new ArrayList<>();
    private final Map<Integer, AnnotationParameters> parameterAnnotations = new LinkedHashMap<>();

    private final MethodDescriptor methodDescriptor;
    private final String declaringClassName;
    private final ClassCreator classCreator;
    private String signature;

    MethodCreatorImpl(BytecodeCreatorImpl enclosing, MethodDescriptor methodDescriptor, String declaringClassName, ClassCreator classCreator) {
        super(enclosing, true);
        this.methodDescriptor = methodDescriptor;
        this.declaringClassName = declaringClassName;
        this.classCreator = classCreator;
    }

    @Override
    public MethodCreator addException(String exception) {
        exceptions.add(exception.replace('.', '/'));
        return this;
    }

    @Override
    public List<String> getExceptions() {
        return Collections.unmodifiableList(exceptions);
    }

    @Override
    public MethodDescriptor getMethodDescriptor() {
        return methodDescriptor;
    }

    @Override
    public AnnotatedElement getParameterAnnotations(int param) {
        if(parameterAnnotations.containsKey(param)) {
            return parameterAnnotations.get(param);
        }
        AnnotationParameters p = new AnnotationParameters();
        parameterAnnotations.put(param, p);
        return p;
    }

    @Override
    public int getModifiers() {
        return modifiers;
    }

    @Override
    public MethodCreator setModifiers(int mods) {
        this.modifiers = mods;
        return this;
    }

    @Override
    public void write(ClassVisitor file) {
        MethodVisitor visitor = file.visitMethod(modifiers, methodDescriptor.getName(), methodDescriptor.getDescriptor(), signature, exceptions.toArray(new String[0]));

        int localVarCount = Modifier.isStatic(modifiers) ? 0 : 1;
        for (int i = 0; i < methodDescriptor.getParameterTypes().length; ++i) {
            String s = methodDescriptor.getParameterTypes()[i];
            if (s.equals("J") || s.equals("D")) {
                localVarCount += 2;
            } else {
                localVarCount++;
            }
        }
        int varCount = allocateLocalVariables(localVarCount);
        writeOperations(visitor);
        visitor.visitMaxs(0, varCount);

        for(AnnotationCreatorImpl annotation : annotations) {
            AnnotationVisitor av = visitor.visitAnnotation(DescriptorUtils.extToInt(annotation.getAnnotationType()), annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
            for(Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
                AnnotationUtils.visitAnnotationValue(av, e.getKey(), e.getValue());
            }
            av.visitEnd();
        }
        for(Map.Entry<Integer, AnnotationParameters> entry : parameterAnnotations.entrySet()) {
            for(AnnotationCreatorImpl annotation : entry.getValue().annotations) {
                AnnotationVisitor av = visitor.visitParameterAnnotation(entry.getKey(), DescriptorUtils.extToInt(annotation.getAnnotationType()), annotation.getRetentionPolicy() == RetentionPolicy.RUNTIME);
                for(Map.Entry<String, Object> e : annotation.getValues().entrySet()) {
                    AnnotationUtils.visitAnnotationValue(av, e.getKey(), e.getValue());
                }
                av.visitEnd();
            }
        }
        visitor.visitEnd();
    }

    @Override
    ResultHandle resolve(final ResultHandle handle, BytecodeCreator creator) {
        return handle;
    }

    @Override
    ResultHandle[] resolve(BytecodeCreator owner, final ResultHandle... handles) {
        return handles;
    }

    @Override
    public String toString() {
        return "MethodCreatorImpl [declaringClassName=" + getDeclaringClassName() + ", methodDescriptor=" + methodDescriptor + "]";
    }

    @Override
    public AnnotationCreator addAnnotation(String annotationType, RetentionPolicy retentionPolicy) {
        AnnotationCreatorImpl ac = new AnnotationCreatorImpl(annotationType, retentionPolicy);
        annotations.add(ac);
        return ac;
    }

    String getDeclaringClassName() {
        return declaringClassName;
    }

    ClassOutput getClassOutput() {
        return classCreator.getClassOutput();
    }

    ClassCreator getClassCreator() {
        return classCreator;
    }

    FunctionCreatorImpl addFunctionBody(final ResultHandle instance, final ClassCreator cc, final MethodCreatorImpl mc, final BytecodeCreatorImpl owner) {
        FunctionCreatorImpl fc = new FunctionCreatorImpl(instance, cc, mc, owner);
        operations.add(new Operation() {
            @Override
            void writeBytecode(final MethodVisitor methodVisitor) {
                fc.getBytecode().writeOperations(methodVisitor);
            }

            @Override
            Set<ResultHandle> getInputResultHandles() {
                return Collections.emptySet();
            }

            @Override
            ResultHandle getTopResultHandle() {
                return null;
            }

            @Override
            ResultHandle getOutgoingResultHandle() {
                return null;
            }

            @Override
            public void findResultHandles(final Set<ResultHandle> vc) {
                fc.getBytecode().findActiveResultHandles(vc);
            }
        });
        return fc;
    }

    @Override
    public String getSignature() {
        return signature;
    }

    @Override
    public MethodCreator setSignature(String signature) {
        this.signature = signature;
        return this;
    }

    private static class AnnotationParameters implements AnnotatedElement {

        final List<AnnotationCreatorImpl> annotations = new ArrayList<>();

        @Override
        public AnnotationCreator addAnnotation(String annotationType, RetentionPolicy retentionPolicy) {
            AnnotationCreatorImpl ret = new AnnotationCreatorImpl(annotationType, retentionPolicy);
            annotations.add(ret);
            return ret;
        }
    }

}
