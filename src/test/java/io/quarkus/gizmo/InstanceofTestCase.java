package io.quarkus.gizmo;

import org.junit.Assert;
import org.junit.Test;

public class InstanceofTestCase {

    @Test
    public void testInstanceof() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(BooleanInterface.class).build()) {
            MethodCreator method = creator.getMethodCreator("test", boolean.class, Object.class);
            method.returnValue(method.instanceOf(method.getMethodParam(0), String.class));

        }
        BooleanInterface myInterface = (BooleanInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertTrue(myInterface.test("PARAM"));
        Assert.assertFalse(myInterface.test(this));
    }

}
