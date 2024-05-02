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

import org.junit.Assert;
import org.junit.Test;

public class SimpleTestCase {

    @SuppressWarnings("unused")
    public static int staticField;

    @Test
    public void testSimpleGetMessage() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle message = method.invokeStaticMethod(
                    MethodDescriptor.ofMethod(MessageClass.class.getName(), "getMessage", "Ljava/lang/String;"));
            method.returnValue(message);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Assert.assertTrue(clazz.isSynthetic());
        MyInterface myInterface = (MyInterface) clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals("MESSAGE", myInterface.transform("ignored"));
    }

    @Test
    public void testSetStaticField() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Runnable.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("run", void.class);
            method.writeStaticField(FieldDescriptor.of(SimpleTestCase.class, "staticField", int.class), method.load(101));
            method.returnValue(null);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Runnable myInterface = (Runnable) clazz.getDeclaredConstructor().newInstance();
        myInterface.run();
        Assert.assertEquals(101, staticField);
    }

    @Test
    public void testStringTransform() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle message = method.invokeStaticMethod(
                    MethodDescriptor.ofMethod(MessageClass.class.getName(), "getMessage", "Ljava/lang/String;"));
            ResultHandle constant = method.load(":CONST:");
            message = method.invokeVirtualMethod(
                    MethodDescriptor.ofMethod("java/lang/String", "concat", "Ljava/lang/String;", "Ljava/lang/String;"),
                    message, constant);
            message = method.invokeVirtualMethod(
                    MethodDescriptor.ofMethod("java/lang/String", "concat", "Ljava/lang/String;", "Ljava/lang/String;"),
                    message, method.getMethodParam(0));

            method.returnValue(message);
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("MESSAGE:CONST:PARAM", myInterface.transform("PARAM"));
    }

    @Test
    public void testUnboxing() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(IntInterface.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", int.class, Integer.class);
            method.returnValue(method.getMethodParam(0));
        }
        IntInterface myInterface = (IntInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals(10, myInterface.get(10));
    }

    @Test
    public void testUnboxing2() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(IntInterface.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", int.class, Integer.class);
            method.returnValue(method.newInstance(MethodDescriptor.ofConstructor(Integer.class, int.class), method.load(11)));
        }
        IntInterface myInterface = (IntInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals(11, myInterface.get(10));
    }
}
