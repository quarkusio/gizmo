package io.quarkus.gizmo;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.junit.Assert;
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
        Assert.assertEquals("test", annotation.value());
    }

    @Test
    public void testClassAnnotationWithStringArray() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addAnnotationWithStringArray(creator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyArrayAnnotation.class);
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testClassAnnotationWithAnnotationValueArray() throws ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            addAnnotationWithAnnotationValueArray(creator);
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getAnnotation(MyArrayAnnotation.class);
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
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
        Assert.assertEquals("test", annotation.value());
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
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodAnnotationWithAnnotationValueArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class)) {
                addAnnotationWithAnnotationValueArray(methodCreator);
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test")
                .getAnnotation(MyArrayAnnotation.class);
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
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
        Assert.assertEquals("test", annotation.value());
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
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    @Test
    public void testMethodParamAnnotationWithAnnotationValueArray() throws ClassNotFoundException, NoSuchMethodException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").build()) {
            try (MethodCreator methodCreator = creator.getMethodCreator("test", void.class, String.class)) {
                addAnnotationWithAnnotationValueArray(methodCreator.getParameterAnnotations(0));
                methodCreator.returnValue(null);
            }
        }

        MyArrayAnnotation annotation = cl.loadClass("com.MyTest")
                .getMethod("test", String.class)
                .getParameters()[0]
                .getAnnotation(MyArrayAnnotation.class);
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
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
        Assert.assertEquals("test", annotation.value());
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
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
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
        Assert.assertArrayEquals(new String[] { "test" }, annotation.value());
    }

    private void addAnnotationWithString(AnnotatedElement element) {
        element.addAnnotation(MyAnnotation.class).addValue("value", "test");
    }

    private void addAnnotationWithStringArray(AnnotatedElement element) {
        element.addAnnotation(MyArrayAnnotation.class).addValue("value", new String[] { "test" });
    }

    private void addAnnotationWithAnnotationValueArray(AnnotatedElement element) {
        AnnotationInstance annotation = AnnotationInstance.create(DotName.createSimple(MyArrayAnnotation.class.getName()), null,
                new AnnotationValue[] {
                        AnnotationValue.createArrayValue("value",
                                new AnnotationValue[] {
                                        AnnotationValue.createStringValue("value", "test")
                                }
                        )
                }
        );
        element.addAnnotation(annotation);
    }
}
