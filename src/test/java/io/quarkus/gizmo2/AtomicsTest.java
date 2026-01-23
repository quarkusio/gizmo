package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

public final class AtomicsTest {

    private static final MethodDesc assertFalse = MethodDesc.of(Assertions.class, "assertFalse", void.class, boolean.class);
    private static final MethodDesc assertTrue = MethodDesc.of(Assertions.class, "assertTrue", void.class, boolean.class);
    private static final MethodDesc assertEqualsI = MethodDesc.of(Assertions.class, "assertEquals", void.class, int.class,
            int.class);
    private static final MethodDesc assertEqualsL = MethodDesc.of(Assertions.class, "assertEquals", void.class, Object.class,
            Object.class);

    @Test
    public void testGetAndSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndSet");
        Class<?> clazz = tcm.loadClass(g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            FieldDesc strVal = zc.field("strVal", Const.of("Hello"));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            StaticFieldVar staticStrVal = zc.staticField("staticStrVal", sfc -> {
                sfc.setInitial("Goodbye");
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, instance.field(intVal), Const.of(123));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(123),
                            b0.getAndSet(instance.field(intVal), Const.of(234), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234),
                            b0.getAndSet(instance.field(intVal), Const.of(432), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432),
                            b0.getAndSet(instance.field(intVal), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(333), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndSet(staticIntVal, Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.getAndSet(staticIntVal, Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.getAndSet(staticIntVal, Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndSet(array.elem(0), Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.getAndSet(array.elem(0), Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.getAndSet(array.elem(0), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), array.elem(0));
                    b0.return_();
                });
            });
            zc.staticMethod("test3", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), instance.field(strVal));
                    // instance obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"),
                            b0.getAndSet(instance.field(strVal), Const.of("Meow"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), instance.field(strVal));
                    // instance obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"),
                            b0.getAndSet(instance.field(strVal), Const.of("Arf"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"), instance.field(strVal));
                    // instance obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"),
                            b0.getAndSet(instance.field(strVal), Const.of("Moo"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Moo"), instance.field(strVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test4", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), staticStrVal);
                    // static obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.getAndSet(staticStrVal, Const.of("Apple"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), staticStrVal);
                    // static obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.getAndSet(staticStrVal, Const.of("Banana"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), staticStrVal);
                    // static obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.getAndSet(staticStrVal, Const.of("Pear"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), staticStrVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test5", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(String.class, Const.of("Goodbye")));
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), array.elem(0));
                    // array obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.getAndSet(array.elem(0), Const.of("Apple"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), array.elem(0));
                    // array obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.getAndSet(array.elem(0), Const.of("Banana"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), array.elem(0));
                    // array obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.getAndSet(array.elem(0), Const.of("Pear"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), array.elem(0));
                    b0.return_();
                });
            });
        }));
        tcm.staticMethod(clazz, "test0", Runnable.class).run();
        tcm.staticMethod(clazz, "test1", Runnable.class).run();
        tcm.staticMethod(clazz, "test2", Runnable.class).run();
        tcm.staticMethod(clazz, "test3", Runnable.class).run();
        tcm.staticMethod(clazz, "test4", Runnable.class).run();
        tcm.staticMethod(clazz, "test5", Runnable.class).run();
    }

    @Test
    public void testGetAndAdd() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndAdd");
        ClassDesc xxx = g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, Const.of(123), instance.field(intVal));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(123),
                            b0.getAndAdd(instance.field(intVal), Const.of(234), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(357), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(357),
                            b0.getAndAdd(instance.field(intVal), Const.of(432), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(789), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(789),
                            b0.getAndAdd(instance.field(intVal), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1122), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndAdd(staticIntVal, Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(515), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(515),
                            b0.getAndAdd(staticIntVal, Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(715), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(715),
                            b0.getAndAdd(staticIntVal, Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1714), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndAdd(array.elem(0), Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(515), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(515),
                            b0.getAndAdd(array.elem(0), Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(715), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(715),
                            b0.getAndAdd(array.elem(0), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1714), array.elem(0));
                    b0.return_();
                });
            });
        });
        Class<?> clazz = tcm.loadClass(xxx);
        tcm.staticMethod(clazz, "test0", Runnable.class).run();
        tcm.staticMethod(clazz, "test1", Runnable.class).run();
        tcm.staticMethod(clazz, "test2", Runnable.class).run();
    }

    @Test
    public void testGetAndBitwiseOr() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndBitwiseOr");
        Class<?> clazz = tcm.loadClass(g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, Const.of(123), instance.field(intVal));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(123),
                            b0.getAndBitwiseOr(instance.field(intVal), Const.of(234), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(251), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(251),
                            b0.getAndBitwiseOr(instance.field(intVal), Const.of(432), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(507), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(507),
                            b0.getAndBitwiseOr(instance.field(intVal), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(511), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndBitwiseOr(staticIntVal, Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(511), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(511),
                            b0.getAndBitwiseOr(staticIntVal, Const.of(832), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1023), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1023),
                            b0.getAndBitwiseOr(staticIntVal, Const.of(4096), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(5119), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.getAndBitwiseOr(array.elem(0), Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(511), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(511),
                            b0.getAndBitwiseOr(array.elem(0), Const.of(832), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1023), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(1023),
                            b0.getAndBitwiseOr(array.elem(0), Const.of(4096), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(5119), array.elem(0));
                    b0.return_();
                });
            });
        }));
        tcm.staticMethod(clazz, "test0", Runnable.class).run();
        tcm.staticMethod(clazz, "test1", Runnable.class).run();
        tcm.staticMethod(clazz, "test2", Runnable.class).run();
    }

    @Test
    public void testGetAndBitwiseAnd() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndBitwiseAnd");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(4095));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(4095);
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, Const.of(4095), instance.field(intVal));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseAnd(instance.field(intVal), Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811),
                            b0.getAndBitwiseAnd(instance.field(intVal), Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265),
                            b0.getAndBitwiseAnd(instance.field(intVal), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(65), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseAnd(staticIntVal, Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811),
                            b0.getAndBitwiseAnd(staticIntVal, Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265),
                            b0.getAndBitwiseAnd(staticIntVal, Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(193), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(4095)));
                    b0.invokeStatic(assertEqualsI, Const.of(4095), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseAnd(array.elem(0), Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3811),
                            b0.getAndBitwiseAnd(array.elem(0), Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3265),
                            b0.getAndBitwiseAnd(array.elem(0), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(193), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
    }

    @Test
    public void testGetAndBitwiseXor() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndBitwiseXor");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(4095));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(4095);
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, Const.of(4095), instance.field(intVal));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseXor(instance.field(intVal), Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284),
                            b0.getAndBitwiseXor(instance.field(intVal), Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293),
                            b0.getAndBitwiseXor(instance.field(intVal), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3472), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseXor(staticIntVal, Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284),
                            b0.getAndBitwiseXor(staticIntVal, Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293),
                            b0.getAndBitwiseXor(staticIntVal, Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3898), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(4095)));
                    b0.invokeStatic(assertEqualsI, Const.of(4095), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(4095),
                            b0.getAndBitwiseXor(array.elem(0), Const.of(3811), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(284),
                            b0.getAndBitwiseXor(array.elem(0), Const.of(3521), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3293),
                            b0.getAndBitwiseXor(array.elem(0), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(3898), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
    }

    @Test
    public void testCompareAndSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestCompareAndSet");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            FieldDesc strVal = zc.field("strVal", Const.of("Hello"));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            StaticFieldVar staticStrVal = zc.staticField("staticStrVal", sfc -> {
                sfc.setInitial("Goodbye");
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, instance.field(intVal), Const.of(123));
                    // instance int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.compareAndSet(instance.field(intVal), Const.of(777), Const.of(666)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(instance.field(intVal), Const.of(123), Const.of(234)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.compareAndSet(staticIntVal, Const.of(777), Const.of(666)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(staticIntVal, Const.of(404), Const.of(111)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.compareAndSet(array.elem(0), Const.of(777), Const.of(666)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(array.elem(0), Const.of(404), Const.of(111)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), array.elem(0));
                    b0.return_();
                });
            });
            zc.staticMethod("test3", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), instance.field(strVal));
                    // instance obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse,
                            b0.compareAndSet(instance.field(strVal), Const.of("Wrong"), Const.of("More wrong")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(instance.field(strVal), Const.of("Hello"), Const.of("Meow")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), instance.field(strVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test4", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), staticStrVal);
                    // static obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.compareAndSet(staticStrVal, Const.of("Wrong"), Const.of("More wrong")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(staticStrVal, Const.of("Goodbye"), Const.of("Apple")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), staticStrVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test5", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(String.class, Const.of("Goodbye")));
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), array.elem(0));
                    // array obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.compareAndSet(array.elem(0), Const.of("Wrong"), Const.of("More wrong")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.compareAndSet(array.elem(0), Const.of("Goodbye"), Const.of("Apple")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
        tcm.staticMethod(desc, "test3", Runnable.class).run();
        tcm.staticMethod(desc, "test4", Runnable.class).run();
        tcm.staticMethod(desc, "test5", Runnable.class).run();
    }

    @Test
    public void testWeakCompareAndSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestWeakCompareAndSet");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            FieldDesc strVal = zc.field("strVal", Const.of("Hello"));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            StaticFieldVar staticStrVal = zc.staticField("staticStrVal", sfc -> {
                sfc.setInitial("Goodbye");
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, instance.field(intVal), Const.of(123));
                    // instance int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.weakCompareAndSet(instance.field(intVal), Const.of(432), Const.of(888)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.weakCompareAndSet(instance.field(intVal), Const.of(123), Const.of(234)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.weakCompareAndSet(staticIntVal, Const.of(432), Const.of(888)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.weakCompareAndSet(staticIntVal, Const.of(404), Const.of(111)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.weakCompareAndSet(array.elem(0), Const.of(432), Const.of(888)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.weakCompareAndSet(array.elem(0), Const.of(404), Const.of(111)));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), array.elem(0));
                    b0.return_();
                });
            });
            zc.staticMethod("test3", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), instance.field(strVal));
                    // instance obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse,
                            b0.weakCompareAndSet(instance.field(strVal), Const.of("Cat"), Const.of("Dog")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue,
                            b0.weakCompareAndSet(instance.field(strVal), Const.of("Hello"), Const.of("Meow")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), instance.field(strVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test4", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), staticStrVal);
                    // static obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.weakCompareAndSet(staticStrVal, Const.of("Cat"), Const.of("Dog")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.weakCompareAndSet(staticStrVal, Const.of("Goodbye"), Const.of("Apple")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), staticStrVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test5", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(String.class, Const.of("Goodbye")));
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), array.elem(0));
                    // array obj
                    b0.line(nextLine());
                    b0.invokeStatic(assertFalse, b0.weakCompareAndSet(array.elem(0), Const.of("Cat"), Const.of("Dog")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertTrue, b0.weakCompareAndSet(array.elem(0), Const.of("Goodbye"), Const.of("Apple")));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
        tcm.staticMethod(desc, "test3", Runnable.class).run();
        tcm.staticMethod(desc, "test4", Runnable.class).run();
        tcm.staticMethod(desc, "test5", Runnable.class).run();
    }

    @Test
    public void testCompareAndExchange() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestCompareAndExchange");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            FieldDesc strVal = zc.field("strVal", Const.of("Hello"));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            StaticFieldVar staticStrVal = zc.staticField("staticStrVal", sfc -> {
                sfc.setInitial("Goodbye");
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, instance.field(intVal), Const.of(123));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(123),
                            b0.compareAndExchange(instance.field(intVal), Const.of(123), Const.of(234), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234), instance.field(intVal));
                    // instance int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234),
                            b0.compareAndExchange(instance.field(intVal), Const.of(234), Const.of(432), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432), instance.field(intVal));
                    // instance int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432),
                            b0.compareAndExchange(instance.field(intVal), Const.of(432), Const.of(333), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(333), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.compareAndExchange(staticIntVal, Const.of(404), Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), staticIntVal);
                    // static int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.compareAndExchange(staticIntVal, Const.of(111), Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), staticIntVal);
                    // static int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.compareAndExchange(staticIntVal, Const.of(200), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.compareAndExchange(array.elem(0), Const.of(404), Const.of(111), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), array.elem(0));
                    // array int: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.compareAndExchange(array.elem(0), Const.of(111), Const.of(200), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), array.elem(0));
                    // array int: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.compareAndExchange(array.elem(0), Const.of(200), Const.of(999), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), array.elem(0));
                    b0.return_();
                });
            });
            zc.staticMethod("test3", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), instance.field(strVal));
                    // instance obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), b0.compareAndExchange(instance.field(strVal),
                            Const.of("Hello"), Const.of("Meow"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), instance.field(strVal));
                    // instance obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), b0.compareAndExchange(instance.field(strVal),
                            Const.of("Meow"), Const.of("Arf"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"), instance.field(strVal));
                    // instance obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"), b0.compareAndExchange(instance.field(strVal),
                            Const.of("Arf"), Const.of("Moo"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Moo"), instance.field(strVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test4", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), staticStrVal);
                    // static obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.compareAndExchange(staticStrVal, Const.of("Goodbye"), Const.of("Apple"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), staticStrVal);
                    // static obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.compareAndExchange(staticStrVal, Const.of("Apple"), Const.of("Banana"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), staticStrVal);
                    // static obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.compareAndExchange(staticStrVal, Const.of("Banana"), Const.of("Pear"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), staticStrVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test5", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(String.class, Const.of("Goodbye")));
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), array.elem(0));
                    // array obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.compareAndExchange(array.elem(0), Const.of("Goodbye"), Const.of("Apple"), MemoryOrder.Volatile));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), array.elem(0));
                    // array obj: acquire
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.compareAndExchange(array.elem(0), Const.of("Apple"), Const.of("Banana"), MemoryOrder.Acquire));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), array.elem(0));
                    // array obj: release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.compareAndExchange(array.elem(0), Const.of("Banana"), Const.of("Pear"), MemoryOrder.Release));
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
        tcm.staticMethod(desc, "test3", Runnable.class).run();
        tcm.staticMethod(desc, "test4", Runnable.class).run();
        tcm.staticMethod(desc, "test5", Runnable.class).run();
    }

    @Test
    public void testGetAndSetSeparately() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = ClassDesc.of("io.quarkus.gizmo2.TestGetAndSetSeparately");
        g.class_(desc, zc -> {
            FieldDesc intVal = zc.field("intVal", Const.of(123));
            FieldDesc strVal = zc.field("strVal", Const.of("Hello"));
            StaticFieldVar staticIntVal = zc.staticField("staticIntVal", sfc -> {
                sfc.setInitial(404);
            });
            StaticFieldVar staticStrVal = zc.staticField("staticStrVal", sfc -> {
                sfc.setInitial("Goodbye");
            });
            zc.sourceFile(file());
            zc.defaultConstructor();
            // test methods
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsI, instance.field(intVal), Const.of(123));
                    // instance int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(123),
                            b0.get(instance.field(intVal), MemoryOrder.Volatile));
                    b0.set(instance.field(intVal), Const.of(234), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234), instance.field(intVal));
                    // instance int: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(234),
                            b0.get(instance.field(intVal), MemoryOrder.Acquire));
                    b0.set(instance.field(intVal), Const.of(432), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432), instance.field(intVal));
                    // instance int: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(432),
                            b0.get(instance.field(intVal), MemoryOrder.Opaque));
                    b0.set(instance.field(intVal), Const.of(333), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(333), instance.field(intVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404), staticIntVal);
                    // static int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.get(staticIntVal, MemoryOrder.Volatile));
                    b0.set(staticIntVal, Const.of(111), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), staticIntVal);
                    // static int: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.get(staticIntVal, MemoryOrder.Acquire));
                    b0.getAndSet(staticIntVal, Const.of(200), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), staticIntVal);
                    // static int: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.get(staticIntVal, MemoryOrder.Opaque));
                    b0.set(staticIntVal, Const.of(999), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), staticIntVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test2", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(int.class, Const.of(404)));
                    b0.invokeStatic(assertEqualsI, Const.of(404), array.elem(0));
                    // array int: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(404),
                            b0.get(array.elem(0), MemoryOrder.Volatile));
                    b0.set(array.elem(0), Const.of(111), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111), array.elem(0));
                    // array int: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(111),
                            b0.get(array.elem(0), MemoryOrder.Acquire));
                    b0.set(array.elem(0), Const.of(200), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200), array.elem(0));
                    // array int: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(200),
                            b0.get(array.elem(0), MemoryOrder.Opaque));
                    b0.set(array.elem(0), Const.of(999), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsI, Const.of(999), array.elem(0));
                    b0.return_();
                });
            });
            zc.staticMethod("test3", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar instance = b0.localVar("instance", b0.new_(desc));
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"), instance.field(strVal));
                    // instance obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Hello"),
                            b0.get(instance.field(strVal), MemoryOrder.Volatile));
                    b0.set(instance.field(strVal), Const.of("Meow"), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"), instance.field(strVal));
                    // instance obj: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Meow"),
                            b0.get(instance.field(strVal), MemoryOrder.Acquire));
                    b0.set(instance.field(strVal), Const.of("Arf"), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"), instance.field(strVal));
                    // instance obj: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Arf"),
                            b0.get(instance.field(strVal), MemoryOrder.Opaque));
                    b0.set(instance.field(strVal), Const.of("Moo"), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Moo"), instance.field(strVal));
                    b0.return_();
                });
            });
            zc.staticMethod("test4", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), staticStrVal);
                    // static obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.get(staticStrVal, MemoryOrder.Volatile));
                    b0.set(staticStrVal, Const.of("Apple"), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), staticStrVal);
                    // static obj: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.get(staticStrVal, MemoryOrder.Acquire));
                    b0.set(staticStrVal, Const.of("Banana"), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), staticStrVal);
                    // static obj: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.get(staticStrVal, MemoryOrder.Opaque));
                    b0.set(staticStrVal, Const.of("Pear"), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), staticStrVal);
                    b0.return_();
                });
            });
            zc.staticMethod("test5", mc -> {
                mc.body(b0 -> {
                    b0.line(nextLine());
                    LocalVar array = b0.localVar("array", b0.newArray(String.class, Const.of("Goodbye")));
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"), array.elem(0));
                    // array obj: volatile
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Goodbye"),
                            b0.get(array.elem(0), MemoryOrder.Volatile));
                    b0.set(array.elem(0), Const.of("Apple"), MemoryOrder.Volatile);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"), array.elem(0));
                    // array obj: acquire/release
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Apple"),
                            b0.get(array.elem(0), MemoryOrder.Acquire));
                    b0.set(array.elem(0), Const.of("Banana"), MemoryOrder.Release);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"), array.elem(0));
                    // array obj: opaque
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Banana"),
                            b0.get(array.elem(0), MemoryOrder.Opaque));
                    b0.set(array.elem(0), Const.of("Pear"), MemoryOrder.Opaque);
                    b0.line(nextLine());
                    b0.invokeStatic(assertEqualsL, Const.of("Pear"), array.elem(0));
                    b0.return_();
                });
            });
        });
        tcm.staticMethod(desc, "test0", Runnable.class).run();
        tcm.staticMethod(desc, "test1", Runnable.class).run();
        tcm.staticMethod(desc, "test2", Runnable.class).run();
        tcm.staticMethod(desc, "test3", Runnable.class).run();
        tcm.staticMethod(desc, "test4", Runnable.class).run();
        tcm.staticMethod(desc, "test5", Runnable.class).run();
    }

    private static final StackWalker SW = StackWalker.getInstance();

    // get the line # after the call to this method
    private static int nextLine() {
        return SW.walk(s -> s.skip(1).findFirst().orElseThrow()).getLineNumber() + 1;
    }

    // get my source file name
    private static String file() {
        return SW.walk(s -> s.skip(1).findFirst().orElseThrow()).getFileName();
    }
}
