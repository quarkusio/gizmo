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
                    var byteVal = bc.box(Constant.of((byte) 123));
                    var shortVal = bc.box(Constant.of((short) 456));
                    var charVal = bc.box(Constant.of('a'));
                    var intVal = bc.box(Constant.of(65536));
                    var longVal = bc.box(Constant.of(Long.MAX_VALUE));
                    var floatVal = bc.box(Constant.of((float) 1.1));
                    var doubleVal = bc.box(Constant.of(1.2));
                    bc.return_(bc.listOf(boolVal, byteVal, shortVal, charVal, intVal, longVal, floatVal, doubleVal));
                });
            });
        });
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) tcm.staticMethod("test", Supplier.class).get();
        assertEquals(Boolean.TRUE, list.get(0));
        assertEquals((byte) 123, list.get(1));
        assertEquals((short) 456, list.get(2));
        assertEquals('a', list.get(3));
        assertEquals(65536, list.get(4));
        assertEquals(Long.MAX_VALUE, list.get(5));
        assertEquals(1.1F, list.get(6));
        assertEquals(1.2, list.get(7));
    }

    @Test
    public void testUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Unbox", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(Boolean bool, Byte b, Short s, Character c, Integer i, Long l, Float f, Double d) {
                //    if (!bool) {
                //       return 1;
                //    }
                //    if (b != (byte) 123) {
                //        return 2;
                //    }
                //    if (s != (short) 456) {
                //        return 3;
                //    }
                //    if (c != 'a') {
                //        return 4;
                //    }
                //    if (i != 10) {
                //       return 5;
                //    }
                //    if (l != 100l) {
                //       return 6;
                //    }
                //    if (f != (float) 1.2) {
                //       return 7;
                //    }
                //    if (d != 2.1) {
                //       return 8;
                //    }
                //    return 0;
                // }
                var bool = mc.parameter("bool", Boolean.class);
                var b = mc.parameter("b", Byte.class);
                var s = mc.parameter("s", Short.class);
                var c = mc.parameter("c", Character.class);
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
                    bc.unless(bc.unbox(bool), fail -> fail.return_(1));
                    bc.if_(bc.ne(bc.unbox(b), Constant.of((byte) 123)), fail -> fail.return_(2));
                    bc.if_(bc.ne(bc.unbox(s), Constant.of((short) 456)), fail -> fail.return_(3));
                    bc.if_(bc.ne(bc.unbox(c), Constant.of('a')), fail -> fail.return_(4));
                    bc.if_(bc.ne(bc.unbox(i), 10), fail -> fail.return_(5));
                    bc.if_(bc.ne(lu, 100L), fail -> fail.return_(6));
                    bc.if_(bc.ne(fu, 1.2F), fail -> fail.return_(7));
                    bc.if_(bc.ne(du, 2.1), fail -> fail.return_(8));
                    bc.return_(0);
                });
            });
        });
        assertEquals(0, tcm.staticMethod("test", BoxSupplier.class)
                .get(Boolean.TRUE, (byte) 123, (short) 456, 'a', 10, 100L, 1.2F, 2.1));
    }

    public interface BoxSupplier {
        int get(Boolean bool, Byte b, Short s, Character c, Integer i, Long l, Float f, Double d);
    }
}
