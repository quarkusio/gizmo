package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class ConstantsTest {
    @Test
    public void primitiveConstants() {
        test(() -> Const.of((byte) 1), "1|B");
        test(() -> Const.of((short) 2), "2|S");
        test(() -> Const.of('a'), "a|C");
        test(() -> Const.of(4), "4|I");
        test(() -> Const.of(5L), "5|J");
        test(() -> Const.of(6.0F), "6.0|F");
        test(() -> Const.of(7.0), "7.0|D");
        test(() -> Const.of(true), "true|Z");
    }

    @Test
    public void wrappersAsPrimitiveConstants() {
        test(() -> Const.of((Byte) (byte) 1), "1|B");
        test(() -> Const.of((Short) (short) 2), "2|S");
        test(() -> Const.of((Character) 'a'), "a|C");
        test(() -> Const.of((Integer) 4), "4|I");
        test(() -> Const.of((Long) 5L), "5|J");
        test(() -> Const.of((Float) 6.0F), "6.0|F");
        test(() -> Const.of((Double) 7.0), "7.0|D");
        test(() -> Const.of(Boolean.TRUE), "true|Z");
    }

    @Test
    public void defaultConstants() {
        test(() -> Const.ofDefault(byte.class), "0|B");
        test(() -> Const.ofDefault(short.class), "0|S");
        test(() -> Const.ofDefault(char.class), "\0|C");
        test(() -> Const.ofDefault(int.class), "0|I");
        test(() -> Const.ofDefault(long.class), "0|J");
        test(() -> Const.ofDefault(float.class), "0.0|F");
        test(() -> Const.ofDefault(double.class), "0.0|D");
        test(() -> Const.ofDefault(boolean.class), "false|Z");

        test(() -> Const.ofDefault(String.class), "null|Ljava/lang/String;");
        test(() -> Const.ofDefault(Object.class), "null|Ljava/lang/Object;");

        test(() -> Const.ofDefault(String[].class), "null|[Ljava/lang/String;");
        test(() -> Const.ofDefault(Object[][].class), "null|[[Ljava/lang/Object;");
    }

    private void test(Supplier<Const> bytecode, String expectedResult) {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestConstants", cc -> {
            cc.staticMethod("returnValueAndDescriptor", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Const c = bytecode.get();
                    bc.return_(bc.withNewStringBuilder().append(c).append('|')
                            .append(c.type().descriptorString()).objToString());
                });
            });
        });
        assertEquals(expectedResult, tcm.staticMethod("returnValueAndDescriptor", Supplier.class).get());
    }
}
