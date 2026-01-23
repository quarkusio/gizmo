package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.constant.ClassDesc;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.LongUnaryOperator;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.testing.TestClassMaker;

public class ArithmeticTest {
    @FunctionalInterface
    public interface IntToLongFunction {
        long apply(int a);
    }

    @FunctionalInterface
    public interface LongToDoubleFunction {
        double apply(long a);
    }

    @FunctionalInterface
    public interface LongIntToLongFunction {
        long apply(long a, int b);
    }

    @FunctionalInterface
    public interface IntLongToLongFunction {
        long apply(int a, long b);
    }

    @FunctionalInterface
    public interface IntIntToLongFunction {
        long apply(int a, int b);
    }

    @FunctionalInterface
    public interface LongDoubleToDoubleFunction {
        double apply(long a, double b);
    }

    @Test
    public void intArithmetic() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Int", cc -> {
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
        }));
        assertEquals(-2, tcm.staticMethod(clazz, "neg", IntUnaryOperator.class).applyAsInt(2));
        assertEquals(2, tcm.staticMethod(clazz, "neg", IntUnaryOperator.class).applyAsInt(-2));
        assertEquals(2 + 3, tcm.staticMethod(clazz, "add", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 - 3, tcm.staticMethod(clazz, "sub", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 * 3, tcm.staticMethod(clazz, "mul", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 / 3, tcm.staticMethod(clazz, "div", IntBinaryOperator.class).applyAsInt(2, 3));
        assertThrows(ArithmeticException.class, () -> {
            tcm.staticMethod(clazz, "div", IntBinaryOperator.class).applyAsInt(2, 0);
        });
        assertEquals(2 % 3, tcm.staticMethod(clazz, "rem", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 & 3, tcm.staticMethod(clazz, "and", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 | 3, tcm.staticMethod(clazz, "or", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 ^ 3, tcm.staticMethod(clazz, "xor", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(2 << 3, tcm.staticMethod(clazz, "shl", IntBinaryOperator.class).applyAsInt(2, 3));
        assertEquals(200 >> 3, tcm.staticMethod(clazz, "shr", IntBinaryOperator.class).applyAsInt(200, 3));
        assertEquals(-200 >> 3, tcm.staticMethod(clazz, "shr", IntBinaryOperator.class).applyAsInt(-200, 3));
        assertEquals(200 >>> 3, tcm.staticMethod(clazz, "ushr", IntBinaryOperator.class).applyAsInt(200, 3));
        assertEquals(-200 >>> 3, tcm.staticMethod(clazz, "ushr", IntBinaryOperator.class).applyAsInt(-200, 3));
    }

    @Test
    public void longArithmetic() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc xxx = g.class_("io.quarkus.gizmo2.Long", cc -> {
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
        Class<?> clazz = tcm.loadClass(xxx);
        assertEquals(-2L, tcm.staticMethod(clazz, "neg", LongUnaryOperator.class).applyAsLong(2L));
        assertEquals(2L, tcm.staticMethod(clazz, "neg", LongUnaryOperator.class).applyAsLong(-2L));
        assertEquals(2L + 3L, tcm.staticMethod(clazz, "add", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L - 3L, tcm.staticMethod(clazz, "sub", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L * 3L, tcm.staticMethod(clazz, "mul", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L / 3L, tcm.staticMethod(clazz, "div", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertThrows(ArithmeticException.class, () -> {
            tcm.staticMethod(clazz, "div", LongBinaryOperator.class).applyAsLong(2L, 0L);
        });
        assertEquals(2L % 3L, tcm.staticMethod(clazz, "rem", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L & 3L, tcm.staticMethod(clazz, "and", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L | 3L, tcm.staticMethod(clazz, "or", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L ^ 3L, tcm.staticMethod(clazz, "xor", LongBinaryOperator.class).applyAsLong(2L, 3L));
        assertEquals(2L << 3L, tcm.staticMethod(clazz, "shl", LongIntToLongFunction.class).apply(2L, 3));
        assertEquals(200L >> 3L, tcm.staticMethod(clazz, "shr", LongIntToLongFunction.class).apply(200L, 3));
        assertEquals(-200L >> 3L, tcm.staticMethod(clazz, "shr", LongIntToLongFunction.class).apply(-200L, 3));
        assertEquals(200L >>> 3L, tcm.staticMethod(clazz, "ushr", LongIntToLongFunction.class).apply(200L, 3));
        assertEquals(-200L >>> 3L, tcm.staticMethod(clazz, "ushr", LongIntToLongFunction.class).apply(-200L, 3));
    }

    @Test
    public void intLongArithmetic() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Long", cc -> {
            cc.staticMethod("neg", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(a));
                });
            });
            cc.staticMethod("add", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.add(a, b));
                });
            });
            cc.staticMethod("sub", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.sub(a, b));
                });
            });
            cc.staticMethod("mul", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.mul(a, b));
                });
            });
            cc.staticMethod("div", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.div(a, b));
                });
            });
            cc.staticMethod("rem", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.rem(a, b));
                });
            });
            cc.staticMethod("and", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.and(a, b));
                });
            });
            cc.staticMethod("or", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.or(a, b));
                });
            });
            cc.staticMethod("xor", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", long.class);
                mc.body(bc -> {
                    bc.return_(bc.xor(a, b));
                });
            });
            cc.staticMethod("shl", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shl(a, b));
                });
            });
            cc.staticMethod("shr", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.shr(a, b));
                });
            });
            cc.staticMethod("ushr", mc -> {
                mc.returning(long.class);
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.body(bc -> {
                    bc.return_(bc.ushr(a, b));
                });
            });
        }));
        assertEquals(-2L, tcm.staticMethod(clazz, "neg", IntToLongFunction.class).apply(2));
        assertEquals(2L, tcm.staticMethod(clazz, "neg", IntToLongFunction.class).apply(-2));
        assertEquals(2L + 3L, tcm.staticMethod(clazz, "add", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2L - 3L, tcm.staticMethod(clazz, "sub", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2L * 3L, tcm.staticMethod(clazz, "mul", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2L / 3L, tcm.staticMethod(clazz, "div", IntLongToLongFunction.class).apply(2, 3L));
        assertThrows(ArithmeticException.class, () -> {
            tcm.staticMethod(clazz, "div", IntLongToLongFunction.class).apply(2, 0L);
        });
        assertEquals(2 % 3, tcm.staticMethod(clazz, "rem", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2 & 3, tcm.staticMethod(clazz, "and", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2 | 3, tcm.staticMethod(clazz, "or", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2 ^ 3, tcm.staticMethod(clazz, "xor", IntLongToLongFunction.class).apply(2, 3L));
        assertEquals(2 << 3, tcm.staticMethod(clazz, "shl", IntIntToLongFunction.class).apply(2, 3));
        assertEquals(200 >> 3, tcm.staticMethod(clazz, "shr", IntIntToLongFunction.class).apply(200, 3));
        assertEquals(-200 >> 3, tcm.staticMethod(clazz, "shr", IntIntToLongFunction.class).apply(-200, 3));
        assertEquals(200 >>> 3, tcm.staticMethod(clazz, "ushr", IntIntToLongFunction.class).apply(200, 3));
        assertEquals(-200 >>> 3, tcm.staticMethod(clazz, "ushr", IntIntToLongFunction.class).apply(-200, 3));
    }

    @Test
    public void doubleArithmetic() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Double", cc -> {
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
        }));
        assertEquals(-2.0, tcm.staticMethod(clazz, "neg", DoubleUnaryOperator.class).applyAsDouble(2.0));
        assertEquals(2.0, tcm.staticMethod(clazz, "neg", DoubleUnaryOperator.class).applyAsDouble(-2.0));
        assertEquals(2.0 + 3.0, tcm.staticMethod(clazz, "add", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 - 3.0, tcm.staticMethod(clazz, "sub", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 * 3.0, tcm.staticMethod(clazz, "mul", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 / 3.0, tcm.staticMethod(clazz, "div", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
        assertEquals(2.0 / 0.0, tcm.staticMethod(clazz, "div", DoubleBinaryOperator.class).applyAsDouble(2.0, 0.0));
        assertEquals(-2.0 / 0.0, tcm.staticMethod(clazz, "div", DoubleBinaryOperator.class).applyAsDouble(-2.0, 0.0));
        assertEquals(2.0 % 3.0, tcm.staticMethod(clazz, "rem", DoubleBinaryOperator.class).applyAsDouble(2.0, 3.0));
    }

    @Test
    public void longDoubleArithmetic() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Double", cc -> {
            cc.staticMethod("neg", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(a));
                });
            });
            cc.staticMethod("add", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.add(a, b));
                });
            });
            cc.staticMethod("sub", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.sub(a, b));
                });
            });
            cc.staticMethod("mul", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.mul(a, b));
                });
            });
            cc.staticMethod("div", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.div(a, b));
                });
            });
            cc.staticMethod("rem", mc -> {
                mc.returning(double.class);
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.return_(bc.rem(a, b));
                });
            });
        }));
        assertEquals(-2.0, tcm.staticMethod(clazz, "neg", LongToDoubleFunction.class).apply(2L));
        assertEquals(2.0, tcm.staticMethod(clazz, "neg", LongToDoubleFunction.class).apply(-2L));
        assertEquals(2L + 3.0, tcm.staticMethod(clazz, "add", LongDoubleToDoubleFunction.class).apply(2L, 3.0));
        assertEquals(2L - 3.0, tcm.staticMethod(clazz, "sub", LongDoubleToDoubleFunction.class).apply(2L, 3.0));
        assertEquals(2L * 3.0, tcm.staticMethod(clazz, "mul", LongDoubleToDoubleFunction.class).apply(2L, 3.0));
        assertEquals(2L / 3.0, tcm.staticMethod(clazz, "div", LongDoubleToDoubleFunction.class).apply(2L, 3.0));
        assertEquals(2L / 0.0, tcm.staticMethod(clazz, "div", LongDoubleToDoubleFunction.class).apply(2L, 0.0));
        assertEquals(-2L / 0.0, tcm.staticMethod(clazz, "div", LongDoubleToDoubleFunction.class).apply(-2L, 0.0));
        assertEquals(2L % 3.0, tcm.staticMethod(clazz, "rem", LongDoubleToDoubleFunction.class).apply(2L, 3.0));
    }
}
