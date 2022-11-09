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
import org.objectweb.asm.Opcodes;

public class InterfaceTestCase {

    @Test
    public void testSimpleInterface() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyInterface").setInterface(true).build()) {
        }
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces("com.MyInterface").build()) {
        }
        Class<?> intf = cl.loadClass("com.MyInterface");
        Assert.assertTrue(intf.isInterface());
        Assert.assertTrue(intf.isSynthetic());
        Class<?> clazz = cl.loadClass("com.MyTest");
        Assert.assertTrue(clazz.isSynthetic());
        Assert.assertArrayEquals(new Class[]{intf}, clazz.getInterfaces());
    }

    @Test
    public void testInterface() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyInterface").setInterface(true).build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            method.setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_ABSTRACT);
        }
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces("com.MyInterface").build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);

            Gizmo.StringBuilderGenerator strBuilder = Gizmo.newStringBuilder(method);
            strBuilder.append(method.getMethodParam(0));
            strBuilder.append(method.load("-impl"));

            method.returnValue(strBuilder.callToString());
        }
        Class<?> intf = cl.loadClass("com.MyInterface");
        Assert.assertTrue(intf.isInterface());
        Assert.assertTrue(intf.isSynthetic());
        Class<?> clazz = cl.loadClass("com.MyTest");
        Assert.assertTrue(clazz.isSynthetic());
        Assert.assertArrayEquals(new Class[]{intf}, clazz.getInterfaces());

        Object inst = clazz.getDeclaredConstructor().newInstance();
        intf.cast(inst);

        String actual = (String) clazz.getDeclaredMethod("transform", String.class).invoke(inst, "foo");
        Assert.assertEquals("foo-impl", actual);
    }

}
