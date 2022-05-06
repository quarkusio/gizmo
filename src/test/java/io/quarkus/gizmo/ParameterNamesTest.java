package io.quarkus.gizmo;

import org.junit.Test;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParameterNamesTest {
    @Test
    public void test() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator method = creator.getMethodCreator("staticMethod", void.class, String.class)
                    .setModifiers(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) {
                AnnotationCreator annotation = method.getParameterAnnotations(0).addAnnotation(MyAnnotation.class);
                annotation.addValue("value", "static method");
                annotation.addValue("enumVal", MyEnum.NO);
                method.setParameterNames(new String[] {"staticMethodParameter"});
                method.returnValue(null);
            }

            try (MethodCreator method = creator.getMethodCreator("instanceMethod", void.class, String.class)) {
                AnnotationCreator annotation = method.getParameterAnnotations(0).addAnnotation(MyAnnotation.class);
                annotation.addValue("value", "instance method");
                annotation.addValue("enumVal", MyEnum.YES);
                method.setParameterNames(new String[] {"instanceMethodParameter"});
                method.returnValue(null);
            }
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
        {
            Method method = clazz.getMethod("staticMethod", String.class);
            assertEquals(1, method.getParameterCount());
            assertEquals(String.class, method.getParameterTypes()[0]);
            Parameter parameter = method.getParameters()[0];
            assertTrue(parameter.isAnnotationPresent(MyAnnotation.class));
            MyAnnotation annotation = parameter.getAnnotation(MyAnnotation.class);
            assertEquals("static method", annotation.value());
            assertEquals(MyEnum.NO, annotation.enumVal());
            assertEquals("staticMethodParameter", parameter.getName());
        }
        {
            Method method = clazz.getMethod("instanceMethod", String.class);
            assertEquals(1, method.getParameterCount());
            assertEquals(String.class, method.getParameterTypes()[0]);
            Parameter parameter = method.getParameters()[0];
            assertTrue(parameter.isAnnotationPresent(MyAnnotation.class));
            MyAnnotation annotation = parameter.getAnnotation(MyAnnotation.class);
            assertEquals("instance method", annotation.value());
            assertEquals(MyEnum.YES, annotation.enumVal());
            assertEquals("instanceMethodParameter", parameter.getName());
        }
    }
}
