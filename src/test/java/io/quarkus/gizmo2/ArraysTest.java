package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.MethodDesc;

public class ArraysTest {

    @Test
    public void testArrayVariableLength() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArrayOps", cc -> {
            cc.staticMethod("runTest", mc -> {
                // static int runTest() {
                //    String[] arr = new String[5];
                //    arr[0] = "foo";
                //    arr[1] = "bar";
                //    if (!arr[1].equals("bar")) {
                //       return -1;
                //    }
                //    return arr.length;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var arr = bc.localVar("arr", bc.newEmptyArray(String.class, Const.of(5)));
                    bc.set(arr.elem(0), Const.of("foo"));
                    bc.set(arr.elem(Const.of(1)), Const.of("bar"));
                    bc.ifNot(bc.exprEquals(arr.elem(Integer.valueOf(1)), Const.of("bar")), fail -> fail.return_(-1));
                    bc.return_(arr.length());
                });
            });
        });
        assertEquals(5, tcm.staticMethod("runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void createPrimitiveArray() {
        testCreateArray(bc -> bc.newArray(boolean.class, Const.of(true), Const.of(false)), "[true, false]");
        testCreateArray(bc -> bc.newArray(byte.class, Const.of((byte) 1), Const.of((byte) 2)), "[1, 2]");
        testCreateArray(bc -> bc.newArray(short.class, Const.of((short) 3), Const.of((short) 4)), "[3, 4]");
        testCreateArray(bc -> bc.newArray(char.class, Const.of('a'), Const.of('b')), "[a, b]");
        testCreateArray(bc -> bc.newArray(int.class, Const.of(7), Const.of(8)), "[7, 8]");
        testCreateArray(bc -> bc.newArray(long.class, Const.of(9L), Const.of(10L)), "[9, 10]");
        testCreateArray(bc -> bc.newArray(float.class, Const.of(11.0F), Const.of(12.0F)), "[11.0, 12.0]");
        testCreateArray(bc -> bc.newArray(double.class, Const.of(13.0), Const.of(14.0)), "[13.0, 14.0]");
    }

    @Test
    public void createMultiDimensionalPrimitiveArray() {
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(boolean.class, Const.of(true), Const.of(false));
            Expr a2 = bc.newArray(boolean.class, Const.of(false), Const.of(true));
            return bc.newArray(boolean[].class, a1, a2);
        }, "[[true, false], [false, true]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(byte.class, Const.of((byte) 1), Const.of((byte) 2));
            Expr a2 = bc.newArray(byte.class, Const.of((byte) 3), Const.of((byte) 4));
            Expr a3 = bc.newArray(byte.class, Const.of((byte) 5), Const.of((byte) 6));
            return bc.newArray(byte[].class, a1, a2, a3);
        }, "[[1, 2], [3, 4], [5, 6]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(short.class, Const.of((short) 7));
            Expr a2 = bc.newArray(short.class, Const.of((short) 8), Const.of((short) 9));
            return bc.newArray(short[].class, a1, a2);
        }, "[[7], [8, 9]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(char.class, Const.of('a'));
            Expr a2 = bc.newArray(char.class, Const.of('b'), Const.of('c'));
            Expr a3 = bc.newArray(char.class);
            Expr a4 = bc.newArray(char.class);
            Expr a5 = bc.newArray(char.class, Const.of('d'));
            return bc.newArray(char[].class, a1, a2, a3, a4, a5);
        }, "[[a], [b, c], [], [], [d]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(int.class, Const.of(10));
            Expr a2 = bc.newArray(int.class, Const.of(11), Const.of(12));
            Expr a3 = bc.newArray(int.class, Const.of(13), Const.of(14), Const.of(15));
            return bc.newArray(int[].class, a1, a2, a3);
        }, "[[10], [11, 12], [13, 14, 15]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(long.class, Const.of(16L));
            Expr a2 = bc.newArray(long.class);
            Expr a3 = bc.newArray(long.class, Const.of(17L));
            return bc.newArray(long[].class, a1, a2, a3);
        }, "[[16], [], [17]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(float.class, Const.of(18.0F));
            Expr a2 = bc.newArray(float.class);
            Expr a3 = bc.newArray(float[].class, a1, a2);
            Expr a4 = bc.newArray(float.class, Const.of(19.0F));
            Expr a5 = bc.newArray(float.class, Const.of(20.0F), Const.of(21.0F));
            Expr a6 = bc.newArray(float[].class, a4, a5);
            return bc.newArray(float[][].class, a3, a6);
        }, "[[[18.0], []], [[19.0], [20.0, 21.0]]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(double.class, Const.of(22.0));
            Expr a2 = bc.newArray(double.class, Const.of(23.0), Const.of(24.0));
            Expr a3 = bc.newArray(double[].class, a1, a2);
            Expr a4 = bc.newArray(double.class, Const.of(25.0), Const.of(26.0));
            Expr a5 = bc.newArray(double.class, Const.of(27.0));
            Expr a6 = bc.newArray(double[].class, a4, a5);
            Expr a7 = bc.newArray(double.class, Const.of(27.0), Const.of(28.0));
            Expr a8 = bc.newArray(double.class);
            Expr a9 = bc.newArray(double[].class, a7, a8);
            return bc.newArray(double[][].class, a3, a6, a9);
        }, "[[[22.0], [23.0, 24.0]], [[25.0, 26.0], [27.0]], [[27.0, 28.0], []]]");
    }

    @Test
    public void createReferenceArray() {
        testCreateArray(bc -> bc.newArray(String.class, Const.of("ab"), Const.of("cd")),
                "[ab, cd]");
        testCreateArray(bc -> {
            Expr ef = bc.new_(StringBuilder.class, Const.of("ef"));
            return bc.newArray(CharSequence.class, ef, Const.of("gh"));
        }, "[ef, gh]");
        testCreateArray(bc -> {
            Expr num = bc.new_(Integer.class, Const.of(123));
            return bc.newArray(Object.class, Const.of("ij"), num, Const.of("klm"));
        }, "[ij, 123, klm]");
    }

    @Test
    public void createMultiDimensionalReferenceArrays() {
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(String.class, Const.of("ab"), Const.of("cd"));
            Expr a2 = bc.newArray(String.class, Const.of("ef"), Const.of("gh"));
            return bc.newArray(String[].class, a1, a2);
        }, "[[ab, cd], [ef, gh]]");
        testCreateArray(bc -> {
            Expr cs1 = bc.localVar("cs1", bc.new_(StringBuilder.class, Const.of("ij")));
            Expr cs2 = bc.localVar("cs2", bc.new_(StringBuilder.class, Const.of("mn")));
            Expr a1 = bc.newArray(CharSequence.class, cs1, Const.of("kl"), cs2);
            Expr a2 = bc.newArray(CharSequence.class);
            return bc.newArray(CharSequence[].class, a1, a2);
        }, "[[ij, kl, mn], []]");
        testCreateArray(bc -> {
            Expr num1 = bc.localVar("num1", bc.new_(Integer.class, Const.of(123)));
            Expr num2 = bc.localVar("num2", bc.new_(Integer.class, Const.of(456)));
            Expr a1 = bc.newArray(String.class, Const.of("op"), Const.of("qr"));
            Expr a2 = bc.newArray(CharSequence.class);
            Expr a3 = bc.newArray(Integer.class, num1, num2);
            return bc.newArray(Object[].class, a1, a2, a3);
        }, "[[op, qr], [], [123, 456]]");
    }

    private void testCreateArray(Function<BlockCreator, Expr> bytecode, String expectedResult) {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestCreateArray", cc -> {
            cc.staticMethod("returnArrayString", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr array = bytecode.apply(bc);
                    if (array.type().componentType().isArray()) {
                        Expr toString = bc
                                .invokeStatic(MethodDesc.of(Arrays.class, "deepToString", String.class, Object[].class), array);
                        bc.return_(toString);
                    } else {
                        ClassDesc arrayType = array.type().componentType().isPrimitive()
                                ? array.type()
                                : ConstantDescs.CD_Object.arrayType();
                        MethodTypeDesc toStringType = MethodTypeDesc.of(ConstantDescs.CD_String, arrayType);
                        Expr toString = bc.invokeStatic(MethodDesc.of(Arrays.class, "toString", toStringType), array);
                        bc.return_(toString);
                    }
                });
            });
        });
        assertEquals(expectedResult, tcm.staticMethod("returnArrayString", Supplier.class).get());
    }

    @Test
    public void readPrimitiveArray() {
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(boolean.class, Const.of(true), Const.of(false)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 1);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(byte.class, Const.of((byte) 1), Const.of((byte) 2)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 3);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(short.class, Const.of((short) 3), Const.of((short) 4)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 7);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(char.class, Const.of('a'), Const.of('b')));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 195); // a = 97, b = 98
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(int.class, Const.of(7), Const.of(8)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 15);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(long.class, Const.of(9L), Const.of(10L)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 19);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(float.class, Const.of(11.0F), Const.of(12.0F)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 23);
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(double.class, Const.of(13.0), Const.of(14.0)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 27);
    }

    @Test
    public void readReferenceArray() {
        testReadArray(bc -> {
            Expr arr = bc.localVar("arr", bc.newArray(String.class, Const.of("ab"), Const.of("cde")));
            Expr l1 = bc.withString(arr.elem(0)).length();
            Expr l2 = bc.withString(arr.elem(1)).length();
            return bc.add(l1, l2);
        }, 5);
    }

    private void testReadArray(Function<BlockCreator, Expr> bytecode, int expectedResult) {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestReadArray", cc -> {
            cc.staticMethod("returnInt", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bytecode.apply(bc));
                });
            });
        });
        assertEquals(expectedResult, tcm.staticMethod("returnInt", IntSupplier.class).getAsInt());
    }

    @Test
    public void testComputedIndex() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ComputedIndex", cc -> {
            MethodDesc one = cc.staticMethod("one", mc -> {
                // static int one() {
                //     return 1;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(1);
                });
            });
            MethodDesc two = cc.staticMethod("two", mc -> {
                // static int two() {
                //     return 2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(2);
                });
            });
            cc.staticMethod("runTest", mc -> {
                // static Object runTest() {
                //    String[] arr = new String[] { "foo", "bar", "baz", "quux" };
                //    return arr[one() + two()];
                // }
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    LocalVar arr = bc.localVar("arr", bc.newArray(String.class, Const.of("foo"),
                            Const.of("bar"), Const.of("baz"), Const.of("quux")));
                    bc.return_(arr.elem(bc.add(bc.invokeStatic(one), bc.invokeStatic(two))));
                });
            });
        });
        assertEquals("quux", tcm.staticMethod("runTest", Supplier.class).get());
    }

    @Test
    public void testMultipleComputedIndices() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.MultipleComputedIndices", cc -> {
            MethodDesc two = cc.staticMethod("two", mc -> {
                // static int two() {
                //     return 2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(2);
                });
            });
            MethodDesc three = cc.staticMethod("three", mc -> {
                // static int three() {
                //     return 3;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(3);
                });
            });
            cc.staticMethod("runTest", mc -> {
                // static int runTest() {
                //    int[] arr = new int[] { 1, 2, 3, 4, 5 };
                //    return arr[two()] + arr[three()];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar arr = bc.localVar("arr", bc.newArray(int.class, Const.of(1),
                            Const.of(2), Const.of(3), Const.of(4), Const.of(5)));
                    bc.return_(bc.add(arr.elem(bc.invokeStatic(two)), arr.elem(bc.invokeStatic(three))));
                });
            });
        });
        assertEquals(7, tcm.staticMethod("runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testGetArrayElement() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GetArrayElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    Expr array = bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3));
                    bc.return_(array.elem(1));
                });
            });
        });
        assertEquals(2, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testExplicitGetArrayElement() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ExplicitGetArrayElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    Expr array = bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3));
                    bc.return_(bc.get(array.elem(1)));
                });
            });
        });
        assertEquals(2, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testVolatileGetArrayElement() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileGetArrayElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    Expr array = bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3));
                    bc.return_(bc.get(array.elem(1), MemoryOrder.Volatile));
                });
            });
        });
        assertEquals(2, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testSetArrayElement() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.SetArrayElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array",
                            bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3)));

                    bc.set(array.elem(1), param);

                    bc.return_(array.elem(1));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }

    @Test
    public void testVolatileSetArrayElement() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileSetArrayElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array",
                            bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3)));

                    bc.set(array.elem(1), param, MemoryOrder.Volatile);

                    bc.return_(array.elem(1));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }

    @Test
    public void testCreateArrayByMapping() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CreateArrayByMapping", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `String[]`
                mc.body(bc -> {
                    bc.return_(bc.newArray(String.class, List.of("foo", "bar"), it -> {
                        Const value = Const.of(it);
                        return bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class), value);
                    }));
                });
            });
        });
        assertArrayEquals(new String[] { "FOO", "BAR" }, (String[]) tcm.staticMethod("test", Supplier.class).get());
    }
}
