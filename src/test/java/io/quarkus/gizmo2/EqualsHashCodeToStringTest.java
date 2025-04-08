package io.quarkus.gizmo2;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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
            fields.add(cc.field("booleanValue", fc -> fc.withType(boolean.class)));
            fields.add(cc.field("byteValue", fc -> fc.withType(byte.class)));
            fields.add(cc.field("shortValue", fc -> fc.withType(short.class)));
            fields.add(cc.field("intValue", fc -> fc.withType(int.class)));
            fields.add(cc.field("longValue", fc -> fc.withType(long.class)));
            fields.add(cc.field("floatValue", fc -> fc.withType(float.class)));
            fields.add(cc.field("doubleValue", fc -> fc.withType(double.class)));
            fields.add(cc.field("charValue", fc -> fc.withType(char.class)));
            fields.add(cc.field("stringValue", fc -> fc.withType(String.class)));

            fields.add(cc.field("booleanArrayValue", fc -> fc.withType(boolean[].class)));
            fields.add(cc.field("byteArrayValue", fc -> fc.withType(byte[].class)));
            fields.add(cc.field("shortArrayValue", fc -> fc.withType(short[].class)));
            fields.add(cc.field("intArrayValue", fc -> fc.withType(int[].class)));
            fields.add(cc.field("longArrayValue", fc -> fc.withType(long[].class)));
            fields.add(cc.field("floatArrayValue", fc -> fc.withType(float[].class)));
            fields.add(cc.field("doubleArrayValue", fc -> fc.withType(double[].class)));
            fields.add(cc.field("charArrayValue", fc -> fc.withType(char[].class)));
            fields.add(cc.field("stringArrayValue", fc -> fc.withType(String[].class)));

            fields.add(cc.field("boolean2DArrayValue", fc -> fc.withType(boolean[][].class)));
            fields.add(cc.field("byte2DArrayValue", fc -> fc.withType(byte[][].class)));
            fields.add(cc.field("short2DArrayValue", fc -> fc.withType(short[][].class)));
            fields.add(cc.field("int2DArrayValue", fc -> fc.withType(int[][].class)));
            fields.add(cc.field("long2DArrayValue", fc -> fc.withType(long[][].class)));
            fields.add(cc.field("float2DArrayValue", fc -> fc.withType(float[][].class)));
            fields.add(cc.field("double2DArrayValue", fc -> fc.withType(double[][].class)));
            fields.add(cc.field("char2DArrayValue", fc -> fc.withType(char[][].class)));
            fields.add(cc.field("string2DArrayValue", fc -> fc.withType(String[][].class)));

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
}
