package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class BoxUnboxTest {

    @Test
    public void testBox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Box", cc -> {
            cc.staticMethod("test", mc -> {
                // static List test() {
                //    Boolean boolVal = true;
                //    Integer intVal = 65536;
                //    Long longVal = 0x7fffffffffffffffL;
                //    Float floatVal = 1.1;
                //    Double doubleVal = 1.2;
                //    return List.of(boolVal, byteVal, charVal, shortVal, intVal, longVal, floatVal, doubleVal);
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    var boolVal = bc.box(Constant.of(true));
                    var intVal = bc.box(Constant.of(65536));
                    var longVal = bc.box(Constant.of(Long.MAX_VALUE));
                    var floatVal = bc.box(Constant.of((float) 1.1));
                    var doubleVal = bc.box(Constant.of(1.2));
                    bc.return_(bc.listOf(boolVal, intVal, longVal, floatVal, doubleVal));
                });
            });
        });
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) tcm.staticMethod("test", Supplier.class).get();
        assertEquals(Boolean.TRUE, list.get(0));
        assertEquals(Integer.valueOf(65536), list.get(1));
        assertEquals(Long.MAX_VALUE, list.get(2));
        assertEquals(Float.valueOf((float) 1.1), list.get(3));
        assertEquals(Double.valueOf(1.2), list.get(4));
    }

    @Test
    public void testUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Unbox", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(Boolean b, Integer i, Long l, Float f, Double d) {
                //    if (!b) {
                //       return 1;
                //    }
                //    if (i != 10) {
                //       return 2;
                //    }
                //    if (l != 100l) {
                //       return 3;
                //    }
                //    if (f != (float) 1.2) {
                //       return 4;
                //    }
                //    if (d != 2.1) {
                //       return 5;
                //    }
                //    return 0;
                // }
                var b = mc.parameter("b", Boolean.class);
                var i = mc.parameter("i", Integer.class);
                var l = mc.parameter("l", Long.class);
                var f = mc.parameter("f", Float.class);
                var d = mc.parameter("d", Double.class);
                mc.returning(int.class);
                mc.body(bc -> {
                    // WORKAROUND: we need to use local vars for types where unboxing involves cmp/cmpg
                    // TODO: file a new issue
                    var lu = bc.define("lv", bc.unbox(l));
                    var fu = bc.define("fv", bc.unbox(f));
                    var du = bc.define("dv", bc.unbox(d));
                    bc.unless(bc.unbox(b), fail -> fail.return_(1));
                    bc.if_(bc.ne(bc.unbox(i), 10), fail -> fail.return_(2));
                    bc.if_(bc.ne(lu, 100l), fail -> fail.return_(3));
                    bc.if_(bc.ne(fu, (float) 1.2), fail -> fail.return_(4));
                    bc.if_(bc.ne(du, 2.1), fail -> fail.return_(5));
                    bc.return_(0);
                });
            });
        });
        assertEquals(0, tcm.staticMethod("test", BoxSupplier.class).get(Boolean.TRUE, 10, 100l, (float) 1.2, 2.1));
    }

    public interface BoxSupplier {

        int get(Boolean b, Integer i, Long l, Float f, Double d);

    }

}