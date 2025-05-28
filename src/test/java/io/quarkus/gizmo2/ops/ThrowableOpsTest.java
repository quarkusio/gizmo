package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.TestClassMaker;
import io.quarkus.gizmo2.creator.ops.ThrowableOps;

public class ThrowableOpsTest {

    @Test
    public void testThrowableAddSuppressed() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Throwables", cc -> {
            cc.staticMethod("test", mc -> {
                // static void test() {
                //    Exception e = new IllegalStateException("foo");
                //    e.addSuppressed(new NullPointerException("npe"));
                //    e.addSuppressed(new IllegalArgumentException("iae"));
                //    throw e;
                // }
                mc.body(bc -> {
                    var e = bc.localVar("e", bc.new_(IllegalStateException.class, Const.of("foo")));
                    ThrowableOps throwableOps = bc.withThrowable(e);
                    throwableOps.addSuppressed(bc.new_(NullPointerException.class, Const.of("npe")));
                    throwableOps.addSuppressed(bc.new_(IllegalArgumentException.class, Const.of("iae")));
                    bc.throw_(e);
                });
            });
        });
        IllegalStateException ise = assertThrows(IllegalStateException.class,
                () -> tcm.staticMethod("test", Runnable.class).run());
        Throwable[] suppressed = ise.getSuppressed();
        assertEquals(2, suppressed.length);
        assertEquals("npe", suppressed[0].getMessage());
        assertEquals("iae", suppressed[1].getMessage());
    }

}
