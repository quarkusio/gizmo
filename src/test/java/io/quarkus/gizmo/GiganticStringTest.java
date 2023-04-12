package io.quarkus.gizmo;

import static io.quarkus.gizmo.BytecodeCreatorImpl.MAX_STRING_LENGTH;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

public class GiganticStringTest {

    @Test
    public void exactCutoffSize() throws Exception {
        doTest(MAX_STRING_LENGTH);
    }

    @Test
    public void cutoffSizePlus1() throws Exception {
        doTest(MAX_STRING_LENGTH + 1);
    }

    @Test
    public void cutoffSizePlusSeven() throws Exception {
        doTest(MAX_STRING_LENGTH + 7);
    }

    @Test
    public void tenTimesTheCutoffSize() throws Exception {
        doTest(MAX_STRING_LENGTH * 10);
    }

    @Test
    public void thirteenTimesTheCutoffSizePlus13() throws Exception {
        doTest((MAX_STRING_LENGTH * 10) + 13);
    }

    private void doTest(int size)
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class).build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            char[] chars = new char[size];
            Arrays.fill(chars, 'a');
            chars[0] = 'f';
            chars[size - 1] = 'l';

            String str = new String(chars);

            method.returnValue(method.load(str));
        }
        Class<?> clazz = cl.loadClass("com.MyTest");
        Supplier myInterface = (Supplier) clazz.getDeclaredConstructor().newInstance();
        Object o = myInterface.get();
        Assert.assertEquals(String.class, o.getClass());
        String s = o.toString();
        Assert.assertEquals(size, s.length());
        Assert.assertEquals('f', s.charAt(0));
        Assert.assertEquals('l', s.charAt(size - 1));
        Assert.assertEquals('a', s.charAt(size / 2));
    }
}
