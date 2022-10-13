package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Test;

public class IfStatementTestCase {

    @Test
    public void testIfStatement() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle equalsResult = method.invokeVirtualMethod(
                    MethodDescriptor.ofMethod(Object.class, "equals", boolean.class, Object.class), method.getMethodParam(0),
                    method.load("TEST"));
            BranchResult branch = method.ifNonZero(equalsResult);
            branch.trueBranch().returnValue(branch.trueBranch().load("TRUE BRANCH"));
            branch.falseBranch().returnValue(method.getMethodParam(0));

        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("PARAM", myInterface.transform("PARAM"));
        Assert.assertEquals("TRUE BRANCH", myInterface.transform("TEST"));
    }

    @Test
    public void testIfNull() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle nullHandle = method.loadNull();
            BranchResult branch = method.ifNull(nullHandle);
            branch.trueBranch().returnValue(branch.trueBranch().load("TRUE"));
            branch.falseBranch().returnValue(branch.falseBranch().load("FALSE"));
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("TRUE", myInterface.transform("TEST"));
    }

    @Test
    public void testIfGreaterThanZero() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle lengthHandle = method.invokeVirtualMethod(MethodDescriptor.ofMethod(String.class, "length", int.class),
                    method.getMethodParam(0));
            BranchResult branch = method.ifGreaterThanZero(lengthHandle);
            branch.trueBranch().returnValue(branch.trueBranch().load("TRUE"));
            branch.falseBranch().returnValue(branch.falseBranch().load("FALSE"));
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("TRUE", myInterface.transform("TEST"));
        Assert.assertEquals("FALSE", myInterface.transform(""));
    }

    @Test
    public void testIfIntegerEqual() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle lengthHandle = method.invokeVirtualMethod(MethodDescriptor.ofMethod(String.class, "length", int.class),
                    method.getMethodParam(0));
            BranchResult branch = method.ifIntegerEqual(lengthHandle, method.load(3));
            branch.trueBranch().returnValue(branch.trueBranch().load("TRUE"));
            branch.falseBranch().returnValue(branch.falseBranch().load("FALSE"));
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("TRUE", myInterface.transform("TES"));
        Assert.assertEquals("FALSE", myInterface.transform("TEST"));
    }

    @Test
    public void testIfIntegerLessThan() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(MyInterface.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("transform", String.class, String.class);
            ResultHandle lengthHandle = method.invokeVirtualMethod(MethodDescriptor.ofMethod(String.class, "length", int.class),
                    method.getMethodParam(0));
            BranchResult branch = method.ifIntegerLessThan(lengthHandle, method.load(3));
            branch.trueBranch().returnValue(branch.trueBranch().load("TRUE"));
            branch.falseBranch().returnValue(branch.falseBranch().load("FALSE"));
        }
        MyInterface myInterface = (MyInterface) cl.loadClass("com.MyTest").newInstance();
        Assert.assertEquals("TRUE", myInterface.transform("T"));
        Assert.assertEquals("FALSE", myInterface.transform("TEST"));
    }

    @Test
    public void testIfReferencesEqual() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(ReferenceEqualsTest.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("referenceEquals", boolean.class, Object.class, Object.class);
            BranchResult branch = method.ifReferencesEqual(method.getMethodParam(0), method.getMethodParam(1));
            branch.trueBranch().returnValue(branch.trueBranch().load(true));
            branch.falseBranch().returnValue(branch.falseBranch().load(false));
        }
        ReferenceEqualsTest myTest = (ReferenceEqualsTest) cl.loadClass("com.MyTest").newInstance();
        Item item1 = new Item(1000);
        Item item2 = new Item(1000);
        assertEquals(item1, item2);
        assertFalse(myTest.referenceEquals(item1, item2));
        assertTrue(myTest.referenceEquals(item1, item1));
    }

    @Test
    public void testIfReferencesNotEqual() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(ReferenceEqualsTest.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("referenceEquals", boolean.class, Object.class, Object.class);
            BranchResult branch = method.ifReferencesNotEqual(method.getMethodParam(0), method.getMethodParam(1));
            branch.trueBranch().returnValue(branch.trueBranch().load(false));
            branch.falseBranch().returnValue(branch.falseBranch().load(true));
        }
        ReferenceEqualsTest myTest = (ReferenceEqualsTest) cl.loadClass("com.MyTest").newInstance();
        Item item1 = new Item(1000);
        Item item2 = new Item(1000);
        assertEquals(item1, item2);
        assertFalse(myTest.referenceEquals(item1, item2));
        assertTrue(myTest.referenceEquals(item1, item1));
    }

    @Test
    public void testLongComparison() throws ReflectiveOperationException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(LongEqualsTest.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("longEquals", boolean.class, long.class, long.class);
            ResultHandle cmp = method.compareLong(method.getMethodParam(0), method.getMethodParam(1));
            BranchResult branch = method.ifZero(cmp);
            branch.trueBranch().returnValue(branch.trueBranch().load(true));
            branch.falseBranch().returnValue(branch.falseBranch().load(false));
        }
        LongEqualsTest myTest = (LongEqualsTest) cl.loadClass("com.MyTest").newInstance();
        assertTrue(myTest.longEquals(1L, 1L));
        assertFalse(myTest.longEquals(1L, 2L));
    }

    @Test
    public void testFloatComparison() throws ReflectiveOperationException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(FloatEqualsTest.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("floatEquals", boolean.class, float.class, float.class);
            ResultHandle cmp = method.compareFloat(method.getMethodParam(0), method.getMethodParam(1), false);
            BranchResult branch = method.ifZero(cmp);
            branch.trueBranch().returnValue(branch.trueBranch().load(true));
            branch.falseBranch().returnValue(branch.falseBranch().load(false));
        }
        FloatEqualsTest myTest = (FloatEqualsTest) cl.loadClass("com.MyTest").newInstance();
        assertTrue(myTest.floatEquals(1.0F, 1.0F));
        assertFalse(myTest.floatEquals(1.0F, 2.0F));
    }

    @Test
    public void testDoubleComparison() throws ReflectiveOperationException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                .interfaces(DoubleEqualsTest.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("doubleEquals", boolean.class, double.class, double.class);
            ResultHandle cmp = method.compareDouble(method.getMethodParam(0), method.getMethodParam(1), false);
            BranchResult branch = method.ifZero(cmp);
            branch.trueBranch().returnValue(branch.trueBranch().load(true));
            branch.falseBranch().returnValue(branch.falseBranch().load(false));
        }
        DoubleEqualsTest myTest = (DoubleEqualsTest) cl.loadClass("com.MyTest").newInstance();
        assertTrue(myTest.doubleEquals(1.0, 1.0));
        assertFalse(myTest.doubleEquals(1.0, 2.0));
    }

    public interface ReferenceEqualsTest {
        boolean referenceEquals(Object obj1, Object obj2);
    }

    public interface LongEqualsTest {
        boolean longEquals(long long1, long long2);
    }

    public interface FloatEqualsTest {
        boolean floatEquals(float float1, float float2);
    }

    public interface DoubleEqualsTest {
        boolean doubleEquals(double double1, double double2);
    }

    public static class Item {

        private int id;

        public Item(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Item other = (Item) obj;
            return id == other.id;
        }

    }

}
