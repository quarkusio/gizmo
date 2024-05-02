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

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

public class ArithmeticTestCase {

    @Test
    public void testAddition() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator addInts = creator.getMethodCreator("addInts", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1 = addInts.load(1);
            ResultHandle int2 = addInts.load(2);
            addInts.returnValue(addInts.add(int1, int2));

            MethodCreator addLongs = creator.getMethodCreator("addLongs", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1 = addLongs.load(3L);
            ResultHandle long2 = addLongs.load(4L);
            addLongs.returnValue(addLongs.add(long1, long2));

            MethodCreator addFloats = creator.getMethodCreator("addFloats", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1 = addFloats.load(5.0F);
            ResultHandle float2 = addFloats.load(6.0F);
            addFloats.returnValue(addFloats.add(float1, float2));

            MethodCreator addDoubles = creator.getMethodCreator("addDoubles", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1 = addDoubles.load(7.0);
            ResultHandle double2 = addDoubles.load(8.0);
            addDoubles.returnValue(addDoubles.add(double1, double2));
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertEquals(3, clazz.getMethod("addInts").invoke(null));
        assertEquals(7L, clazz.getMethod("addLongs").invoke(null));
        assertEquals(11.0F, clazz.getMethod("addFloats").invoke(null));
        assertEquals(15.0, clazz.getMethod("addDoubles").invoke(null));
    }

    @Test
    public void testSubtraction() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator subInts = creator.getMethodCreator("subInts", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1 = subInts.load(2);
            ResultHandle int2 = subInts.load(1);
            subInts.returnValue(subInts.subtract(int1, int2));

            MethodCreator subLongs = creator.getMethodCreator("subLongs", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1 = subLongs.load(5L);
            ResultHandle long2 = subLongs.load(8L);
            subLongs.returnValue(subLongs.subtract(long1, long2));

            MethodCreator subFloats = creator.getMethodCreator("subFloats", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1 = subFloats.load(3.0F);
            ResultHandle float2 = subFloats.load(3.0F);
            subFloats.returnValue(subFloats.subtract(float1, float2));

            MethodCreator subDoubles = creator.getMethodCreator("subDoubles", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1 = subDoubles.load(21.0);
            ResultHandle double2 = subDoubles.load(6.0);
            subDoubles.returnValue(subDoubles.subtract(double1, double2));
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertEquals(1, clazz.getMethod("subInts").invoke(null));
        assertEquals(-3L, clazz.getMethod("subLongs").invoke(null));
        assertEquals(0.0F, clazz.getMethod("subFloats").invoke(null));
        assertEquals(15.0, clazz.getMethod("subDoubles").invoke(null));
    }

    @Test
    public void testMultiplication() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator multiplyInts = creator.getMethodCreator("multiplyInts", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1 = multiplyInts.load(2);
            ResultHandle int2 = multiplyInts.load(3);
            multiplyInts.returnValue(multiplyInts.multiply(int1, int2));

            MethodCreator multiplyLongs = creator.getMethodCreator("multiplyLongs", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1 = multiplyLongs.load(4L);
            ResultHandle long2 = multiplyLongs.load(5L);
            multiplyLongs.returnValue(multiplyLongs.multiply(long1, long2));

            MethodCreator multiplyFloats = creator.getMethodCreator("multiplyFloats", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1 = multiplyFloats.load(6.0F);
            ResultHandle float2 = multiplyFloats.load(7.0F);
            multiplyFloats.returnValue(multiplyFloats.multiply(float1, float2));

            MethodCreator multiplyDoubles = creator.getMethodCreator("multiplyDoubles", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1 = multiplyDoubles.load(8.0D);
            ResultHandle double2 = multiplyDoubles.load(9.0D);
            multiplyDoubles.returnValue(multiplyDoubles.multiply(double1, double2));
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertEquals(6, clazz.getMethod("multiplyInts").invoke(null));
        assertEquals(20L, clazz.getMethod("multiplyLongs").invoke(null));
        assertEquals(42.0F, clazz.getMethod("multiplyFloats").invoke(null));
        assertEquals(72.0D, clazz.getMethod("multiplyDoubles").invoke(null));
    }

    @Test
    public void testDivision() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator divideInts = creator.getMethodCreator("divideInts", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1 = divideInts.load(2);
            ResultHandle int2 = divideInts.load(3);
            divideInts.returnValue(divideInts.divide(int1, int2));

            MethodCreator divideIntsImproper = creator.getMethodCreator("divideIntsImproper", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1imp = divideIntsImproper.load(18);
            ResultHandle int2imp = divideIntsImproper.load(5);
            divideIntsImproper.returnValue(divideIntsImproper.divide(int1imp, int2imp));

            MethodCreator divideLongs = creator.getMethodCreator("divideLongs", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1 = divideLongs.load(5L);
            ResultHandle long2 = divideLongs.load(4L);
            divideLongs.returnValue(divideLongs.divide(long1, long2));

            MethodCreator divideFloats = creator.getMethodCreator("divideFloats", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1 = divideFloats.load(6.0F);
            ResultHandle float2 = divideFloats.load(7.0F);
            divideFloats.returnValue(divideFloats.divide(float1, float2));

            MethodCreator divideDoubles = creator.getMethodCreator("divideDoubles", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1 = divideDoubles.load(8.0D);
            ResultHandle double2 = divideDoubles.load(9.0D);
            divideDoubles.returnValue(divideDoubles.divide(double1, double2));
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertEquals(0, clazz.getMethod("divideInts").invoke(null));
        assertEquals(3, clazz.getMethod("divideIntsImproper").invoke(null));
        assertEquals(1L, clazz.getMethod("divideLongs").invoke(null));
        assertEquals(0.85714287F, clazz.getMethod("divideFloats").invoke(null));
        assertEquals(0.8888888888888888D, clazz.getMethod("divideDoubles").invoke(null));
    }

    @Test
    public void testRemainder() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator remainderInts = creator.getMethodCreator("remainderInts", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1 = remainderInts.load(2);
            ResultHandle int2 = remainderInts.load(3);
            remainderInts.returnValue(remainderInts.remainder(int1, int2));

            MethodCreator remainderIntsImproper = creator.getMethodCreator("remainderIntsImproper", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle int1imp = remainderIntsImproper.load(17);
            ResultHandle int2imp = remainderIntsImproper.load(5);
            remainderIntsImproper.returnValue(remainderIntsImproper.remainder(int1imp, int2imp));

            MethodCreator remainderLongs = creator.getMethodCreator("remainderLongs", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1 = remainderLongs.load(6L);
            ResultHandle long2 = remainderLongs.load(4L);
            remainderLongs.returnValue(remainderLongs.remainder(long1, long2));

            MethodCreator remainderLongsNegative = creator.getMethodCreator("remainderLongsNegative", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle long1Negative = remainderLongsNegative.load(-7L);
            ResultHandle long2Negative = remainderLongsNegative.load(4L);
            remainderLongsNegative.returnValue(remainderLongsNegative.remainder(long1Negative, long2Negative));

            MethodCreator remainderFloats = creator.getMethodCreator("remainderFloats", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1 = remainderFloats.load(12.0F);
            ResultHandle float2 = remainderFloats.load(7.0F);
            remainderFloats.returnValue(remainderFloats.remainder(float1, float2));

            MethodCreator remainderFloatsNegative = creator.getMethodCreator("remainderFloatsNegative", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle float1Negative = remainderFloatsNegative.load(8.0F);
            ResultHandle float2Negative = remainderFloatsNegative.load(-3.0F);
            remainderFloatsNegative.returnValue(remainderFloatsNegative.remainder(float1Negative, float2Negative));

            MethodCreator remainderDoubles = creator.getMethodCreator("remainderDoubles", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1 = remainderDoubles.load(10.0D);
            ResultHandle double2 = remainderDoubles.load(8.0D);
            remainderDoubles.returnValue(remainderDoubles.remainder(double1, double2));

            MethodCreator remainderDoublesNegative = creator.getMethodCreator("remainderDoublesNegative", Object.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC);
            ResultHandle double1Negative = remainderDoublesNegative.load(-13.0D);
            ResultHandle double2Negative = remainderDoublesNegative.load(-5.0D);
            remainderDoublesNegative.returnValue(remainderDoublesNegative.remainder(double1Negative, double2Negative));
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertEquals(2, clazz.getMethod("remainderInts").invoke(null));
        assertEquals(2, clazz.getMethod("remainderIntsImproper").invoke(null));
        assertEquals(2L, clazz.getMethod("remainderLongs").invoke(null));
        assertEquals(-3L, clazz.getMethod("remainderLongsNegative").invoke(null));
        assertEquals(5.0F, clazz.getMethod("remainderFloats").invoke(null));
        assertEquals(2.0F, clazz.getMethod("remainderFloatsNegative").invoke(null));
        assertEquals(2.0, clazz.getMethod("remainderDoubles").invoke(null));
        assertEquals(-3.0, clazz.getMethod("remainderDoublesNegative").invoke(null));
    }
}
