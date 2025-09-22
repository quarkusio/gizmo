package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.constant.ClassDesc;

import org.junit.jupiter.api.Test;

public class AssertTest {
    @Test
    public void assert_() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Assert"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.assert_(b1 -> {
                        b1.yield(Const.of(true));
                    }, "assertion");
                    b0.return_();
                });
            });
        });

        AssertionError e = assertThrows(AssertionError.class, () -> {
            tcm.staticMethod("hello", Runnable.class).run();
        });
        assertEquals("assertion", e.getMessage());
    }
}
