package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import java.util.List;
import java.util.function.Function;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

public class ClassTransformerTest {

    @Test
    public void addMethodsAndFieldsToVisitor() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());

        String className = "com/MyTest";
        String myInterfaceName = MyInterface.class.getName().replace('.', '/');
        String objectName = "java/lang/Object";

        ClassTransformer classTransformer = new ClassTransformer(className);

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

        transform.returnValue(ret);

        // public String toString() {
        //    return toStr;
        // }
        MethodCreator toString = classTransformer.addMethod("toString", String.class);
        toString.returnValue(toString.readInstanceField(toStr.getFieldDescriptor(), toString.getThis()));

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = classTransformer.applyTo(cw);

        cv.visit(Opcodes.V11, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, objectName,
                new String[] { myInterfaceName });
        cv.visitSource(null, null);

        cv.visitEnd();
        cl.write(className, cw.toByteArray());

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
    public void addMethodToExistingClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassTransformer classTransformer = new ClassTransformer(className);
        MethodCreator toString = classTransformer.addMethod("toString", String.class);
        toString.returnValue(
                toString.readInstanceField(FieldDescriptor.of(className, "val", String.class), toString.getThis()));

        ClassVisitor cv = classTransformer.applyTo(cw);

        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        Object myTest = clazz.getDeclaredConstructor().newInstance();
        assertEquals("test", myTest.toString());
    }

    @Test
    public void renameMethodInExistingClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassTransformer classTransformer = new ClassTransformer(className);
        classTransformer.modifyMethod("test", String.class, int.class).rename("test$");

        MethodCreator test = classTransformer.addMethod("test", String.class, int.class);
        ResultHandle original = test.invokeVirtualMethod(MethodDescriptor.ofMethod(className, "test$", String.class, int.class),
                test.getThis(), test.getMethodParam(0));
        ResultHandle result = Gizmo.newStringBuilder(test).append("transformed: ").append(original).callToString();
        test.returnValue(result);

        ClassVisitor cv = classTransformer.applyTo(cw);

        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        Object myTest = clazz.getDeclaredConstructor().newInstance();
        Method testMethod = clazz.getMethod("test", int.class);
        assertEquals("transformed: test42", testMethod.invoke(myTest, 42));

        assertNotNull(clazz.getMethod("test$", int.class));
    }

    @Test
    public void changeModifiersOfExistingClass() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassTransformer classTransformer = new ClassTransformer(className);
        classTransformer.addModifiers(Modifier.FINAL);
        classTransformer.removeModifiers(Modifier.PUBLIC);
        classTransformer.modifyField("val", String.class)
                .addModifiers(Modifier.PUBLIC)
                .removeModifiers(Modifier.FINAL);
        classTransformer.modifyMethod("test", String.class, int.class)
                .addModifiers(Modifier.FINAL)
                .removeModifiers(Modifier.PUBLIC);

        ClassVisitor cv = classTransformer.applyTo(cw);

        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        assertFalse(Modifier.isPublic(clazz.getModifiers()));
        assertTrue(Modifier.isStatic(clazz.getModifiers()));
        assertTrue(Modifier.isFinal(clazz.getModifiers()));
        assertTrue(Modifier.isPublic(clazz.getDeclaredField("val").getModifiers()));
        assertFalse(Modifier.isStatic(clazz.getDeclaredField("val").getModifiers()));
        assertFalse(Modifier.isFinal(clazz.getDeclaredField("val").getModifiers()));
        assertFalse(Modifier.isPublic(clazz.getDeclaredMethod("test", int.class).getModifiers()));
        assertFalse(Modifier.isStatic(clazz.getDeclaredMethod("test", int.class).getModifiers()));
        assertTrue(Modifier.isFinal(clazz.getDeclaredMethod("test", int.class).getModifiers()));
    }

    @Test
    public void addInterfaceAndMethodWithEnumSwitch() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

        ClassTransformer classTransformer = new ClassTransformer(className);
        classTransformer.addInterface(Function.class);
        MethodCreator apply = classTransformer.addMethod("apply", Object.class, Object.class);
        // switch (param) {
        //     case YES, NO -> return param.toString();
        //     case UNKNOWN -> return "?";
        //     default: -> return null;
        // }
        Switch.EnumSwitch<MyEnum> s = apply.enumSwitch(apply.getMethodParam(0), MyEnum.class);
        s.caseOf(List.of(MyEnum.YES, MyEnum.NO), bc -> {
            bc.returnValue(Gizmo.toString(bc, apply.getMethodParam(0)));
        });
        s.caseOf(MyEnum.UNKNOWN, bc -> {
            bc.returnValue(bc.load("?"));
        });
        s.defaultCase(bc -> bc.returnNull());

        ClassVisitor cv = classTransformer.applyTo(cw);

        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        Function<MyEnum, String> myTest = (Function<MyEnum, String>) clazz.getDeclaredConstructor().newInstance();
        assertEquals("YES", myTest.apply(MyEnum.YES));
        assertEquals("NO", myTest.apply(MyEnum.NO));
        assertEquals("?", myTest.apply(MyEnum.UNKNOWN));
    }

    @Test
    public void alreadyUsedVisitor() throws Exception {
        String className = "com/MyTest";
        String myInterfaceName = MyInterface.class.getName().replace('.', '/');
        String objectName = "java/lang/Object";

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V11, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, objectName,
                new String[] { myInterfaceName });
        cw.visitSource(null, null);

        ClassTransformer classTransformer = new ClassTransformer(className);
        MethodCreator toString = classTransformer.addMethod("toString", String.class);
        toString.returnValue(toString.load("test"));

        ClassVisitor cv = classTransformer.applyTo(cw);
        assertThrows(IllegalStateException.class, () -> {
            cv.visitEnd();
        });
    }

    @Test
    public void testRemoveField() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        String objectName = "java/lang/Object";
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V11, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, objectName,
                new String[] {});
        cw.visitSource(null, null);

        ClassTransformer classTransformer = new ClassTransformer(className);

        classTransformer.removeField("val", String.class);
        assertThrows(NullPointerException.class, () -> {
            classTransformer.removeField(null);
        });
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.removeField("val", String.class);
        });
        assertTrue(ise.getMessage().startsWith("Field already removed"));

        classTransformer.modifyField("modified", boolean.class).rename("deifidom");
        ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.removeField("modified", boolean.class);
        });
        assertTrue(ise.getMessage().startsWith("Modified field cannot be removed"));

        ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.modifyField("val", String.class);
        });
        assertTrue(ise.getMessage().startsWith("Removed field cannot be modified"));

        ClassVisitor cv = classTransformer.applyTo(cw);
        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        assertThrows(NoSuchFieldException.class, () -> {
            clazz.getDeclaredField("val");
        });
    }

    @Test
    public void testRemoveMethod() throws Exception {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        String className = MyTest.class.getName().replace('.', '/');
        String objectName = "java/lang/Object";
        ClassReader cr = new ClassReader("io.quarkus.gizmo.ClassTransformerTest$MyTest");
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cw.visit(Opcodes.V11, ACC_PUBLIC | ACC_SUPER | ACC_SYNTHETIC, className, null, objectName,
                new String[] {});
        cw.visitSource(null, null);

        ClassTransformer classTransformer = new ClassTransformer(className);

        MethodDescriptor testMethod = MethodDescriptor.ofMethod(className, "test", String.class, int.class);
        MethodDescriptor modifiedMethod = MethodDescriptor.ofMethod(className, "modified", String.class, int.class);

        classTransformer.removeMethod(testMethod);
        assertThrows(NullPointerException.class, () -> {
            classTransformer.removeMethod(null);
        });
        IllegalStateException ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.removeMethod("test", String.class, int.class);
        });
        assertTrue(ise.getMessage().startsWith("Method already removed"));

        classTransformer.modifyMethod(modifiedMethod).rename("deifidom");
        ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.removeMethod(modifiedMethod);
        });
        assertTrue(ise.getMessage().startsWith("Modified method cannot be removed"));

        ise = assertThrows(IllegalStateException.class, () -> {
            classTransformer.modifyMethod(testMethod);
        });
        assertTrue(ise.getMessage().startsWith("Removed method cannot be modified"));

        ClassVisitor cv = classTransformer.applyTo(cw);
        cr.accept(cv, 0);
        cl.write(className, cw.toByteArray());

        Class<?> clazz = cl.loadClass(MyTest.class.getName());
        assertThrows(NoSuchMethodException.class, () -> {
            clazz.getDeclaredMethod("test", int.class);
        });
    }

    public static class MyTest {

        final String val = "test";
        boolean modified = false;

        public String test(int arg) {
            return val + arg;
        }

        public String modified(int arg) {
            return val + arg;
        }

    }

}
