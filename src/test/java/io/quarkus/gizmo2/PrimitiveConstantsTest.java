package io.quarkus.gizmo2;

import org.junit.jupiter.api.Test;

import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrimitiveConstantsTest {
    @Test
    public void primitiveConstants() {
        test(() -> Constant.of((byte) 1), "B");
        test(() -> Constant.of((short) 2), "S");
        test(() -> Constant.of('a'), "C");
        test(() -> Constant.of(4), "I");
        test(() -> Constant.of(5L), "J");
        test(() -> Constant.of(6.0F), "F");
        test(() -> Constant.of(7.0), "D");
        test(() -> Constant.of(true), "Z");
    }

    @Test
    public void wrappersAsPrimitiveConstants() {
        test(() -> Constant.of((Byte) (byte) 1), "B");
        test(() -> Constant.of((Short) (short) 2), "S");
        test(() -> Constant.of((Character) 'a'), "C");
        test(() -> Constant.of((Integer) 4), "I");
        test(() -> Constant.of((Long) 5L), "J");
        test(() -> Constant.of((Float) 6.0F), "F");
        test(() -> Constant.of((Double) 7.0), "D");
        test(() -> Constant.of(Boolean.TRUE), "Z");
    }

    private void test(Supplier<Constant> bytecode, String expectedResult) {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestPrimitiveConstants", cc -> {
            cc.staticMethod("returnDescriptor", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Constant c = bytecode.get();
                    bc.return_(c.type().descriptorString());
                });
            });
        });
        assertEquals(expectedResult, tcm.staticMethod("returnDescriptor", Supplier.class).get());
    }
}
