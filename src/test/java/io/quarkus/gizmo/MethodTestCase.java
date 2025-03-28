package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Method;
import java.util.function.Supplier;

import org.junit.Test;

public class MethodTestCase {
    @Test
    public void testMethodCheckerAutoAddReturnValCtor() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator ctor = creator.getMethodCreator(MethodDescriptor.ofConstructor("com.MyTest", String.class));
            ctor.invokeSpecialMethod(
                    MethodDescriptor.ofConstructor("java.lang.Object"),
                    ctor.getThis());
            //missing method.returnValue(null);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Object obj = clazz.getDeclaredConstructor(String.class).newInstance("foo");
        assertNotNull(obj);
    }
    @Test
    public void testMethodCheckerAutoAddSuperCtor() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator ctor = creator.getMethodCreator(MethodDescriptor.ofConstructor("com.MyTest", String.class));
            //missing invokeSpecial of super()
            //missing method.returnValue(null);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Object obj = clazz.getDeclaredConstructor(String.class).newInstance("foo");
        assertNotNull(obj);
    }
    @Test
    public void testMethodCheckerAutoAddSuperCtorWithReturn() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            MethodCreator ctor = creator.getMethodCreator(MethodDescriptor.ofConstructor("com.MyTest", String.class));
            //missing invokeSpecial of super()
            ctor.returnValue(null);
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Object obj = clazz.getDeclaredConstructor(String.class).newInstance("foo");
        assertNotNull(obj);
    }
    @Test
    public void testMethodCheckerAutoAddReturnValVoidMethod() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                //missing methodCreator.returnValue(null);
            }
        }
        Class<?> testClass = cl.loadClass("com.MyTest");
        Object o = testClass.getConstructor().newInstance();
        Method method = testClass.getMethod("test");
        method.invoke(o);
    }
    @Test
    public void testMethodCheckerMissingReturnVal() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            //missing method.returnValue(method.loadNull());
        } catch (RuntimeException e) {
            assertEquals("Missing returnValue for method [declaringClassName=com/MyTest, methodDescriptor=MethodDescriptor{name='get', returnType='Ljava/lang/Object;', parameterTypes=[]}]", e.getMessage());
        }
    }
}
