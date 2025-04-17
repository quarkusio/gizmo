package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
                    var len = bc.define("len",
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
                    var len = bc.define("len",
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
    public void testSelectExpr() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.SelectExpr", cc -> {
            cc.staticMethod("test", mc -> {
                // static boolean test(String val) {
                //    int len = val.length();
                //    return (len != 5) ? false : true;
                // }
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(b0 -> {
                    var len = b0.define("len",
                            b0.invokeVirtual(MethodDesc.of(String.class, "length", int.class), val));
                    Expr result = b0.selectExpr(boolean.class, b0.ne(len, 5),
                            b1 -> b1.yield(Constant.of(false)),
                            b1 -> b1.yield(Constant.of(true)));
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

}
