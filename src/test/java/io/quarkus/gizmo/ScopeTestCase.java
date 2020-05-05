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

    @Test
    public void testWhileLoop() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(10));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            WhileLoop loop = method.whileLoop(bc -> bc.ifNonZero(val));
            BytecodeCreator block = loop.block();
            block.invokeVirtualMethod(sbAppend, sb, block
                    .invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "toString", String.class, int.class), val));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));
            block.assign(val, block.invokeStaticMethod(
                    MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, block.load(-1)));

            method.returnValue(
                    method.invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "toString", String.class), sb));
        }
        Supplier myInterface = (Supplier) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("10-9-8-7-6-5-4-3-2-1-", myInterface.get());
    }
    
    @Test
    public void testWhileLoopContinue() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(11));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            WhileLoop loop = method.whileLoop(bc -> bc.ifIntegerGreaterThan(val, bc.load(1)));
            BytecodeCreator block = loop.block();
            // decrement the value
            block.assign(val, block.invokeStaticMethod(
                    MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, block.load(-1)));
            // skip number 5
            block.ifIntegerEqual(val, block.load(5)).trueBranch().continueScope(loop.scope());
            block.invokeVirtualMethod(sbAppend, sb, block
                    .invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "toString", String.class, int.class), val));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));
            
            method.returnValue(
                    method.invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "toString", String.class), sb));
        }
        Supplier myInterface = (Supplier) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("10-9-8-7-6-4-3-2-1-", myInterface.get());
    }
    
    @Test
    public void testWhileLoopBreak() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(11));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            WhileLoop loop = method.whileLoop(bc -> bc.ifIntegerGreaterThan(val, bc.load(1)));
            BytecodeCreator block = loop.block();
            // decrement the value
            block.assign(val, block.invokeStaticMethod(
                    MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, block.load(-1)));
            // break if number 5
            block.ifIntegerEqual(val, block.load(5)).trueBranch().breakScope(loop.scope());
            block.invokeVirtualMethod(sbAppend, sb, block
                    .invokeStaticMethod(MethodDescriptor.ofMethod(Integer.class, "toString", String.class, int.class), val));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));

            method.returnValue(
                    method.invokeVirtualMethod(MethodDescriptor.ofMethod(Object.class, "toString", String.class), sb));
        }
        Supplier myInterface = (Supplier) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("10-9-8-7-6-", myInterface.get());
    }
    
}
