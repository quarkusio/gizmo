package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public final class CastTest {
    // n.b. box/unbox casts are covered by BoxUnboxTest

    @Test
    public void testCheckCast() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CheckCast", cc -> {
            cc.staticMethod("test", smc -> {
                smc.returning(String.class);
                ParamVar input = smc.parameter("input", Object.class);
                smc.body(b0 -> {
                    b0.return_(b0.cast(input, String.class));
                });
            });
        });
        assertEquals("Hello", tcm.staticMethod("test", CheckCast.class).run("Hello"));
    }

    public interface CheckCast {
        String run(Object input);
    }

    @Test
    public void testUnsafeCast() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.UnsafeCast", cc -> {
            cc.staticMethod("test", smc -> {
                smc.returning(Object.class);
                ParamVar input = smc.parameter("input", String.class);
                smc.body(b0 -> {
                    b0.return_(b0.unsafeCast(input, Object.class));
                });
            });
        });
        assertEquals("Hello", tcm.staticMethod("test", UnsafeCast.class).run("Hello"));
    }

    public interface UnsafeCast {
        Object run(String input);
    }

    @Test
    public void testPrimitiveCast() {
        // we don't have to test every combination here; it's implemented by the classfile library
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.PrimitiveCast", cc -> {
            cc.staticMethod("test0", smc -> {
                smc.returning(byte.class);
                ParamVar input = smc.parameter("input", int.class);
                smc.body(b0 -> {
                    b0.return_(b0.cast(input, byte.class));
                });
            });
            cc.staticMethod("test1", smc -> {
                smc.returning(short.class);
                ParamVar input = smc.parameter("input", int.class);
                smc.body(b0 -> {
                    b0.return_(b0.cast(input, short.class));
                });
            });
        });
        assertEquals(0x50, tcm.staticMethod("test0", IntToByte.class).run(0xff443350));
        assertEquals(0x3350, tcm.staticMethod("test1", IntToShort.class).run(0xff443350));
    }

    public interface IntToByte {
        byte run(int input);
    }

    public interface IntToShort {
        short run(int input);
    }
}
