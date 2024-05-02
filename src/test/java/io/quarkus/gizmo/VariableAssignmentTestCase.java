package io.quarkus.gizmo;

import org.junit.Assert;
import org.junit.Test;

public class VariableAssignmentTestCase {

    @Test
    public void testVariableAssignment() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            AssignableResultHandle val = method.createVariable(String.class);
            ResultHandle equalsResult = method.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(Object.class, "equals", boolean.class, Object.class), method.getMethodParam(0),
                    method.load("TEST"));
            BranchResult branch = method.ifNonZero(equalsResult);
            branch.trueBranch().assign(val, method.load("TRUE BRANCH"));
            branch.falseBranch().assign(val, method.getMethodParam(0));

            method.returnValue(val);

        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("PARAM", myInterface.transform("PARAM"));
        Assert.assertEquals("TRUE BRANCH", myInterface.transform("TEST"));
    }

}
