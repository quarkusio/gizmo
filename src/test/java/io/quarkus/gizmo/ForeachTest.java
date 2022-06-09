package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;

import java.util.function.Supplier;

import org.junit.Test;

public class ForeachTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testForEachLoop() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            ResultHandle list = Gizmo.listOperations(method).of(method.load("foo"), method.load("bar"));

            ForEachLoop loop = method.forEach(list);

            BytecodeCreator block = loop.block();
            block.invokeVirtualMethod(sbAppend, sb, Gizmo.toString(block, loop.element()));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));

            method.returnValue(Gizmo.toString(method, sb));
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("foo-bar-", myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testForEachLoopContinue() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(0));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            ResultHandle list = Gizmo.listOperations(method).of(method.load("foo"), method.load("bar"), method.load("baz"));

            ForEachLoop loop = method.forEach(list);

            BytecodeCreator block = loop.block();
            // increment counter
            block.assign(val, block.invokeStaticMethod(
                    MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, block.load(1)));
            // skip the second element
            loop.doContinue(block.ifIntegerEqual(val, block.load(2)).trueBranch());
            block.invokeVirtualMethod(sbAppend, sb, Gizmo.toString(block, loop.element()));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));

            method.returnValue(Gizmo.toString(method, sb));
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("foo-baz-", myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void ForEachLoopBreak() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            AssignableResultHandle val = method.createVariable(int.class);
            method.assign(val, method.load(0));
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);

            ResultHandle list = Gizmo.listOperations(method).of(method.load("foo"), method.load("bar"));

            ForEachLoop loop = method.forEach(list);

            BytecodeCreator block = loop.block();
            // increment counter
            block.assign(val, block.invokeStaticMethod(
                    MethodDescriptor.ofMethod(Math.class, "addExact", int.class, int.class, int.class), val, block.load(1)));
            block.invokeVirtualMethod(sbAppend, sb, Gizmo.toString(block, loop.element()));
            block.invokeVirtualMethod(sbAppend, sb, block.load("-"));
            // break after the first element
            loop.doBreak(block.ifIntegerEqual(val, block.load(1)).trueBranch());
            method.returnValue(Gizmo.toString(method, sb));
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("foo-", myInterface.get());
    }

}
