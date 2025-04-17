package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class PrimitiveConstantsTest {
    @Test
    public void primitiveConstants() {
        test(() -> Const.of((byte) 1), "B");
        test(() -> Const.of((short) 2), "S");
        test(() -> Const.of('a'), "C");
        test(() -> Const.of(4), "I");
        test(() -> Const.of(5L), "J");
        test(() -> Const.of(6.0F), "F");
        test(() -> Const.of(7.0), "D");
        test(() -> Const.of(true), "Z");
    }

    @Test
    public void wrappersAsPrimitiveConstants() {
        test(() -> Const.of((Byte) (byte) 1), "B");
        test(() -> Const.of((Short) (short) 2), "S");
        test(() -> Const.of((Character) 'a'), "C");
        test(() -> Const.of((Integer) 4), "I");
        test(() -> Const.of((Long) 5L), "J");
        test(() -> Const.of((Float) 6.0F), "F");
        test(() -> Const.of((Double) 7.0), "D");
        test(() -> Const.of(Boolean.TRUE), "Z");
    }

    private void test(Supplier<Const> bytecode, String expectedResult) {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestPrimitiveConstants", cc -> {
            cc.staticMethod("returnDescriptor", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Const c = bytecode.get();
                    bc.return_(c.type().descriptorString());
                });
            });
        });
        assertEquals(expectedResult, tcm.staticMethod("returnDescriptor", Supplier.class).get());
    }
}
