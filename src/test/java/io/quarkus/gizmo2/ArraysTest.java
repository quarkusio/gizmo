package io.quarkus.gizmo2;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.MethodDesc;
import org.junit.jupiter.api.Test;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.lang.constant.MethodTypeDesc;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ArraysTest {
    @Test
    public void createPrimitiveArray() {
        testCreateArray(bc -> bc.newArray(boolean.class, Constant.of(true), Constant.of(false)), "[true, false]");
        testCreateArray(bc -> bc.newArray(byte.class, Constant.of((byte) 1), Constant.of((byte) 2)), "[1, 2]");
        testCreateArray(bc -> bc.newArray(short.class, Constant.of((short) 3), Constant.of((short) 4)), "[3, 4]");
        testCreateArray(bc -> bc.newArray(char.class, Constant.of('a'), Constant.of('b')), "[a, b]");
        testCreateArray(bc -> bc.newArray(int.class, Constant.of(7), Constant.of(8)), "[7, 8]");
        testCreateArray(bc -> bc.newArray(long.class, Constant.of(9L), Constant.of(10L)), "[9, 10]");
        testCreateArray(bc -> bc.newArray(float.class, Constant.of(11.0F), Constant.of(12.0F)), "[11.0, 12.0]");
        testCreateArray(bc -> bc.newArray(double.class, Constant.of(13.0), Constant.of(14.0)), "[13.0, 14.0]");
    }

    @Test
    public void createMultiDimensionalPrimitiveArray() {
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(boolean.class, Constant.of(true), Constant.of(false));
            Expr a2 = bc.newArray(boolean.class, Constant.of(false), Constant.of(true));
            return bc.newArray(boolean[].class, a1, a2);
        }, "[[true, false], [false, true]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(byte.class, Constant.of((byte) 1), Constant.of((byte) 2));
            Expr a2 = bc.newArray(byte.class, Constant.of((byte) 3), Constant.of((byte) 4));
            Expr a3 = bc.newArray(byte.class, Constant.of((byte) 5), Constant.of((byte) 6));
            return bc.newArray(byte[].class, a1, a2, a3);
        }, "[[1, 2], [3, 4], [5, 6]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(short.class, Constant.of((short) 7));
            Expr a2 = bc.newArray(short.class, Constant.of((short) 8), Constant.of((short) 9));
            return bc.newArray(short[].class, a1, a2);
        }, "[[7], [8, 9]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(char.class, Constant.of('a'));
            Expr a2 = bc.newArray(char.class, Constant.of('b'), Constant.of('c'));
            Expr a3 = bc.newArray(char.class);
            Expr a4 = bc.newArray(char.class);
            Expr a5 = bc.newArray(char.class, Constant.of('d'));
            return bc.newArray(char[].class, a1, a2, a3, a4, a5);
        }, "[[a], [b, c], [], [], [d]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(int.class, Constant.of(10));
            Expr a2 = bc.newArray(int.class, Constant.of(11), Constant.of(12));
            Expr a3 = bc.newArray(int.class, Constant.of(13), Constant.of(14), Constant.of(15));
            return bc.newArray(int[].class, a1, a2, a3);
        }, "[[10], [11, 12], [13, 14, 15]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(long.class, Constant.of(16L));
            Expr a2 = bc.newArray(long.class);
            Expr a3 = bc.newArray(long.class, Constant.of(17L));
            return bc.newArray(long[].class, a1, a2, a3);
        }, "[[16], [], [17]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(float.class, Constant.of(18.0F));
            Expr a2 = bc.newArray(float.class);
            Expr a3 = bc.newArray(float[].class, a1, a2);
            Expr a4 = bc.newArray(float.class, Constant.of(19.0F));
            Expr a5 = bc.newArray(float.class, Constant.of(20.0F), Constant.of(21.0F));
            Expr a6 = bc.newArray(float[].class, a4, a5);
            return bc.newArray(float[][].class, a3, a6);
        }, "[[[18.0], []], [[19.0], [20.0, 21.0]]]");
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(double.class, Constant.of(22.0));
            Expr a2 = bc.newArray(double.class, Constant.of(23.0), Constant.of(24.0));
            Expr a3 = bc.newArray(double[].class, a1, a2);
            Expr a4 = bc.newArray(double.class, Constant.of(25.0), Constant.of(26.0));
            Expr a5 = bc.newArray(double.class, Constant.of(27.0));
            Expr a6 = bc.newArray(double[].class, a4, a5);
            Expr a7 = bc.newArray(double.class, Constant.of(27.0), Constant.of(28.0));
            Expr a8 = bc.newArray(double.class);
            Expr a9 = bc.newArray(double[].class, a7, a8);
            return bc.newArray(double[][].class, a3, a6, a9);
        }, "[[[22.0], [23.0, 24.0]], [[25.0, 26.0], [27.0]], [[27.0, 28.0], []]]");
    }

    @Test
    public void createReferenceArray() {
        testCreateArray(bc -> bc.newArray(String.class, Constant.of("ab"), Constant.of("cd")),
                "[ab, cd]");
        testCreateArray(bc -> {
            Expr ef = bc.new_(StringBuilder.class, Constant.of("ef"));
            return bc.newArray(CharSequence.class, ef, Constant.of("gh"));
        }, "[ef, gh]");
        testCreateArray(bc -> {
            Expr num = bc.new_(Integer.class, Constant.of(123));
            return bc.newArray(Object.class, Constant.of("ij"), num, Constant.of("klm"));
        }, "[ij, 123, klm]");
    }

    @Test
    public void createMultiDimensionalReferenceArrays() {
        testCreateArray(bc -> {
            Expr a1 = bc.newArray(String.class, Constant.of("ab"), Constant.of("cd"));
            Expr a2 = bc.newArray(String.class, Constant.of("ef"), Constant.of("gh"));
            return bc.newArray(String[].class, a1, a2);
        }, "[[ab, cd], [ef, gh]]");
        testCreateArray(bc -> {
            Expr cs1 = bc.define("cs1", bc.new_(StringBuilder.class, Constant.of("ij")));
            Expr cs2 = bc.define("cs2", bc.new_(StringBuilder.class, Constant.of("mn")));
            Expr a1 = bc.newArray(CharSequence.class, cs1, Constant.of("kl"), cs2);
            Expr a2 = bc.newArray(CharSequence.class);
            return bc.newArray(CharSequence[].class, a1, a2);
        }, "[[ij, kl, mn], []]");
        testCreateArray(bc -> {
            Expr num1 = bc.define("num1", bc.new_(Integer.class, Constant.of(123)));
            Expr num2 = bc.define("num2", bc.new_(Integer.class, Constant.of(456)));
            Expr a1 = bc.newArray(String.class, Constant.of("op"), Constant.of("qr"));
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
                        Expr toString = bc.invokeStatic(MethodDesc.of(Arrays.class, "deepToString", String.class, Object[].class), array);
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
            Expr arr = bc.define("arr", bc.newArray(boolean.class, Constant.of(true), Constant.of(false)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 1);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(byte.class, Constant.of((byte) 1), Constant.of((byte) 2)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 3);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(short.class, Constant.of((short) 3), Constant.of((short) 4)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 7);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(char.class, Constant.of('a'), Constant.of('b')));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 195); // a = 97, b = 98
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(int.class, Constant.of(7), Constant.of(8)));
            return bc.add(arr.elem(0), arr.elem(1));
        }, 15);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(long.class, Constant.of(9L), Constant.of(10L)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 19);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(float.class, Constant.of(11.0F), Constant.of(12.0F)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 23);
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(double.class, Constant.of(13.0), Constant.of(14.0)));
            return bc.add(bc.cast(arr.elem(0), int.class), bc.cast(arr.elem(1), int.class));
        }, 27);
    }

    @Test
    public void readReferenceArray() {
        testReadArray(bc -> {
            Expr arr = bc.define("arr", bc.newArray(String.class, Constant.of("ab"), Constant.of("cde")));
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
}
