package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

public class FieldAccessTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            FieldDesc bravoDesc = cc.field("bravo", fc -> {
                fc.withType(String.class);
            });
            cc.constructor(con -> {
                // this.bravo = "charlie";
                Var this_ = con.this_();
                con.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), this_);
                    var bravo = this_.field(bravoDesc);
                    bc.set(bravo, Constant.of("charlie"));
                    bc.return_();
                });
            });
            cc.method("test", mc -> {
                // int test() {
                //    return bravo.length();
                // }
                mc.returning(int.class);
                Var this_ = mc.this_();
                mc.body(bc -> {
                    var b = bc.get(this_.field(bravoDesc));
                    var length = bc.withString(b).length();
                    bc.return_(length);
                });
            });
        });
        assertEquals(7, tcm.instanceMethod("test", ToIntFunction.class).applyAsInt(tcm.constructor(Supplier.class).get()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            var bravo = cc.staticField("bravo", fc -> {
                fc.withType(String.class);
                fc.withInitial(Constant.of("charlie"));
            });
            cc.defaultConstructor();
            cc.staticMethod("test", mc -> {
                // Object test(Object input) {
                //    if (input != null) {
                //       Alpha.bravo = Alpha.bravo.concat("s");
                //    }
                //    if (input == null) {
                //       Alpha.bravo = "NULL";
                //    }
                //    return Alpha.bravo.length();
                // }
                mc.returning(Object.class);
                ParamVar input = mc.parameter("input", Object.class);
                mc.body(bc -> {
                    bc.ifNotNull(input, bc1 -> {
                        var b = bc1.get(bravo);
                        var newVal = bc1.withString(b).concat(Constant.of("s"));
                        bc1.set(bravo, newVal);
                    });
                    bc.ifNull(input, bc1 -> {
                        bc1.setStaticField(bravo.desc(), Constant.of("NULL"));
                    });
                    bc.return_(bc.getStaticField(bravo.desc()));
                });
            });
        });
        assertEquals("charlies", tcm.staticMethod("test", Function.class).apply("foo").toString());
        assertEquals("NULL", tcm.staticMethod("test", Function.class).apply(null).toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstantField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            var bravo = cc.constantField("BRAVO", Constant.of("charlie"));
            cc.defaultConstructor();
            cc.method("test", mc -> {
                // int test() {
                //    return BRAVO.length();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var b = bc.get(bravo);
                    var length = bc.withString(b).length();
                    bc.return_(length);
                });
            });
        });
        assertEquals(7, tcm.instanceMethod("test", ToIntFunction.class).applyAsInt(tcm.constructor(Supplier.class).get()));
    }
}
