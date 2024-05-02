package io.quarkus.gizmo;

import java.io.IOException;
import java.util.function.Supplier;

import org.junit.Test;

public class InvalidScopeTestCase {

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidScope() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle handle = null;
            try (TryBlock tryBlock = method.tryBlock()) {
                handle = tryBlock.load(100.f);
                tryBlock.addCatch(IOException.class);
            }
            method.returnValue(handle);
        }
    }
}
