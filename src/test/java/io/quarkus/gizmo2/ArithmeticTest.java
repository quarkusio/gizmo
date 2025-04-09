package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import org.junit.jupiter.api.Test;

public class ArithmeticTest {
    @Test
    public void intArithmetic() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Int", cc -> {
            cc.staticMethod("neg", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(a));
                });
            });
            cc.staticMethod("add", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.add(a, b));
                });
            });
            cc.staticMethod("sub", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.sub(a, b));
                });
            });
            cc.staticMethod("mul", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.mul(a, b));
                });
            });
            cc.staticMethod("div", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.div(a, b));
                });
            });
            cc.staticMethod("rem", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.rem(a, b));
                });
            });
            cc.staticMethod("and", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.and(a, b));
                });
            });
            cc.staticMethod("or", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.or(a, b));
                });
            });
            cc.staticMethod("xor", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.xor(a, b));
                });
            });
            cc.staticMethod("shl", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shl(a, b));
                });
            });
            cc.staticMethod("shr", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shr(a, b));
                });
            });
            cc.staticMethod("ushr", mc -> {
                mc.returning(int.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.ushr(a, b));
                });
            });
        });
        assertEquals(-2, tcm.staticMethod("neg", IntUnaryOperator.class).applyAsInt(2));
        assertEquals(2, tcm.staticMethod("neg", IntUnaryOperator.class).applyAsInt(-2));
        assertEquals(2 + 3, tcm.staticMethod("add", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 - 3, tcm.staticMethod("sub", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 * 3, tcm.staticMethod("mul", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 / 3, tcm.staticMethod("div", IntBinaryOperator.class).applyAsInt(2, 3));
        assertThrows(ArithmeticException.class, () -> {
            tcm.staticMethod("div", IntBinaryOperator.class).applyAsInt(2, 0);
        });
        assertEquals(2 % 3, tcm.staticMethod("rem", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 & 3, tcm.staticMethod("and", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 | 3, tcm.staticMethod("or", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 ^ 3, tcm.staticMethod("xor", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 << 3, tcm.staticMethod("shl", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(200 >> 3, tcm.staticMethod("shr", IntBinaryOperator.class).applyAsInt(200, 3));
        assertEquals(-200 >> 3, tcm.staticMethod("shr", IntBinaryOperator.class).applyAsInt(-200, 3));
        assertEquals(200 >>> 3, tcm.staticMethod("ushr", IntBinaryOperator.class).applyAsInt(200, 3));
        assertEquals(-200 >>> 3, tcm.staticMethod("ushr", IntBinaryOperator.class).applyAsInt(-200, 3));
    }

    @Test
    public void longArithmetic() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Long", cc -> {
            cc.staticMethod("neg", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(a));
                });
            });
            cc.staticMethod("add", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.add(a, b));
                });
            });
            cc.staticMethod("sub", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.sub(a, b));
                });
            });
            cc.staticMethod("mul", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.mul(a, b));
                });
            });
            cc.staticMethod("div", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.div(a, b));
                });
            });
            cc.staticMethod("rem", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.rem(a, b));
                });
            });
            cc.staticMethod("and", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.and(a, b));
                });
            });
            cc.staticMethod("or", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.or(a, b));
                });
            });
            cc.staticMethod("xor", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.xor(a, b));
                });
            });
            cc.staticMethod("shl", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shl(a, b));
                });
            });
            cc.staticMethod("shr", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shr(a, b));
                });
            });
            cc.staticMethod("ushr", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.ushr(a, b));
                });
            });
        });
        assertEquals(-2L, tcm.staticMethod("neg", LongUnaryOperator.class).applyAsLong(2L));
        assertEquals(2L, tcm.staticMethod("neg", LongUnaryOperator.class).applyAsLong(-2L));
        assertEquals(2L + 3L, tcm.staticMethod("add", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L - 3L, tcm.staticMethod("sub", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L * 3L, tcm.staticMethod("mul", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L / 3L, tcm.staticMethod("div", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertThrows(ArithmeticException.class, () -> {
            tcm.staticMethod("div", LongBinaryOperator.class).applyAsLong(2L, 0L);
        });
        assertEquals(2L % 3L, tcm.staticMethod("rem", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L & 3L, tcm.staticMethod("and", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L | 3L, tcm.staticMethod("or", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L ^ 3L, tcm.staticMethod("xor", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L << 3L, tcm.staticMethod("shl", LongIntToLongFunction.class).apply(2L, 3));
        assertEquals(200L >> 3L, tcm.staticMethod("shr", LongIntToLongFunction.class).apply(200L, 3));
        assertEquals(-200L >> 3L, tcm.staticMethod("shr", LongIntToLongFunction.class).apply(-200L, 3));
        assertEquals(200L >>> 3L, tcm.staticMethod("ushr", LongIntToLongFunction.class).apply(200L, 3));
        assertEquals(-200L >>> 3L, tcm.staticMethod("ushr", LongIntToLongFunction.class).apply(-200L, 3));
    }

    @FunctionalInterface
    public interface LongIntToLongFunction {
        long apply(long a, int b);
    }

    @Test
    public void doubleArithmetic() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Double", cc -> {
            cc.staticMethod("neg", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(a));
                });
            });
            cc.staticMethod("add", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.add(a, b));
                });
            });
            cc.staticMethod("sub", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.sub(a, b));
                });
            });
            cc.staticMethod("mul", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.mul(a, b));
                });
            });
            cc.staticMethod("div", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.div(a, b));
                });
            });
            cc.staticMethod("rem", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", double.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.rem(a, b));
                });
            });
        });
        assertEquals(-2.0, tcm.staticMethod("neg", DoubleUnaryOperator.class).applyAsDouble(2.0));
        assertEquals(2.0, tcm.staticMethod("neg", DoubleUnaryOperator.class).applyAsDouble(-2.0));
        assertEquals(2.0 + 3.0, tcm.staticMethod("add", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 - 3.0, tcm.staticMethod("sub", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 * 3.0, tcm.staticMethod("mul", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 / 3.0, tcm.staticMethod("div", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 / 0.0, tcm.staticMethod("div", DoubleBinaryOperator.class).applyAsDouble(2.0, 0.0));
        assertEquals(-2.0 / 0.0, tcm.staticMethod("div", DoubleBinaryOperator.class).applyAsDouble(-2.0, 0.0));
        assertEquals(2.0 % 3.0, tcm.staticMethod("rem", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
    }
}
