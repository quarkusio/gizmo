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

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class LoadClassTestCase {

    @Test
    public void testLoadClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle stringHandle = method.loadClass(String.class);
            method.returnValue(stringHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(String.class, myInterface.get());
    }

    @Test
    public void testLoadClassFromTCCL() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle stringHandle = method.loadClassFromTCCL(String.class);
            method.returnValue(stringHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(String.class, myInterface.get());
    }

    @Test
    public void testLoadNonPublicClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle stringHandle = method.loadClass("java.util.Collections$EmptyList");
            method.returnValue(stringHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Assert.assertThrows(IllegalAccessError.class, myInterface::get);
    }

    @Test
    public void testLoadNonPublicClassFromTCCL() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle stringHandle = method.loadClassFromTCCL("java.util.Collections$EmptyList");
            method.returnValue(stringHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(Class.forName("java.util.Collections$EmptyList"), myInterface.get());
    }

    @Test
    public void testLoadVoidClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle voidHandle = method.loadClass(void.class);
            method.returnValue(voidHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(void.class, myInterface.get());
    }

}
