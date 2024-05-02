package io.quarkus.gizmo;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class BoxingTestCase {

    @Test
    public void testAutoBoxConstantDouble() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            method.returnValue(method.load(10.0d));
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(Double.class, o.getClass());
        double val = (Double) o;
        Assert.assertEquals(10.0d, val, 0);

    }

    @Test
    public void testAutoBoxConstantFloat() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            method.returnValue(method.load(10.0f));
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(Float.class, o.getClass());
        double val = (Float) o;
        Assert.assertEquals(10.0f, val, 0);

    }
}
