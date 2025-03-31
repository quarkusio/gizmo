package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import org.junit.jupiter.api.Test;

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
                con.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), con.this_());
                    var bravo = con.this_().field(bravoDesc);
                    bc.set(bravo, Constant.of("charlie"));
                    bc.return_();
                });
            });
            cc.method("test", mc -> {
                // int test() {
                //    return bravo.length();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var b = bc.get(mc.this_().field(bravoDesc));
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
            cc.method("test", mc -> {
                // int test() {
                //    return bravo.length();
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
