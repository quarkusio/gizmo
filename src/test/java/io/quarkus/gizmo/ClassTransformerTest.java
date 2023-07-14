package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.function.Function;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassTransformerTest {

    @Test
    public void testTransfomer() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());

        String className = "com/MyTest";
        String myInterfaceName = MyInterface.class.getName().replace('.', '/');
        String objectName = "java/lang/Object";

        ClassWriter cv = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cv.visit(Opcodes.V11, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, objectName,
                new String[] { myInterfaceName });
        cv.visitSource(null, null);

        assertThrows(NullPointerException.class, () -> new ClassTransformer(className, null));
        ClassTransformer classTransformer = new ClassTransformer(className, cv);

        // private String latVal;
        FieldCreator lastValue = classTransformer.addField(FieldDescriptor.of(className, "lastVal", String.class));
        lastValue.setModifiers(ACC_PRIVATE);

        // public String toStr;
        FieldCreator toStr = classTransformer.addField("toStr", String.class);
        toStr.setModifiers(ACC_PUBLIC);

        // public MyTest() {
        //    this.toStr = "Baf!";
        // }
        MethodCreator constructor = classTransformer.addMethod(MethodDescriptor.ofConstructor(className));
        constructor.invokeSpecialMethod(MethodDescriptor.ofConstructor(Object.class), constructor.getThis());
        constructor.writeInstanceField(toStr.getFieldDescriptor(), constructor.getThis(), constructor.load("Baf!"));
        constructor.returnVoid();

        // public final String transform(String val) {
        //    if (lastVal != null && lastVal.equals("bar")) {
        //       return "baZ";
        //    }
        //    lastVal = val;
        //    if (val.equals("bar")) {
        //       return val;
        //    }
        //    return val.toUpperCase();
        // }
        MethodDescriptor transformDescriptor = MethodDescriptor.ofMethod(className, "transform", String.class, String.class);
        MethodCreator transform = classTransformer.addMethod(transformDescriptor);
        transform.setModifiers(ACC_PUBLIC | ACC_FINAL);

        // Method already added
        assertThrows(IllegalStateException.class, () -> classTransformer.addMethod(transformDescriptor));

        ResultHandle lastVal = transform.readInstanceField(lastValue.getFieldDescriptor(), transform.getThis());
        BytecodeCreator isLastValNotNull = transform.ifNotNull(lastVal).trueBranch();
        BytecodeCreator isLastValBar = isLastValNotNull
                .ifTrue(Gizmo.equals(isLastValNotNull, lastVal, isLastValNotNull.load("bar")))
                .trueBranch();
        isLastValBar.returnValue(isLastValBar.load("baZ"));

        transform.writeInstanceField(lastValue.getFieldDescriptor(), transform.getThis(), transform.getMethodParam(0));

        BytecodeCreator isBar = transform.ifTrue(Gizmo.equals(transform, transform.getMethodParam(0), transform.load("bar")))
                .trueBranch();
        isBar.returnValue(transform.getMethodParam(0));

        ResultHandle ret = transform.invokeVirtualMethod(MethodDescriptor.ofMethod(String.class, "toUpperCase", String.class),
                transform.getMethodParam(0));

        // Unsupported operations
        assertThrows(UnsupportedOperationException.class, () -> transform.createFunction(Function.class));
        assertThrows(UnsupportedOperationException.class, () -> transform.enumSwitch(null, null));

        transform.returnValue(ret);

        // public String toString() {
        //    return toStr; 
        // }
        MethodCreator toString = classTransformer.addMethod("toString", String.class);
        toString.returnValue(toString.readInstanceField(toStr.getFieldDescriptor(), toString.getThis()));

        classTransformer.close();

        cv.visitEnd();
        cl.write(className, cv.toByteArray());

        Class<?> clazz = cl.loadClass("com.MyTest");
        assertTrue(clazz.isSynthetic());
        MyInterface myInterface = (MyInterface) clazz.getDeclaredConstructor().newInstance();
        assertEquals("Baf!", myInterface.toString());

        Method transformMethod = clazz.getMethod("transform", String.class);
        assertTrue(Modifier.isFinal(transformMethod.getModifiers()));
        Field lastValField = clazz.getDeclaredField("lastVal");
        assertNotNull(lastValField);
        assertTrue(Modifier.isPrivate(lastValField.getModifiers()));
        Field toStrField = clazz.getDeclaredField("toStr");
        assertNotNull(toStrField);
        assertTrue(Modifier.isPublic(toStrField.getModifiers()));

        assertEquals("FOO", myInterface.transform("foo"));
        assertEquals("bar", myInterface.transform("bar"));
        assertEquals("baZ", myInterface.transform("foo"));
        assertEquals("baZ", myInterface.transform("bar"));
    }

    @Test
    public void testTransformationOfExistingClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cv = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cr.accept(cv, 0);
        try (ClassTransformer classTransformer = new ClassTransformer(className, cv)) {
            MethodCreator toString = classTransformer.addMethod("toString", String.class);
            toString.returnValue(
                    toString.readInstanceField(FieldDescriptor.of(className, "val", String.class), toString.getThis()));
        }
        cv.visitEnd();
        cl.write(className, cv.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        Object myTest = clazz.getDeclaredConstructor().newInstance();
        assertEquals("test", myTest.toString());
    }

    public static class MyTest {

        final String val = "test";

    }

}
