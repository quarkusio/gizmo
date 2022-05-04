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

import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;


public class ArrayTestCase {

    @Test
    public void testNewArray() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(String.class, method.load(10));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(String[].class, o.getClass());
        String[] res = (String[]) o;
        Assert.assertEquals(10, res.length);

    }

    @Test
    public void testWriteArrayBoolean() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(boolean.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load(true));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(boolean[].class, o.getClass());
        boolean[] res = (boolean[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(true, res[0]);
    }

    @Test
    public void testWriteArrayByte() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(byte.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load((byte) 42));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(byte[].class, o.getClass());
        byte[] res = (byte[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42, res[0]);
    }

    @Test
    public void testWriteArrayShort() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(short.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load((short) 42));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(short[].class, o.getClass());
        short[] res = (short[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42, res[0]);
    }

    @Test
    public void testWriteArrayInt() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(int.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load(42));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(int[].class, o.getClass());
        int[] res = (int[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42, res[0]);
    }

    @Test
    public void testWriteArrayLong() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(long.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load(42L));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(long[].class, o.getClass());
        long[] res = (long[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42L, res[0]);
    }

    @Test
    public void testWriteArrayFloat() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(float.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load(42.0F));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(float[].class, o.getClass());
        float[] res = (float[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42.0F, res[0], 0.0);
    }

    @Test
    public void testWriteArrayDouble() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(double.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load(42.0));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(double[].class, o.getClass());
        double[] res = (double[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals(42.0, res[0], 0.0);
    }

    @Test
    public void testWriteArrayChar() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(char.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load('a'));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(char[].class, o.getClass());
        char[] res = (char[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals('a', res[0], 0.0);
    }

    @Test
    public void testWriteArrayObject() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(String.class, 1);
            method.writeArrayValue(arrayHandle, method.load(0), method.load("hello"));
            method.returnValue(arrayHandle);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(String[].class, o.getClass());
        String[] res = (String[]) o;
        Assert.assertEquals(1, res.length);
        Assert.assertEquals("hello", res[0]);
    }

    @Test
    public void testReadArrayBoolean() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), boolean[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        boolean[] array = new boolean[1];
        array[0] = true;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Boolean.class, o.getClass());
        boolean val = (Boolean) o;
        Assert.assertEquals(true, val);
    }

    @Test
    public void testReadArrayChar() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), char[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        char[] array = new char[1];
        array[0] = 'a';
        Object o = myInterface.apply(array);
        Assert.assertEquals(Character.class, o.getClass());
        char val = (Character) o;
        Assert.assertEquals('a', val);
    }

    @Test
    public void testReadArrayDouble() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), double[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        double[] array = new double[1];
        array[0] = 101.0d;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Double.class, o.getClass());
        double val = (Double) o;
        Assert.assertEquals(101.0d, val, 0);
    }

    @Test
    public void testReadArrayFloat() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), float[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        float[] array = new float[1];
        array[0] = 101.0f;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Float.class, o.getClass());
        float val = (Float) o;
        Assert.assertEquals(101.0f, val, 0);
    }

    @Test
    public void testReadArrayByte() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), byte[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        byte[] array = new byte[1];
        array[0] = 26;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Byte.class, o.getClass());
        Assert.assertEquals(26, (byte) o);
    }

    @Test
    public void testReadArrayShort() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), short[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        short[] array = new short[1];
        array[0] = 26;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Short.class, o.getClass());
        Assert.assertEquals(26, (short) o);
    }

    @Test
    public void testReadArrayInt() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), int[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        int[] array = new int[1];
        array[0] = 26;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Integer.class, o.getClass());
        Assert.assertEquals(26, (int) o);
    }

    @Test
    public void testReadArrayLong() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), long[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        long[] array = new long[1];
        array[0] = 26;
        Object o = myInterface.apply(array);
        Assert.assertEquals(Long.class, o.getClass());
        Assert.assertEquals(26, (long) o);
    }

    @Test
    public void testReadArrayObject() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Function.class).build()) {
            MethodCreator method = creator.getMethodCreator("apply", Object.class, Object.class);
            ResultHandle arrayHandle = method.checkCast(method.getMethodParam(0), Object[].class);
            ResultHandle ret = method.readArrayValue(arrayHandle, 0);
            method.returnValue(ret);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Function myInterface = (Function) clazz.getDeclaredConstructor().newInstance();
        String[] array = new String[1];
        array[0] = "hello array";
        Object o = myInterface.apply(array);
        Assert.assertEquals(String.class, o.getClass());
        Assert.assertEquals("hello array", o);
    }

    @Test
    public void testArrayLength() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle arrayHandle = method.newArray(String.class, method.load(10));
            ResultHandle arrayLength = method.arrayLength(arrayHandle);
            method.returnValue(
                    method.invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "valueOf", Integer.class, int.class), arrayLength));
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(Integer.class, o.getClass());
        Integer res = (Integer) o;
        Assert.assertEquals((Object) 10, res);

    }
}
