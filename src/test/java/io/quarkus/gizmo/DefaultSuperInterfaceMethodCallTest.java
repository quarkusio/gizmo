package io.quarkus.gizmo;

import java.util.function.IntSupplier;

import org.junit.Assert;
import org.junit.Test;

public class DefaultSuperInterfaceMethodCallTest {

    @Test
    public void test() throws Exception {
        final TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(IntSupplier.class, InterfaceWithDefaultMethod.class).build()) {

            MethodCreator override = creator.getMethodCreator("whatever", int.class);
            override.returnValue(override.invokeSpecialInterfaceMethod(MethodDescriptor.ofMethod(
                    InterfaceWithDefaultMethod.class, "whatever", int.class), override.getThis()));

            MethodCreator bc = creator.getMethodCreator("getAsInt", int.class);
            bc.returnValue(bc.invokeVirtualMethod(override.getMethodDescriptor(), bc.getThis()));
        }
        Class<? extends IntSupplier> clazz = cl.loadClass("com.MyTest").asSubclass(IntSupplier.class);
        IntSupplier supplier = clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(13, supplier.getAsInt());
    }
}
