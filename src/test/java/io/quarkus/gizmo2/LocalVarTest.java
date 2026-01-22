package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.constant.ClassDesc;
import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.testing.TestClassMaker;

public class LocalVarTest {
    @Test
    public void booleanVar() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc testClass = g.class_("io.quarkus.gizmo2.BooleanVar", cc -> {
            cc.staticMethod("get", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    LocalVar lv = bc.localVar("lv", true);
                    bc.return_(lv);
                });
            });
        });
        assertTrue(tcm.staticMethod(testClass, "get", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void intVar() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc testClass = g.class_("io.quarkus.gizmo2.IntVar", cc -> {
            cc.staticMethod("get", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar lv = bc.localVar("lv", 42);
                    bc.return_(lv);
                });
            });
        });
        assertEquals(42, tcm.staticMethod(testClass, "get", IntSupplier.class).getAsInt());
    }

    @Test
    public void longVar() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc testClass = g.class_("io.quarkus.gizmo2.LongVar", cc -> {
            cc.staticMethod("get", mc -> {
                mc.returning(long.class);
                mc.body(bc -> {
                    LocalVar lv = bc.localVar("lv", 42L);
                    bc.return_(lv);
                });
            });
        });
        assertEquals(42L, tcm.staticMethod(testClass, "get", LongSupplier.class).getAsLong());
    }

    @Test
    public void doubleVar() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc testClass = g.class_("io.quarkus.gizmo2.DoubleVar", cc -> {
            cc.staticMethod("get", mc -> {
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar lv = bc.localVar("lv", 42.0);
                    bc.return_(lv);
                });
            });
        });
        assertEquals(42.0, tcm.staticMethod(testClass, "get", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void stringVar() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc testClass = g.class_("io.quarkus.gizmo2.StringVar", cc -> {
            cc.staticMethod("get", mc -> {
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    LocalVar lv = bc.localVar("lv", "Hello World!");
                    bc.return_(lv);
                });
            });
        });
        assertEquals("Hello World!", tcm.staticMethod(testClass, "get", Supplier.class).get());
    }
}
