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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.jandex.TypeVariable;


public class MethodDescriptor {

    private final String declaringClass;
    private final String name;
    private final String returnType;
    private final String[] parameterTypes;
    private final String descriptor;
    private Map<String, FormalType> formalTypeParameters;

    private MethodDescriptor(String declaringClass, String name, String returnType, String... parameterTypes) {
        this.declaringClass = declaringClass;
        this.name = name;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
        this.formalTypeParameters = new HashMap<>();
        this.descriptor = DescriptorUtils.methodSignatureToDescriptor(returnType, formalTypeParameters, parameterTypes);
        for (String p : parameterTypes) {
            if (p.length() != 1) {
                if (!(p.startsWith("L") && p.endsWith(";") || p.startsWith("["))) {
                    throw new IllegalArgumentException("Invalid parameter type " + p + " it must be in the JVM descriptor format");
                }
            }
        }
        if (returnType.length() != 1) {
            if (!(returnType.startsWith("L") && returnType.endsWith(";") || returnType.startsWith("["))) {
                throw new IllegalArgumentException("Invalid return type " + returnType + " it must be in the JVM descriptor format");
            }
        }
    }

    private MethodDescriptor(MethodInfo info) {
        this.name = info.name();
        this.returnType = DescriptorUtils.typeToString(info.returnType());
        String[] paramTypes = new String[info.parameters().size()];
        for (int i = 0; i < paramTypes.length; ++i) {
            paramTypes[i] = DescriptorUtils.typeToString(info.parameters().get(i));
        }
        this.parameterTypes = paramTypes;
        this.declaringClass = info.declaringClass().toString().replace('.', '/');
        this.formalTypeParameters = new HashMap<>();
        this.descriptor = DescriptorUtils.methodSignatureToDescriptor(returnType, formalTypeParameters, parameterTypes);
    }

    public static MethodDescriptor ofMethod(String declaringClass, String name, String returnType, String... parameterTypes) {
        return new MethodDescriptor(DescriptorUtils.objectToInternalClassName(declaringClass), name, DescriptorUtils.objectToDescriptor(returnType), DescriptorUtils.objectsToDescriptor(parameterTypes));
    }

    public static MethodDescriptor ofMethod(Class<?> declaringClass, String name, Class<?> returnType, Class<?>... parameterTypes) {
        String[] args = new String[parameterTypes.length];
        for (int i = 0; i < args.length; ++i) {
            args[i] = DescriptorUtils.classToStringRepresentation(parameterTypes[i]);
        }

        return new MethodDescriptor(DescriptorUtils.objectToInternalClassName(declaringClass), name, DescriptorUtils.classToStringRepresentation(returnType), args);
    }

    public static MethodDescriptor ofMethod(Method method) {
        return ofMethod(method.getDeclaringClass(), method.getName(), method.getReturnType(), method.getGenericParameterTypes(), method.getParameterTypes());
    }

    public static MethodDescriptor ofMethod(Object declaringClass, String name, Object returnType, Object... parameterTypes) {
        return new MethodDescriptor(DescriptorUtils.objectToInternalClassName(declaringClass), name, DescriptorUtils.objectToDescriptor(returnType), DescriptorUtils.objectsToDescriptor(parameterTypes));
    }

    public static MethodDescriptor ofConstructor(String declaringClass, String... parameterTypes) {
        return ofMethod(declaringClass, "<init>", void.class.getName(), parameterTypes);
    }

    public static MethodDescriptor ofConstructor(Class<?> declaringClass, Class<?>... parameterTypes) {
        return ofMethod(declaringClass, "<init>", void.class, (Object[]) parameterTypes);
    }

    public static MethodDescriptor ofConstructor(Object declaringClass, Object... parameterTypes) {
        return ofMethod(declaringClass, "<init>", void.class, (Object[]) parameterTypes);
    }

    public static MethodDescriptor of(MethodInfo methodInfo) {
        return new MethodDescriptor(methodInfo);
    }

    public void formalType(String name, String superClass, String... interfaces) {
        formalTypeParameters.put(name, new FormalType(name, superClass, interfaces));
    }

    public String getName() {
        return name;
    }

    public String getReturnType() {
        return returnType;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getDeclaringClass() {
        return declaringClass;
    }

    public Map<String, FormalType> getFormalTypeParameters() {
        return formalTypeParameters;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MethodDescriptor && equals((MethodDescriptor) o);
    }

    public boolean equals(MethodDescriptor o) {
        return o == this || o != null
            && declaringClass.equals(o.declaringClass)
            && name.equals(o.name)
            && descriptor.equals(o.descriptor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(declaringClass, name, descriptor);
    }

    @Override
    public String toString() {
        return "MethodDescriptor{" +
                (formalTypeParameters.isEmpty() ? "": "<" + formalTypeParameters.values().toString() + ">") +
                "name='" + name + '\'' +
                ", returnType='" + returnType + '\'' +
                ", parameterTypes=" + Arrays.toString(parameterTypes) +
                '}';
    }

    public String getDescriptor() {
        if (formalTypeParameters.isEmpty()) {
            return descriptor;
        } else {
            //refresh because formalType is added after creation to avoid public API break
            return DescriptorUtils.methodSignatureToDescriptor(returnType, formalTypeParameters, parameterTypes);
        }
    }
}
