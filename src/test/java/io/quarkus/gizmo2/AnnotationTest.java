package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import org.junit.jupiter.api.Test;

public class AnnotationTest {
    @Retention(RetentionPolicy.RUNTIME)
    @interface MyAnnotation {
        String value();
    }

    @Test
    public void annotationOnClass() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnClass", cc -> {
            cc.withAnnotation(MyAnnotation.class, ann -> {
                ann.with(MyAnnotation::value, "annotationOnClass");
            });
        });
        MyAnnotation ann = tcm.definedClass().getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnClass", ann.value());
    }

    @Test
    public void annotationOnInterface() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnInterface", cc -> {
            cc.withAnnotation(MyAnnotation.class, ann -> {
                ann.with(MyAnnotation::value, "annotationOnInterface");
            });
        });
        MyAnnotation ann = tcm.definedClass().getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnInterface", ann.value());
    }

    @Test
    public void annotationOnStaticClassField() throws NoSuchFieldException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnStaticClassField", cc -> {
            cc.staticField("staticField", fc -> {
                fc.withType(String.class);
                fc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnStaticClassField");
                });
            });
        });
        Field field = tcm.definedClass().getDeclaredField("staticField");
        assertNotNull(field);
        MyAnnotation ann = field.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnStaticClassField", ann.value());
    }

    @Test
    public void annotationOnStaticInterfaceField() throws NoSuchFieldException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnStaticInterfaceField", cc -> {
            cc.staticField("staticField", fc -> {
                fc.withType(String.class);
                fc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnStaticInterfaceField");
                });
            });
        });
        Field field = tcm.definedClass().getDeclaredField("staticField");
        assertNotNull(field);
        MyAnnotation ann = field.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnStaticInterfaceField", ann.value());
    }

    @Test
    public void annotationOnInstanceClassField() throws NoSuchFieldException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnInstanceClassField", cc -> {
            cc.field("field", fc -> {
                fc.withType(String.class);
                fc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnInstanceClassField");
                });
            });
        });
        Field field = tcm.definedClass().getDeclaredField("field");
        assertNotNull(field);
        MyAnnotation ann = field.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnInstanceClassField", ann.value());
    }

    @Test
    public void annotationOnStaticClassMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnStaticClassMethod", cc -> {
            cc.staticMethod("staticMethod", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnStaticClassMethod");
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("staticMethod");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnStaticClassMethod", ann.value());
    }

    @Test
    public void annotationOnStaticInterfaceMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnStaticInterfaceMethod", cc -> {
            cc.staticMethod("staticMethod", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnStaticInterfaceMethod");
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("staticMethod");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnStaticInterfaceMethod", ann.value());
    }

    @Test
    public void annotationOnAbstractClassMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnAbstractClassMethod", cc -> {
            cc.abstractMethod("abstractMethod", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnAbstractClassMethod");
                });
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("abstractMethod");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnAbstractClassMethod", ann.value());
    }

    @Test
    public void annotationOnClassMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnClassMethod", cc -> {
            cc.method("method", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnClassMethod");
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("method");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnClassMethod", ann.value());
    }

    @Test
    public void annotationOnAbstractInterfaceMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnAbstractInterfaceMethod", cc -> {
            cc.method("method", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnAbstractInterfaceMethod");
                });
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("method");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnAbstractInterfaceMethod", ann.value());
    }

    @Test
    public void annotationOnDefaultInterfaceMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnDefaultInterfaceMethod", cc -> {
            cc.defaultMethod("defaultMethod", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnDefaultInterfaceMethod");
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("defaultMethod");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnDefaultInterfaceMethod", ann.value());
    }

    @Test
    public void annotationOnPrivateInterfaceMethod() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnPrivateInterfaceMethod", cc -> {
            cc.privateMethod("privateMethod", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(MyAnnotation.class, ann -> {
                    ann.with(MyAnnotation::value, "annotationOnPrivateInterfaceMethod");
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("privateMethod");
        assertNotNull(method);
        MyAnnotation ann = method.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnPrivateInterfaceMethod", ann.value());
    }

    @Test
    public void annotationOnClassMethodParameter() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnClassMethodParameter", cc -> {
            cc.method("method", mc -> {
                mc.returning(String.class);
                mc.parameter("parameter", pc -> {
                    pc.withType(String.class);
                    pc.withAnnotation(MyAnnotation.class, ann -> {
                        ann.with(MyAnnotation::value, "annotationOnClassMethodParameter");
                    });
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("method", String.class);
        Parameter param = method.getParameters()[0];
        assertNotNull(param);
        MyAnnotation ann = param.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnClassMethodParameter", ann.value());
    }

    @Test
    public void annotationOnInterfaceMethodParameter() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.interface_("io.quarkus.gizmo2.AnnotationOnInterfaceMethodParameter", cc -> {
            cc.method("method", mc -> {
                mc.returning(String.class);
                mc.parameter("parameter", pc -> {
                    pc.withType(String.class);
                    pc.withAnnotation(MyAnnotation.class, ann -> {
                        ann.with(MyAnnotation::value, "annotationOnInterfaceMethodParameter");
                    });
                });
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("method", String.class);
        Parameter param = method.getParameters()[0];
        assertNotNull(param);
        MyAnnotation ann = param.getAnnotation(MyAnnotation.class);
        assertNotNull(ann);
        assertEquals("annotationOnInterfaceMethodParameter", ann.value());
    }

    @Test
    public void annotationOnWrongThing() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationOnWrongThing", cc -> {
            cc.field("wrongPlace", fc -> {
                assertThrows(IllegalArgumentException.class, () -> {
                    fc.withAnnotation(SafeVarargs.class);
                });
            });
        });
    }

    @Test
    public void repeatableAnnotation() throws NoSuchFieldException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.RepeatableAnnotation", cc -> {
            cc.field("notAnnotated", fc -> {
            });
            cc.field("single", fc -> {
                fc.withAnnotation(RepeatableInner.class);
            });
            cc.field("multi", fc -> {
                fc.withAnnotation(RepeatableInner.class);
                fc.withAnnotation(RepeatableInner.class);
                fc.withAnnotation(RepeatableInner.class);
            });
        });
        Field notAnnotated = tcm.definedClass().getDeclaredField("notAnnotated");
        Field single = tcm.definedClass().getDeclaredField("single");
        Field multi = tcm.definedClass().getDeclaredField("multi");
        assertNull(notAnnotated.getAnnotation(RepeatableInner.class));
        assertNull(notAnnotated.getAnnotation(RepeatableOuter.class));
        assertNotNull(single.getAnnotation(RepeatableInner.class));
        assertNull(single.getAnnotation(RepeatableOuter.class));
        assertNull(multi.getAnnotation(RepeatableInner.class));
        assertNotNull(multi.getAnnotation(RepeatableOuter.class));
        assertEquals(3, multi.getAnnotation(RepeatableOuter.class).value().length);
    }

    // ---

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(value = RepeatableOuter.class)
    @interface RepeatableInner {
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface RepeatableOuter {
        RepeatableInner[] value();
    }

    enum MyEnum {
        FOO,
        BAR,
        BAZ,
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface MyComplexAnnotation {
        boolean bool();

        byte b();

        short s();

        int i();

        long l();

        float f();

        double d();

        char ch();

        String str();

        MyEnum en();

        Class<?> cls();

        MyAnnotation nested();

        boolean[] boolArray();

        byte[] bArray();

        short[] sArray();

        int[] iArray();

        long[] lArray();

        float[] fArray();

        double[] dArray();

        char[] chArray();

        String[] strArray();

        MyEnum[] enArray();

        Class<?>[] clsArray();

        MyAnnotation[] nestedArray();
    }

    @Test
    public void annotationCreation() throws NoSuchMethodException {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.AnnotationCreation", cc -> {
            cc.method("method", mc -> {
                mc.returning(String.class);
                mc.withAnnotation(ClassDesc.of(Deprecated.class.getName()), RetentionPolicy.RUNTIME, ac -> {
                    ac.with("since", "1.0");
                    ac.with("forRemoval", true);
                });
                mc.withAnnotation(MyComplexAnnotation.class, ac -> {
                    ac.with(MyComplexAnnotation::bool, true);
                    ac.with(MyComplexAnnotation::b, (byte) 1);
                    ac.with(MyComplexAnnotation::s, (short) 2);
                    ac.with(MyComplexAnnotation::i, 3);
                    ac.with(MyComplexAnnotation::l, 4L);
                    ac.with(MyComplexAnnotation::f, 5.0F);
                    ac.with(MyComplexAnnotation::d, 6.0);
                    ac.with(MyComplexAnnotation::ch, 'a');
                    ac.with(MyComplexAnnotation::str, "bc");
                    ac.with(MyComplexAnnotation::en, MyEnum.FOO);
                    ac.with(MyComplexAnnotation::cls, Object.class);
                    ac.with(MyComplexAnnotation::nested, nac -> {
                        nac.with(MyAnnotation::value, "one");
                    });
                    ac.withArray(MyComplexAnnotation::boolArray, true, false);
                    ac.withArray(MyComplexAnnotation::bArray, (byte) 7, (byte) 8);
                    ac.withArray(MyComplexAnnotation::sArray, (short) 9, (short) 10);
                    ac.withArray(MyComplexAnnotation::iArray, 11, 12);
                    ac.withArray(MyComplexAnnotation::lArray, 13L, 14L);
                    ac.withArray(MyComplexAnnotation::fArray, 15.0F, 16.0F);
                    ac.withArray(MyComplexAnnotation::dArray, 17.0, 18.0);
                    ac.withArray(MyComplexAnnotation::chArray, 'd', 'e');
                    ac.withArray(MyComplexAnnotation::strArray, "fg", "hi");
                    ac.withArray(MyComplexAnnotation::enArray, List.of(MyEnum.BAR, MyEnum.BAZ));
                    ac.withArray(MyComplexAnnotation::clsArray, String.class, void.class, Number[].class, byte[][].class);
                    ac.withArray(MyComplexAnnotation::nestedArray, List.of(nac -> {
                        nac.with(MyAnnotation::value, "two");
                    }, nac -> {
                        nac.with(MyAnnotation::value, "three");
                    }));
                });
                mc.body(bc -> bc.return_("foobar"));
            });
        });
        Method method = tcm.definedClass().getDeclaredMethod("method");
        assertNotNull(method);

        Deprecated deprecated = method.getAnnotation(Deprecated.class);
        assertNotNull(deprecated);
        assertEquals("1.0", deprecated.since());
        assertEquals(true, deprecated.forRemoval());

        MyComplexAnnotation ann = method.getAnnotation(MyComplexAnnotation.class);
        assertNotNull(ann);
        assertEquals(true, ann.bool());
        assertEquals((byte) 1, ann.b());
        assertEquals((short) 2, ann.s());
        assertEquals(3, ann.i());
        assertEquals(4L, ann.l());
        assertEquals(5.0F, ann.f());
        assertEquals(6.0, ann.d());
        assertEquals('a', ann.ch());
        assertEquals("bc", ann.str());
        assertEquals(MyEnum.FOO, ann.en());
        assertEquals(Object.class, ann.cls());
        assertEquals("one", ann.nested().value());

        assertEquals(2, ann.boolArray().length);
        assertEquals(true, ann.boolArray()[0]);
        assertEquals(false, ann.boolArray()[1]);
        assertEquals(2, ann.bArray().length);
        assertEquals((byte) 7, ann.bArray()[0]);
        assertEquals((byte) 8, ann.bArray()[1]);
        assertEquals(2, ann.sArray().length);
        assertEquals((short) 9, ann.sArray()[0]);
        assertEquals((short) 10, ann.sArray()[1]);
        assertEquals(2, ann.iArray().length);
        assertEquals(11, ann.iArray()[0]);
        assertEquals(12, ann.iArray()[1]);
        assertEquals(2, ann.lArray().length);
        assertEquals(13L, ann.lArray()[0]);
        assertEquals(14L, ann.lArray()[1]);
        assertEquals(2, ann.fArray().length);
        assertEquals(15.0F, ann.fArray()[0]);
        assertEquals(16.0F, ann.fArray()[1]);
        assertEquals(2, ann.dArray().length);
        assertEquals(17.0, ann.dArray()[0]);
        assertEquals(18.0, ann.dArray()[1]);
        assertEquals(2, ann.chArray().length);
        assertEquals('d', ann.chArray()[0]);
        assertEquals('e', ann.chArray()[1]);
        assertEquals(2, ann.strArray().length);
        assertEquals("fg", ann.strArray()[0]);
        assertEquals("hi", ann.strArray()[1]);
        assertEquals(2, ann.enArray().length);
        assertEquals(MyEnum.BAR, ann.enArray()[0]);
        assertEquals(MyEnum.BAZ, ann.enArray()[1]);
        assertEquals(4, ann.clsArray().length);
        assertEquals(String.class, ann.clsArray()[0]);
        assertEquals(void.class, ann.clsArray()[1]);
        assertEquals(Number[].class, ann.clsArray()[2]);
        assertEquals(byte[][].class, ann.clsArray()[3]);
        assertEquals(2, ann.nestedArray().length);
        assertEquals("two", ann.nestedArray()[0].value());
        assertEquals("three", ann.nestedArray()[1].value());
    }
}
