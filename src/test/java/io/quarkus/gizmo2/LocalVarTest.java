package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class LocalVarTest {
    @Test
    public void initializedVar() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.InitializedVar", cc -> {
            cc.staticMethod("hello", mc -> {
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    LocalVar hello = bc.declare("hello", Const.of("Hello World!"));
                    bc.return_(hello);
                });
            });
        });
        assertEquals("Hello World!", tcm.staticMethod("hello", Supplier.class).get());
    }

    @Test
    public void noninitializedVar() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NoninitializedVar", cc -> {
            cc.staticMethod("hello", mc -> {
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    LocalVar hello = bc.declare("hello", String.class);
                    bc.return_(hello);
                });
            });
        });
        assertNull(tcm.staticMethod("hello", Supplier.class).get());
    }
}
