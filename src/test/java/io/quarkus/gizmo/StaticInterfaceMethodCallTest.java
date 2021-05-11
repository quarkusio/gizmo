package io.quarkus.gizmo;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.IntSupplier;

public class StaticInterfaceMethodCallTest {

    @Test
    public void testStaticInterfaceFuction() throws Exception {
        final TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(IntSupplier.class).build()) {
            MethodCreator bc = creator.getMethodCreator("getAsInt", int.class);
            bc.returnValue(
                    bc.invokeStaticInterfaceMethod(MethodDescriptor.ofMethod(InterfaceWithMethod.class, "whatever", int.class)));
        }
        Class<? extends IntSupplier> clazz = cl.loadClass("com.MyTest").asSubclass(IntSupplier.class);
        IntSupplier supplier = clazz.getDeclaredConstructor().newInstance();
        Assert.assertEquals(42, supplier.getAsInt());
    }
}
