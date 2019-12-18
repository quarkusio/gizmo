package io.quarkus.gizmo;

import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class ScopeTestCase {


    @Test
    public void testSimpleScopes() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class).build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            AssignableResultHandle val = method.createVariable(String.class);
            BytecodeCreator scope = method.createScope();
            method.returnValue(val);

            ResultHandle equalsResult = scope.invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "equals", boolean.class, Object.class), scope.getMethodParam(0), scope.load("TEST"));
            BranchResult branch = scope.ifNonZero(equalsResult);
            branch.trueBranch().assign(val, scope.load("TRUE BRANCH"));
            branch.trueBranch().breakScope(scope);
            scope.assign(val, scope.getMethodParam(0));
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("PARAM", myInterface.transform("PARAM"));
        Assert.assertEquals("TRUE BRANCH", myInterface.transform("TEST"));
    }

    @Test
    public void testContinueScope() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(10));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));

            BytecodeCreator loop = method.createScope();
            method.returnValue(method.invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "toString", String.class), sb));

            BranchResult branch = loop.ifNonZero(val);
            BytecodeCreator tb = branch.trueBranch();
            tb.invokeVirtualMethod(MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, String.class), sb, tb.invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "toString", String.class, int.class), val));
            tb.invokeVirtualMethod(MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class, String.class), sb, tb.load("-"));
            tb.assign(val, tb.invokeStaticMethod(MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, tb.load(-1)));
            tb.continueScope(loop);

            branch.falseBranch().breakScope(loop);
        }
        Supplier myInterface = (Supplier) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("10-9-8-7-6-5-4-3-2-1-", myInterface.get());
    }
}
