package io.quarkus.gizmo;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.Type;
import org.junit.Test;

public class AnnotationTestCase {

    @Test
    public void testClassAnnotationWithString() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addAnnotationWithString(creator);
        }

        MyAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyAnnotation.class);
        assertEquals("test", annotation.value());
        assertEquals(MyEnum.YES, annotation.enumVal());
    }

    @Test
    public void testClassAnnotationWithStringArray() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addAnnotationWithStringArray(creator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testClassAnnotationWithAnnotationValueArray() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addAnnotationWithStringArrayUsingJandex(creator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testClassFullAnnotationUsingJandex() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addFullAnnotationUsingJandex(creator);
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testClassFullAnnotationUsingNestedCreator() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addFullAnnotationUsingNestedCreator(creator);
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testMethodAnnotationWithString() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addAnnotationWithString(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyAnnotation.class);
        assertEquals("test", annotation.value());
        assertEquals(MyEnum.YES, annotation.enumVal());
    }

    @Test
    public void testMethodAnnotationWithEnum() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                methodCreator.addAnnotation(AnnotationInstance.create(DotName.createSimple(MyAnnotation.class.getName()), null,
                        new AnnotationValue[] {
                                AnnotationValue.createEnumValue("enumVal", DotName.createSimple("io.quarkus.gizmo.MyEnum"),
                                        "NO")
                        }));
                methodCreator.returnValue(null);
            }
        }

        MyAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyAnnotation.class);
        assertEquals(MyEnum.NO, annotation.enumVal());
    }

    @Test
    public void testMethodAnnotationWithStringArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addAnnotationWithStringArray(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodAnnotationWithAnnotationValueArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addAnnotationWithStringArrayUsingJandex(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodFullAnnotationUsingJandex() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addFullAnnotationUsingJandex(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testMethodFullAnnotationUsingNestedCreator() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addFullAnnotationUsingNestedCreator(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testMethodParamAnnotationWithString() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addAnnotationWithString(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyAnnotation.class);
        assertEquals("test", annotation.value());
        assertEquals(MyEnum.YES, annotation.enumVal());
    }

    @Test
    public void testMethodParamAnnotationWithStringArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addAnnotationWithStringArray(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodParamAnnotationWithAnnotationValueArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addAnnotationWithStringArrayUsingJandex(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodParamFullAnnotationUsingJandex() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addFullAnnotationUsingJandex(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testMethodParamFullAnnotationUsingNestedCreator() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addFullAnnotationUsingNestedCreator(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testFieldAnnotationWithString() throws ClassNotFoundException, NoSuchFieldException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            FieldCreator fieldCreator = creator.getFieldCreator("test", String.class);
            addAnnotationWithString(fieldCreator);
        }

        MyAnnotation annotation = cl.loadClass("com.MyTest")
                .getDeclaredField("test")
                .getAnnotation(MyAnnotation.class);
        assertEquals("test", annotation.value());
        assertEquals(MyEnum.YES, annotation.enumVal());
    }

    @Test
    public void testFieldAnnotationWithStringArray() throws ClassNotFoundException, NoSuchFieldException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            FieldCreator fieldCreator = creator.getFieldCreator("test", String.class);
            addAnnotationWithStringArray(fieldCreator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getDeclaredField("test")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testFieldAnnotationWithAnnotationValueArray() throws ClassNotFoundException, NoSuchFieldException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            FieldCreator fieldCreator = creator.getFieldCreator("test", String.class);
            addAnnotationWithStringArray(fieldCreator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getDeclaredField("test")
                .getAnnotation(MyArrayAnnotation.class);
        assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testFieldFullAnnotationUsingJandex() throws ClassNotFoundException, NoSuchFieldException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            FieldCreator fieldCreator = creator.getFieldCreator("test", String.class);
            addFullAnnotationUsingJandex(fieldCreator);
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getDeclaredField("test")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    @Test
    public void testFieldFullAnnotationUsingNestedCreator() throws ClassNotFoundException, NoSuchFieldException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            FieldCreator fieldCreator = creator.getFieldCreator("test", String.class);
            addFullAnnotationUsingNestedCreator(fieldCreator);
        }

        MyFullAnnotation annotation = cl.loadClass("com.MyTest")
                .getDeclaredField("test")
                .getAnnotation(MyFullAnnotation.class);
        verifyFullAnnotation(annotation);
    }

    private void addAnnotationWithString(AnnotatedElement element) {
        AnnotationCreator annotationCreator = element.addAnnotation(MyAnnotation.class);
        annotationCreator.addValue("value", "test");
        annotationCreator.addValue("enumVal", MyEnum.YES);
    }

    private void addAnnotationWithStringArray(AnnotatedElement element) {
        element.addAnnotation(MyArrayAnnotation.class).addValue("value", new String[] { "test" });
    }

    private void addAnnotationWithStringArrayUsingJandex(AnnotatedElement element) {
        AnnotationInstance annotation = AnnotationInstance.builder(MyArrayAnnotation.class)
                .value(new String[] { "test" })
                .build();
        element.addAnnotation(annotation);
    }

    private void addFullAnnotationUsingJandex(AnnotatedElement element) {
        element.addAnnotation(AnnotationInstance.builder(MyFullAnnotation.class)
                .add("bool", true)
                .add("ch", 'c')
                .add("b", (byte) 42)
                .add("s", (short) 42)
                .add("i", 42)
                .add("l", 42L)
                .add("f", 42.0F)
                .add("d", 42.0)
                .add("str", "str")
                .add("enumerated", MyEnum.YES)
                .add("cls", MyInterface.class)
                .add("nested", AnnotationInstance.builder(MyNestedAnnotation.class)
                        .add("cls", MyInterface.class)
                        .add("innerNested", AnnotationInstance.builder(MyAnnotation.class)
                                .add("value", "nested")
                                .add("enumVal", MyEnum.YES)
                                .build())
                        .add("clsArray", new Type[] {
                                ClassType.create(MyInterface.class),
                                ArrayType.create(PrimitiveType.BOOLEAN, 1)
                        })
                        .add("innerNestedArray", new AnnotationInstance[] {
                                AnnotationInstance.builder(MyAnnotation.class)
                                        .add("value", "nested1")
                                        .add("enumVal", MyEnum.YES)
                                        .build(),
                                AnnotationInstance.builder(MyAnnotation.class)
                                        .add("value", "nested2")
                                        .add("enumVal", MyEnum.NO)
                                        .build()
                        })
                        .build())
                .add("boolArray", new boolean[] { true, false })
                .add("chArray", new char[] { 'c', 'd' })
                .add("bArray", new byte[] { (byte) 42, (byte) 43 })
                .add("sArray", new short[] { (short) 42, (short) 43 })
                .add("iArray", new int[] { 42, 43 })
                .add("lArray", new long[] { 42L, 43L })
                .add("fArray", new float[] { 42.0F, 43.0F })
                .add("dArray", new double[] { 42.0, 43.0 })
                .add("strArray", new String[] { "foo", "bar" })
                .add("enumeratedArray", new Enum[] { MyEnum.YES, MyEnum.NO })
                .add("clsArray", new Type[] {
                        ClassType.create(MyInterface.class),
                        ArrayType.create(PrimitiveType.CHAR, 2)
                })
                .add("nestedArray", new AnnotationInstance[] {
                        AnnotationInstance.builder(MyNestedAnnotation.class)
                                .add("cls", MyInterface.class)
                                .add("innerNested", AnnotationInstance.builder(MyAnnotation.class)
                                        .add("value", "nested1")
                                        .add("enumVal", MyEnum.YES)
                                        .build())
                                .add("clsArray", new Type[] {
                                        ClassType.create(MyInterface.class),
                                        ArrayType.create(PrimitiveType.BOOLEAN, 1)
                                })
                                .add("innerNestedArray", new AnnotationInstance[] {
                                        AnnotationInstance.builder(MyAnnotation.class)
                                                .add("value", "nested11")
                                                .add("enumVal", MyEnum.YES)
                                                .build(),
                                        AnnotationInstance.builder(MyAnnotation.class)
                                                .add("value", "nested12")
                                                .add("enumVal", MyEnum.NO)
                                                .build()
                                })
                                .build(),
                        AnnotationInstance.builder(MyNestedAnnotation.class)
                                .add("cls", MyInterface.class)
                                .add("innerNested", AnnotationInstance.builder(MyAnnotation.class)
                                        .add("value", "nested2")
                                        .add("enumVal", MyEnum.YES)
                                        .build())
                                .add("clsArray", new Type[] {
                                        ClassType.create(MyInterface.class),
                                        ArrayType.create(PrimitiveType.BOOLEAN, 1)
                                })
                                .add("innerNestedArray", new AnnotationInstance[] {
                                        AnnotationInstance.builder(MyAnnotation.class)
                                                .add("value", "nested21")
                                                .add("enumVal", MyEnum.YES)
                                                .build(),
                                        AnnotationInstance.builder(MyAnnotation.class)
                                                .add("value", "nested22")
                                                .add("enumVal", MyEnum.NO)
                                                .build()
                                })
                                .build()
                })
                .build());
    }

    private void addFullAnnotationUsingNestedCreator(AnnotatedElement element) {
        AnnotationCreator creator = element.addAnnotation(MyFullAnnotation.class);

        creator.addValue("bool", true);
        creator.addValue("ch", 'c');
        creator.addValue("b", (byte) 42);
        creator.addValue("s", (short) 42);
        creator.addValue("i", 42);
        creator.addValue("l", 42L);
        creator.addValue("f", 42.0F);
        creator.addValue("d", 42.0);
        creator.addValue("str", "str");
        creator.addValue("enumerated", MyEnum.YES);
        creator.addValue("cls", MyInterface.class);
        creator.addValue("nested", AnnotationCreator.of(MyNestedAnnotation.class)
                .add("cls", MyInterface.class)
                .add("innerNested", AnnotationCreator.of(MyAnnotation.class)
                        .add("value", "nested")
                        .add("enumVal", MyEnum.YES))
                .add("clsArray", new Class[] { MyInterface.class, boolean[].class })
                .add("innerNestedArray", new AnnotationCreator[] {
                        AnnotationCreator.of(MyAnnotation.class)
                                .add("value", "nested1")
                                .add("enumVal", MyEnum.YES),
                        AnnotationCreator.of(MyAnnotation.class)
                                .add("value", "nested2")
                                .add("enumVal", MyEnum.NO)
                }));

        creator.addValue("boolArray", new boolean[] { true, false });
        creator.addValue("chArray", new char[] { 'c', 'd' });
        creator.addValue("bArray", new byte[] { (byte) 42, (byte) 43 });
        creator.addValue("sArray", new short[] { (short) 42, (short) 43 });
        creator.addValue("iArray", new int[] { 42, 43 });
        creator.addValue("lArray", new long[] { 42L, 43L });
        creator.addValue("fArray", new float[] { 42.0F, 43.0F });
        creator.addValue("dArray", new double[] { 42.0, 43.0 });
        creator.addValue("strArray", new String[] { "foo", "bar" });
        creator.addValue("enumeratedArray", new MyEnum[] { MyEnum.YES, MyEnum.NO });
        creator.addValue("clsArray", new Class[] { MyInterface.class, char[][].class });
        creator.addValue("nestedArray", new AnnotationCreator[] {
                AnnotationCreator.of(MyNestedAnnotation.class)
                        .add("cls", MyInterface.class)
                        .add("innerNested", AnnotationCreator.of(MyAnnotation.class)
                                .add("value", "nested1")
                                .add("enumVal", MyEnum.YES))
                        .add("clsArray", new Class[] { MyInterface.class, boolean[].class })
                        .add("innerNestedArray", new AnnotationCreator[] {
                                AnnotationCreator.of(MyAnnotation.class)
                                        .add("value", "nested11")
                                        .add("enumVal", MyEnum.YES),
                                AnnotationCreator.of(MyAnnotation.class)
                                        .add("value", "nested12")
                                        .add("enumVal", MyEnum.NO)
                        }),
                AnnotationCreator.of(MyNestedAnnotation.class)
                        .add("cls", MyInterface.class)
                        .add("innerNested", AnnotationCreator.of(MyAnnotation.class)
                                .add("value", "nested2")
                                .add("enumVal", MyEnum.YES))
                        .add("clsArray", new Class[] { MyInterface.class, boolean[].class })
                        .add("innerNestedArray", new AnnotationCreator[] {
                                AnnotationCreator.of(MyAnnotation.class)
                                        .add("value", "nested21")
                                        .add("enumVal", MyEnum.YES),
                                AnnotationCreator.of(MyAnnotation.class)
                                        .add("value", "nested22")
                                        .add("enumVal", MyEnum.NO)
                        }),
        });
    }

    private void verifyFullAnnotation(MyFullAnnotation annotation) {
        assertEquals(true, annotation.bool());
        assertEquals('c', annotation.ch());
        assertEquals((byte) 42, annotation.b());
        assertEquals((short) 42, annotation.s());
        assertEquals(42, annotation.i());
        assertEquals(42L, annotation.l());
        assertEquals(42.0F, annotation.f(), 0.1f);
        assertEquals(42.0, annotation.d(), 0.1);
        assertEquals("str", annotation.str());
        assertEquals(MyEnum.YES, annotation.enumerated());
        assertEquals(MyInterface.class, annotation.cls());
        assertEquals(MyInterface.class, annotation.nested().cls());
        assertEquals("nested", annotation.nested().innerNested().value());
        assertEquals(MyEnum.YES, annotation.nested().innerNested().enumVal());
        assertArrayEquals(new Class[] { MyInterface.class, boolean[].class }, annotation.nested().clsArray());
        assertEquals("nested1", annotation.nested().innerNestedArray()[0].value());
        assertEquals(MyEnum.YES, annotation.nested().innerNestedArray()[0].enumVal());
        assertEquals("nested2", annotation.nested().innerNestedArray()[1].value());
        assertEquals(MyEnum.NO, annotation.nested().innerNestedArray()[1].enumVal());

        assertArrayEquals(new boolean[] { true, false }, annotation.boolArray());
        assertArrayEquals(new char[] { 'c', 'd' }, annotation.chArray());
        assertArrayEquals(new byte[] { (byte) 42, (byte) 43 }, annotation.bArray());
        assertArrayEquals(new short[] { (short) 42, (short) 43 }, annotation.sArray());
        assertArrayEquals(new int[] { 42, 43 }, annotation.iArray());
        assertArrayEquals(new long[] { 42L, 43L }, annotation.lArray());
        assertArrayEquals(new float[] { 42.0F, 43.0F }, annotation.fArray(), 0.1f);
        assertArrayEquals(new double[] { 42.0, 43.0 }, annotation.dArray(), 0.1);
        assertArrayEquals(new String[] { "foo", "bar" }, annotation.strArray());
        assertArrayEquals(new MyEnum[] { MyEnum.YES, MyEnum.NO }, annotation.enumeratedArray());
        assertArrayEquals(new Class[] { MyInterface.class, char[][].class }, annotation.clsArray());
        assertEquals(MyInterface.class, annotation.nestedArray()[0].cls());
        assertEquals("nested1", annotation.nestedArray()[0].innerNested().value());
        assertEquals(MyEnum.YES, annotation.nestedArray()[0].innerNested().enumVal());
        assertArrayEquals(new Class[] { MyInterface.class, boolean[].class }, annotation.nestedArray()[0].clsArray());
        assertEquals("nested11", annotation.nestedArray()[0].innerNestedArray()[0].value());
        assertEquals(MyEnum.YES, annotation.nestedArray()[0].innerNestedArray()[0].enumVal());
        assertEquals("nested12", annotation.nestedArray()[0].innerNestedArray()[1].value());
        assertEquals(MyEnum.NO, annotation.nestedArray()[0].innerNestedArray()[1].enumVal());
        assertEquals(MyInterface.class, annotation.nestedArray()[1].cls());
        assertEquals("nested2", annotation.nestedArray()[1].innerNested().value());
        assertEquals(MyEnum.YES, annotation.nestedArray()[1].innerNested().enumVal());
        assertArrayEquals(new Class[] { MyInterface.class, boolean[].class }, annotation.nestedArray()[1].clsArray());
        assertEquals("nested21", annotation.nestedArray()[1].innerNestedArray()[0].value());
        assertEquals(MyEnum.YES, annotation.nestedArray()[1].innerNestedArray()[0].enumVal());
        assertEquals("nested22", annotation.nestedArray()[1].innerNestedArray()[1].value());
        assertEquals(MyEnum.NO, annotation.nestedArray()[1].innerNestedArray()[1].enumVal());
    }
}
