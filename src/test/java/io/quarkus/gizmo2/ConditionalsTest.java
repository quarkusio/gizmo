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
                // static Object test(Object val) {
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
    public void testUnless() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Unless", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test(Object val) {
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
                    bc.unless(bc.eq(len, 5), BlockCreator::returnFalse);
                    bc.returnTrue();
                });
            });
        });
        assertFalse(tcm.staticMethod("test", BooleanFun.class).apply("foo"));
        assertTrue(tcm.staticMethod("test", BooleanFun.class).apply("fooos"));
    }

    public interface BooleanFun {

        boolean apply(String val);

    }

}
