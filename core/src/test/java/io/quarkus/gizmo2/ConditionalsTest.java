package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public class ConditionalsTest {

    @Test
    public void testIf() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.If", cc -> {
            cc.staticMethod("test", mc -> {
                // static String test(boolean val) {
                //    int len = val.toString().length();
                //    if (len != 5) {
                //       return false;
                //    }
                //    return true;
                // }
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(bc -> {
                    var len = bc.localVar("len",
                            bc.invokeVirtual(MethodDesc.of(String.class, "length", int.class), val));
                    bc.if_(bc.ne(len, 5), BlockCreator::returnFalse);
                    bc.returnTrue();
                });
            });
        });
        assertFalse(tcm.staticMethod("test", BooleanFun.class).apply("foo"));
        assertTrue(tcm.staticMethod("test", BooleanFun.class).apply("fooos"));
    }

    @Test
    public void testIfNot() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfNot", cc -> {
            cc.staticMethod("test", mc -> {
                // static String test(boolean val) {
                //    int len = val.toString().length();
                //    if (len != 5) {
                //       return false;
                //    }
                //    return true;
                // }
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(bc -> {
                    var len = bc.localVar("len",
                            bc.invokeVirtual(MethodDesc.of(String.class, "length", int.class), val));
                    bc.ifNot(bc.eq(len, 5), BlockCreator::returnFalse);
                    bc.returnTrue();
                });
            });
        });
        assertFalse(tcm.staticMethod("test", BooleanFun.class).apply("foo"));
        assertTrue(tcm.staticMethod("test", BooleanFun.class).apply("fooos"));
    }

    @Test
    public void testCond() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cond", cc -> {
            cc.staticMethod("test", mc -> {
                // static boolean test(String val) {
                //    int len = val.length();
                //    return (len != 5) ? false : true;
                // }
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(b0 -> {
                    var len = b0.localVar("len",
                            b0.invokeVirtual(MethodDesc.of(String.class, "length", int.class), val));
                    Expr result = b0.cond(boolean.class, b0.ne(len, 5),
                            b1 -> b1.yield(Const.of(false)),
                            b1 -> b1.yield(Const.of(true)));
                    b0.return_(result);
                });
            });
        });
        assertFalse(tcm.staticMethod("test", BooleanFun.class).apply("foo"));
        assertTrue(tcm.staticMethod("test", BooleanFun.class).apply("fooos"));
    }

    @FunctionalInterface
    public interface BooleanFun {

        boolean apply(String val);

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIfInstanceOf() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfInstanceOf", cc -> {
            cc.staticMethod("test", mc -> {
                // static boolean test(Object param) {
                //    if (param instanceof CharSequence) {
                //        return true;
                //    }
                //    return false;
                // }
                mc.returning(boolean.class);
                ParamVar param = mc.parameter("param", Object.class);
                mc.body(b0 -> {
                    b0.ifInstanceOf(param, CharSequence.class, (b1, ignored) -> {
                        b1.return_(true);
                    });
                    b0.return_(false);
                });
            });
        });
        assertTrue(tcm.staticMethod("test", Predicate.class).test("foobar"));
        assertTrue(tcm.staticMethod("test", Predicate.class).test(new StringBuilder()));
        assertFalse(tcm.staticMethod("test", Predicate.class).test(123));
        assertFalse(tcm.staticMethod("test", Predicate.class).test(new Object()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIfNotInstanceOf() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfNotInstanceOf", cc -> {
            cc.staticMethod("test", mc -> {
                // static boolean test(Object param) {
                //    if (!(param instanceof CharSequence)) {
                //        return true;
                //    }
                //    return false;
                // }
                mc.returning(boolean.class);
                ParamVar param = mc.parameter("param", Object.class);
                mc.body(b0 -> {
                    b0.ifNotInstanceOf(param, CharSequence.class, b1 -> {
                        b1.return_(true);
                    });
                    b0.return_(false);
                });
            });
        });
        assertFalse(tcm.staticMethod("test", Predicate.class).test("foobar"));
        assertFalse(tcm.staticMethod("test", Predicate.class).test(new StringBuilder()));
        assertTrue(tcm.staticMethod("test", Predicate.class).test(123));
        assertTrue(tcm.staticMethod("test", Predicate.class).test(new Object()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testIfInstanceOfElse() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfInstanceOfElse", cc -> {
            cc.staticMethod("test", mc -> {
                // static boolean test(Object param) {
                //    if (param instanceof CharSequence) {
                //        return true;
                //    } else {
                //        return false;
                //    }
                // }
                mc.returning(boolean.class);
                ParamVar param = mc.parameter("param", Object.class);
                mc.body(b0 -> {
                    b0.ifInstanceOfElse(param, CharSequence.class, (b1, ignored) -> {
                        b1.return_(true);
                    }, b1 -> {
                        b1.return_(false);
                    });
                });
            });
        });
        assertTrue(tcm.staticMethod("test", Predicate.class).test("foobar"));
        assertTrue(tcm.staticMethod("test", Predicate.class).test(new StringBuilder()));
        assertFalse(tcm.staticMethod("test", Predicate.class).test(123));
        assertFalse(tcm.staticMethod("test", Predicate.class).test(new Object()));
    }

}
