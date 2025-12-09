package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.constant.ClassDesc;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.smallrye.classfile.ClassModel;
import io.smallrye.classfile.constantpool.MemberRefEntry;
import io.smallrye.classfile.instruction.InvokeInstruction;

public class EqualsHashCodeToStringTest {
    @Test
    public void test() throws ReflectiveOperationException {
        Class<?>[] params = {
                boolean.class,
                byte.class,
                short.class,
                int.class,
                long.class,
                float.class,
                double.class,
                char.class,
                String.class,

                boolean[].class,
                byte[].class,
                short[].class,
                int[].class,
                long[].class,
                float[].class,
                double[].class,
                char[].class,
                String[].class,

                boolean[][].class,
                byte[][].class,
                short[][].class,
                int[][].class,
                long[][].class,
                float[][].class,
                double[][].class,
                char[][].class,
                String[][].class,
        };

        Object[] args = {
                true,
                (byte) 1,
                (short) 2,
                3,
                4L,
                5.0F,
                6.0,
                'a',
                "bc",

                new boolean[] { true },
                new byte[] { 7 },
                new short[] { 8 },
                new int[] { 9 },
                new long[] { 10L },
                new float[] { 11.0F },
                new double[] { 12.0 },
                new char[] { 'd', 'e' },
                new String[] { "fg" },

                new boolean[][] { { true }, { true } },
                new byte[][] { { 13 }, { 14 } },
                new short[][] { { 15 }, { 16 } },
                new int[][] { { 17 }, { 18 } },
                new long[][] { { 19 }, { 20 } },
                new float[][] { { 21.0F }, { 22.0F } },
                new double[][] { { 23.0 }, { 24.0 } },
                new char[][] { { 'h', 'i' }, { 'j', 'k' } },
                new String[][] { { "lm", "no" }, { "pq", "rs" } },
        };

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestClass", cc -> {
            List<FieldDesc> fields = new ArrayList<>();
            fields.add(cc.field("booleanValue", fc -> fc.setType(boolean.class)));
            fields.add(cc.field("byteValue", fc -> fc.setType(byte.class)));
            fields.add(cc.field("shortValue", fc -> fc.setType(short.class)));
            fields.add(cc.field("intValue", fc -> fc.setType(int.class)));
            fields.add(cc.field("longValue", fc -> fc.setType(long.class)));
            fields.add(cc.field("floatValue", fc -> fc.setType(float.class)));
            fields.add(cc.field("doubleValue", fc -> fc.setType(double.class)));
            fields.add(cc.field("charValue", fc -> fc.setType(char.class)));
            fields.add(cc.field("stringValue", fc -> fc.setType(String.class)));

            fields.add(cc.field("booleanArrayValue", fc -> fc.setType(boolean[].class)));
            fields.add(cc.field("byteArrayValue", fc -> fc.setType(byte[].class)));
            fields.add(cc.field("shortArrayValue", fc -> fc.setType(short[].class)));
            fields.add(cc.field("intArrayValue", fc -> fc.setType(int[].class)));
            fields.add(cc.field("longArrayValue", fc -> fc.setType(long[].class)));
            fields.add(cc.field("floatArrayValue", fc -> fc.setType(float[].class)));
            fields.add(cc.field("doubleArrayValue", fc -> fc.setType(double[].class)));
            fields.add(cc.field("charArrayValue", fc -> fc.setType(char[].class)));
            fields.add(cc.field("stringArrayValue", fc -> fc.setType(String[].class)));

            fields.add(cc.field("boolean2DArrayValue", fc -> fc.setType(boolean[][].class)));
            fields.add(cc.field("byte2DArrayValue", fc -> fc.setType(byte[][].class)));
            fields.add(cc.field("short2DArrayValue", fc -> fc.setType(short[][].class)));
            fields.add(cc.field("int2DArrayValue", fc -> fc.setType(int[][].class)));
            fields.add(cc.field("long2DArrayValue", fc -> fc.setType(long[][].class)));
            fields.add(cc.field("float2DArrayValue", fc -> fc.setType(float[][].class)));
            fields.add(cc.field("double2DArrayValue", fc -> fc.setType(double[][].class)));
            fields.add(cc.field("char2DArrayValue", fc -> fc.setType(char[][].class)));
            fields.add(cc.field("string2DArrayValue", fc -> fc.setType(String[][].class)));

            cc.constructor(mc -> {
                mc.public_();
                List<ParamVar> paramVars = new ArrayList<>();
                for (int i = 0; i < params.length; i++) {
                    paramVars.add(mc.parameter("param" + i, params[i]));
                }
                mc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());

                    for (int i = 0; i < params.length; i++) {
                        b0.set(cc.this_().field(fields.get(i)), paramVars.get(i));
                    }

                    b0.return_();
                });
            });

            cc.generateEqualsAndHashCode(cc.instanceFields());
            cc.generateToString(cc.instanceFields());
        });

        Class<?> clazz = tcm.definedClass();
        Constructor<?> ctor = clazz.getConstructor(params);

        Object obj1 = ctor.newInstance(args);
        Object obj2 = ctor.newInstance(args);

        args[0] = false;
        Object obj3 = ctor.newInstance(args);

        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());

        assertNotEquals(obj1, obj3);
        assertNotEquals(obj1.hashCode(), obj3.hashCode());

        assertEquals(
                "TestClass(booleanValue=true, byteValue=1, shortValue=2, intValue=3, longValue=4, floatValue=5.0, doubleValue=6.0, charValue=a, stringValue=bc, booleanArrayValue=[true], byteArrayValue=[7], shortArrayValue=[8], intArrayValue=[9], longArrayValue=[10], floatArrayValue=[11.0], doubleArrayValue=[12.0], charArrayValue=[d, e], stringArrayValue=[fg], boolean2DArrayValue=[[true], [true]], byte2DArrayValue=[[13], [14]], short2DArrayValue=[[15], [16]], int2DArrayValue=[[17], [18]], long2DArrayValue=[[19], [20]], float2DArrayValue=[[21.0], [22.0]], double2DArrayValue=[[23.0], [24.0]], char2DArrayValue=[[h, i], [j, k]], string2DArrayValue=[[lm, no], [pq, rs]])",
                obj1.toString());
        assertEquals(obj1.toString(), obj2.toString());

        assertEquals(
                "TestClass(booleanValue=false, byteValue=1, shortValue=2, intValue=3, longValue=4, floatValue=5.0, doubleValue=6.0, charValue=a, stringValue=bc, booleanArrayValue=[true], byteArrayValue=[7], shortArrayValue=[8], intArrayValue=[9], longArrayValue=[10], floatArrayValue=[11.0], doubleArrayValue=[12.0], charArrayValue=[d, e], stringArrayValue=[fg], boolean2DArrayValue=[[true], [true]], byte2DArrayValue=[[13], [14]], short2DArrayValue=[[15], [16]], int2DArrayValue=[[17], [18]], long2DArrayValue=[[19], [20]], float2DArrayValue=[[21.0], [22.0]], double2DArrayValue=[[23.0], [24.0]], char2DArrayValue=[[h, i], [j, k]], string2DArrayValue=[[lm, no], [pq, rs]])",
                obj3.toString());
    }

    @Test
    public void testConstants() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.Constants", cc -> {
            cc.staticMethod("equalsWithConstant", mc -> {
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.exprEquals(Const.of("foobar"), val));
                });
            });
            cc.staticMethod("hashCodeOfConstant", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.exprHashCode(Const.of("foobar")));
                });
            });
            cc.staticMethod("toStringOfConstant", mc -> {
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_(bc.exprToString(Const.of("foobar")));
                });
            });

            MethodDesc foobar = cc.staticMethod("foobar", mc -> {
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_(Const.of("foobar"));
                });
            });
            cc.staticMethod("equalsWithNonConstant", mc -> {
                ParamVar val = mc.parameter("val", String.class);
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.exprEquals(bc.invokeStatic(foobar), val));
                });
            });
            cc.staticMethod("hashCodeOfNonConstant", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.exprHashCode(bc.invokeStatic(foobar)));
                });
            });
            cc.staticMethod("toStringOfNonConstant", mc -> {
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_(bc.exprToString(bc.invokeStatic(foobar)));
                });
            });
        });

        Predicate<MemberRefEntry> objectsEquals = method -> method.owner().name().equalsString("java/util/Objects")
                && method.name().equalsString("equals");
        Predicate<MemberRefEntry> objectsHashCode = method -> method.owner().name().equalsString("java/util/Objects")
                && method.name().equalsString("hashCode");
        Predicate<MemberRefEntry> stringValueOf = method -> method.owner().name().equalsString("java/lang/String")
                && method.name().equalsString("valueOf");

        Predicate<MemberRefEntry> objectEquals = method -> method.owner().name().equalsString("java/lang/Object")
                && method.name().equalsString("equals");
        Predicate<MemberRefEntry> objectHashCode = method -> method.owner().name().equalsString("java/lang/Object")
                && method.name().equalsString("hashCode");
        Predicate<MemberRefEntry> objectToString = method -> method.owner().name().equalsString("java/lang/Object")
                && method.name().equalsString("toString");

        ClassModel model = tcm.forClass(desc).getModel();

        assertMethod(model, "equalsWithConstant", objectEquals, objectsEquals);
        assertMethod(model, "hashCodeOfConstant", objectHashCode, objectsHashCode);
        assertMethod(model, "toStringOfConstant", objectToString, stringValueOf);

        assertMethod(model, "equalsWithNonConstant", objectsEquals, objectEquals);
        assertMethod(model, "hashCodeOfNonConstant", objectsHashCode, objectHashCode);
        assertMethod(model, "toStringOfNonConstant", stringValueOf, objectToString);
    }

    private void assertMethod(ClassModel model, String methodName,
            Predicate<MemberRefEntry> mustInvoke,
            Predicate<MemberRefEntry> mayNotInvoke) {
        AtomicBoolean mustInvokeOccurs = new AtomicBoolean();
        AtomicBoolean mayNotInvokeOccurs = new AtomicBoolean();

        model.methods()
                .stream()
                .filter(m -> m.methodName().equalsString(methodName))
                .findFirst()
                .orElseThrow()
                .code()
                .orElseThrow()
                .elementStream()
                .filter(it -> it instanceof InvokeInstruction)
                .map(InvokeInstruction.class::cast)
                .forEach(insn -> {
                    if (mustInvoke.test(insn.method())) {
                        mustInvokeOccurs.set(true);
                    }
                    if (mayNotInvoke.test(insn.method())) {
                        mayNotInvokeOccurs.set(true);
                    }
                });

        assertTrue(mustInvokeOccurs.get());
        assertFalse(mayNotInvokeOccurs.get());
    }
}
