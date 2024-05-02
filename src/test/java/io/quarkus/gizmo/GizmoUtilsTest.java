package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.junit.Test;

import io.quarkus.gizmo.Gizmo.JdkList.JdkListInstance;
import io.quarkus.gizmo.Gizmo.JdkOptional;
import io.quarkus.gizmo.Gizmo.JdkSet.JdkSetInstance;
import io.quarkus.gizmo.Gizmo.StringBuilderGenerator;

public class GizmoUtilsTest {

    @Test
    public void testList() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            StringBuilderGenerator sb = Gizmo.newStringBuilder(method);

            // List<String> list = List.of("foo","bar");
            ResultHandle list = Gizmo.listOperations(method).of(method.load("foo"), method.load("bar"));

            JdkListInstance listInstance = Gizmo.listOperations(method).on(list);
            // sb.append(list.get(1));
            sb.append(listInstance.get(1));
            sb.append(':');
            // sb.append(list.size());
            sb.append(listInstance.size());
            sb.append(':');
            // sb.append(list.contains("foo"));
            sb.append(listInstance.contains(method.load("foo")));
            sb.append(':');

            // ArrayList empty = new ArrayList();
            ResultHandle emptyArrayList = Gizmo.newArrayList(method);
            // sb.append(empty.size);
            sb.append(Gizmo.collectionOperations(method).on(emptyArrayList).size());
            sb.append(':');

            // sb.append(List.of().isEmpty())
            sb.append(Gizmo.listOperations(method).on(Gizmo.listOperations(method).of()).isEmpty());
            sb.append(':');

            // List<String> copy = List.copyOf(list);
            ResultHandle listCopy = Gizmo.listOperations(method).copyOf(list);
            // sb.append(copy.size());
            sb.append(Gizmo.collectionOperations(method).on(listCopy).size());
            sb.append(':');

            ResultHandle varArgsList = Gizmo.listOperations(method).of(method.load(1), method.load(2), method.load(3),
                    method.load(4));
            sb.append(Gizmo.collectionOperations(method).on(varArgsList).size());
            sb.append(':');

            try {
                Gizmo.listOperations(null);
                fail();
            } catch (NullPointerException expected) {
            }
            try {
                Gizmo.listOperations(method).on(null);
                fail();
            } catch (NullPointerException expected) {
            }

            method.returnValue(sb.callToString());
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals("bar:2:true:0:true:2:4:", myInterface.get());
    }

    public void testSet() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            StringBuilderGenerator sb = Gizmo.newStringBuilder(method);

            // Set<String> set = Set.of("foo","bar");
            ResultHandle set = Gizmo.setOperations(method).of(method.load("foo"), method.load("bar"));

            JdkSetInstance setInstance = Gizmo.setOperations(method).on(set);
            // sb.append(set.size());
            sb.append(setInstance.size());
            sb.append(':');
            // sb.append(set.contains("foo"));
            sb.append(setInstance.contains(method.load("foo")));
            sb.append(':');

            // sb.append(Set.of().isEmpty())
            sb.append(Gizmo.setOperations(method).on(Gizmo.setOperations(method).of()).isEmpty());
            sb.append(':');

            // Set<String> copy = Set.copyOf(set);
            ResultHandle setCopy = Gizmo.setOperations(method).copyOf(set);
            // sb.append(copy.size());
            sb.append(Gizmo.collectionOperations(method).on(setCopy).size());
            sb.append(':');

            ResultHandle varArgsSet = Gizmo.setOperations(method).of(method.load(1), method.load(2), method.load(3),
                    method.load(4));
            sb.append(Gizmo.collectionOperations(method).on(varArgsSet).size());
            sb.append(':');

            try {
                Gizmo.setOperations(null);
                fail();
            } catch (NullPointerException expected) {
            }
            try {
                Gizmo.setOperations(method).on(null);
                fail();
            } catch (NullPointerException expected) {
            }

            method.returnValue(sb.callToString());
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals("2:true:true:2:4:", myInterface.get());
    }

    @Test
    public void testOptional() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            JdkOptional jdkOptional = Gizmo.optionalOperations(method);
            // Optional<String> optionalFoo = Optional.of("foo");
            ResultHandle optionalFoo = jdkOptional.of(method.load("foo"));
            // if (optionalFoo.isEmpty) return false;
            method.ifTrue(jdkOptional.on(optionalFoo).isEmpty()).trueBranch().returnValue(method.load(false));
            // return optionalFoo.isPresent();
            method.returnValue(Gizmo.optionalOperations(method).on(optionalFoo).isPresent());
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals(true, myInterface.get());
    }

    @Test
    public void testIterable() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            // Set set = Set.of("element");
            ResultHandle set = Gizmo.setOperations(method).of(method.load("element"));
            // Iterator it = set.iterator();
            ResultHandle iterator = Gizmo.iterableOperations(method).on(set).iterator();
            // Object next = it.next();
            ResultHandle next = Gizmo.iteratorOperations(method).on(iterator).next();
            // if (next == null) return true;
            method.ifNull(next).trueBranch().returnValue(method.load(true));
            // return it.hasNext();
            method.returnValue(Gizmo.iteratorOperations(method).on(iterator).hasNext());
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals(false, myInterface.get());
    }

    @Test
    public void testMap() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            StringBuilderGenerator sb = Gizmo.newStringBuilder(method);

            // HashMap map = new HashMap();
            ResultHandle map = Gizmo.newHashMap(method);
            // map.put("alpha", "A");
            Gizmo.mapOperations(method).on(map).put(method.load("alpha"), method.load("A"));
            // map.put("bravo", "B");
            Gizmo.mapOperations(method).on(map).put(method.load("bravo"), method.load("B"));

            // sb.append(map.size());
            sb.append(Gizmo.mapOperations(method).on(map).size());
            sb.append(':');
            // sb.append(map.isEmpty())
            sb.append(Gizmo.mapOperations(method).on(map).isEmpty());
            sb.append(':');
            // sp.append(map.get("alpha").equals(map.get("alpha"))
            sb.append(Gizmo.equals(method, Gizmo.mapOperations(method).on(map).get(method.load("alpha")),
                    Gizmo.mapOperations(method).on(map).get(method.load("alpha"))));
            sb.append(':');
            // sb.append(Map.of().isEmpty())
            sb.append(Gizmo.mapOperations(method).on(Gizmo.mapOperations(method).of()).isEmpty());
            sb.append(':');

            // Map copy = Map.copyOf(map);
            ResultHandle mapCopy = Gizmo.mapOperations(method).copyOf(map);
            // sb.append(copy.containsKey("alpha"));
            sb.append(Gizmo.mapOperations(method).on(mapCopy).containsKey(method.load("alpha")));
            sb.append(':');

            try {
                Gizmo.mapOperations(null);
                fail();
            } catch (NullPointerException expected) {
            }
            try {
                Gizmo.mapOperations(method).on(null);
                fail();
            } catch (NullPointerException expected) {
            }

            method.returnValue(sb.callToString());
        }

        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals("2:false:true:true:true:", myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testSystemOutPrintln() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        System.setErr(new PrintStream(err));
        try {
            TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
            try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest")
                    .interfaces(Supplier.class)
                    .build()) {
                MethodCreator method = creator.getMethodCreator("get", Object.class);
                Gizmo.systemOutPrintln(method, method.load("TEST!"));
                Gizmo.systemOutPrintln(method, method.loadNull());
                Gizmo.systemErrPrintln(method, method.load("ERROR TEST!"));
                method.returnValue(method.load(true));
            }
            Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
            assertEquals(true, myInterface.get());
            assertEquals("TEST!\nnull\n", new String(out.toByteArray(), StandardCharsets.UTF_8));
            assertEquals("ERROR TEST!\n", new String(err.toByteArray(), StandardCharsets.UTF_8));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Test
    public void stringBuilder() throws ReflectiveOperationException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder()
                .classOutput(cl)
                .className("com.MyTest")
                .interfaces(Supplier.class)
                .build()) {

            MethodCreator createCharSequence = creator.getMethodCreator("createCharSequence", CharSequence.class);
            StringBuilderGenerator str = Gizmo.newStringBuilder(createCharSequence);
            str.append("ghi");
            createCharSequence.returnValue(str.getInstance());

            MethodCreator method = creator.getMethodCreator("get", Object.class);

            StringBuilderGenerator strBuilder = Gizmo.newStringBuilder(method);

            strBuilder.append(method.load(true));
            strBuilder.append(method.load((byte) 1));
            strBuilder.append(method.load((short) 2));
            strBuilder.append(method.load(3));
            strBuilder.append(method.load(4L));
            strBuilder.append(method.load(5.0F));
            strBuilder.append(method.load(6.0));
            strBuilder.append(method.load('a'));
            ResultHandle charArrayValue = method.newArray(char.class, 2);
            method.writeArrayValue(charArrayValue, 0, method.load('b'));
            method.writeArrayValue(charArrayValue, 1, method.load('c'));
            strBuilder.append(charArrayValue);
            strBuilder.append(method.load("def"));
            strBuilder.append(method.invokeVirtualMethod(MethodDescriptor.ofMethod(creator.getClassName(),
                    "createCharSequence", CharSequence.class), method.getThis()));
            strBuilder.append(method.newInstance(MethodDescriptor.ofConstructor(MyObject.class)));
            strBuilder.append(method.loadNull());
            strBuilder.append("...");
            strBuilder.append('!');

            method.returnValue(strBuilder.callToString());
        }

        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").getDeclaredConstructor().newInstance();
        assertEquals("true12345.06.0abcdefghijklmnull...!", myInterface.get());
    }

    public static class MyObject {
        @Override
        public String toString() {
            return "jklm";
        }
    }

    @Test
    public void equalityHashCodeToString() throws ReflectiveOperationException {
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
                new String[][] { { "lm" }, { "no" } },
        };

        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder()
                .classOutput(cl)
                .className("com.MyTest")
                .build()) {

            FieldDescriptor booleanDesc = creator.getFieldCreator("booleanValue", boolean.class).getFieldDescriptor();
            FieldDescriptor byteDesc = creator.getFieldCreator("byteValue", byte.class).getFieldDescriptor();
            FieldDescriptor shortDesc = creator.getFieldCreator("shortValue", short.class).getFieldDescriptor();
            FieldDescriptor intDesc = creator.getFieldCreator("intValue", int.class).getFieldDescriptor();
            FieldDescriptor longDesc = creator.getFieldCreator("longValue", long.class).getFieldDescriptor();
            FieldDescriptor floatDesc = creator.getFieldCreator("floatValue", float.class).getFieldDescriptor();
            FieldDescriptor doubleDesc = creator.getFieldCreator("doubleValue", double.class).getFieldDescriptor();
            FieldDescriptor charDesc = creator.getFieldCreator("charValue", char.class).getFieldDescriptor();
            FieldDescriptor stringDesc = creator.getFieldCreator("stringValue", String.class).getFieldDescriptor();

            FieldDescriptor booleanArrayDesc = creator.getFieldCreator("booleanArrayValue", boolean[].class)
                    .getFieldDescriptor();
            FieldDescriptor byteArrayDesc = creator.getFieldCreator("byteArrayValue", byte[].class).getFieldDescriptor();
            FieldDescriptor shortArrayDesc = creator.getFieldCreator("shortArrayValue", short[].class).getFieldDescriptor();
            FieldDescriptor intArrayDesc = creator.getFieldCreator("intArrayValue", int[].class).getFieldDescriptor();
            FieldDescriptor longArrayDesc = creator.getFieldCreator("longArrayValue", long[].class).getFieldDescriptor();
            FieldDescriptor floatArrayDesc = creator.getFieldCreator("floatArrayValue", float[].class).getFieldDescriptor();
            FieldDescriptor doubleArrayDesc = creator.getFieldCreator("doubleArrayValue", double[].class).getFieldDescriptor();
            FieldDescriptor charArrayDesc = creator.getFieldCreator("charArrayValue", char[].class).getFieldDescriptor();
            FieldDescriptor stringArrayDesc = creator.getFieldCreator("stringArrayValue", String[].class).getFieldDescriptor();

            FieldDescriptor boolean2DArrayDesc = creator.getFieldCreator("boolean2DArrayValue", boolean[][].class)
                    .getFieldDescriptor();
            FieldDescriptor byte2DArrayDesc = creator.getFieldCreator("byte2DArrayValue", byte[][].class).getFieldDescriptor();
            FieldDescriptor short2DArrayDesc = creator.getFieldCreator("short2DArrayValue", short[][].class)
                    .getFieldDescriptor();
            FieldDescriptor int2DArrayDesc = creator.getFieldCreator("int2DArrayValue", int[][].class).getFieldDescriptor();
            FieldDescriptor long2DArrayDesc = creator.getFieldCreator("long2DArrayValue", long[][].class).getFieldDescriptor();
            FieldDescriptor float2DArrayDesc = creator.getFieldCreator("float2DArrayValue", float[][].class)
                    .getFieldDescriptor();
            FieldDescriptor double2DArrayDesc = creator.getFieldCreator("double2DArrayValue", double[][].class)
                    .getFieldDescriptor();
            FieldDescriptor char2DArrayDesc = creator.getFieldCreator("char2DArrayValue", char[][].class).getFieldDescriptor();
            FieldDescriptor string2DArrayDesc = creator.getFieldCreator("string2DArrayValue", String[][].class)
                    .getFieldDescriptor();

            MethodCreator ctor = creator.getMethodCreator(MethodDescriptor.INIT, void.class, params);
            ctor.invokeSpecialMethod(MethodDescriptor.ofMethod(Object.class, MethodDescriptor.INIT, void.class),
                    ctor.getThis());

            ctor.writeInstanceField(booleanDesc, ctor.getThis(), ctor.getMethodParam(0));
            ctor.writeInstanceField(byteDesc, ctor.getThis(), ctor.getMethodParam(1));
            ctor.writeInstanceField(shortDesc, ctor.getThis(), ctor.getMethodParam(2));
            ctor.writeInstanceField(intDesc, ctor.getThis(), ctor.getMethodParam(3));
            ctor.writeInstanceField(longDesc, ctor.getThis(), ctor.getMethodParam(4));
            ctor.writeInstanceField(floatDesc, ctor.getThis(), ctor.getMethodParam(5));
            ctor.writeInstanceField(doubleDesc, ctor.getThis(), ctor.getMethodParam(6));
            ctor.writeInstanceField(charDesc, ctor.getThis(), ctor.getMethodParam(7));
            ctor.writeInstanceField(stringDesc, ctor.getThis(), ctor.getMethodParam(8));

            ctor.writeInstanceField(booleanArrayDesc, ctor.getThis(), ctor.getMethodParam(9));
            ctor.writeInstanceField(byteArrayDesc, ctor.getThis(), ctor.getMethodParam(10));
            ctor.writeInstanceField(shortArrayDesc, ctor.getThis(), ctor.getMethodParam(11));
            ctor.writeInstanceField(intArrayDesc, ctor.getThis(), ctor.getMethodParam(12));
            ctor.writeInstanceField(longArrayDesc, ctor.getThis(), ctor.getMethodParam(13));
            ctor.writeInstanceField(floatArrayDesc, ctor.getThis(), ctor.getMethodParam(14));
            ctor.writeInstanceField(doubleArrayDesc, ctor.getThis(), ctor.getMethodParam(15));
            ctor.writeInstanceField(charArrayDesc, ctor.getThis(), ctor.getMethodParam(16));
            ctor.writeInstanceField(stringArrayDesc, ctor.getThis(), ctor.getMethodParam(17));

            ctor.writeInstanceField(boolean2DArrayDesc, ctor.getThis(), ctor.getMethodParam(18));
            ctor.writeInstanceField(byte2DArrayDesc, ctor.getThis(), ctor.getMethodParam(19));
            ctor.writeInstanceField(short2DArrayDesc, ctor.getThis(), ctor.getMethodParam(20));
            ctor.writeInstanceField(int2DArrayDesc, ctor.getThis(), ctor.getMethodParam(21));
            ctor.writeInstanceField(long2DArrayDesc, ctor.getThis(), ctor.getMethodParam(22));
            ctor.writeInstanceField(float2DArrayDesc, ctor.getThis(), ctor.getMethodParam(23));
            ctor.writeInstanceField(double2DArrayDesc, ctor.getThis(), ctor.getMethodParam(24));
            ctor.writeInstanceField(char2DArrayDesc, ctor.getThis(), ctor.getMethodParam(25));
            ctor.writeInstanceField(string2DArrayDesc, ctor.getThis(), ctor.getMethodParam(26));

            ctor.returnVoid();

            Gizmo.generateEqualsAndHashCode(creator, creator.getExistingFields());
            Gizmo.generateNaiveToString(creator, creator.getExistingFields());
        }

        Class<?> clazz = cl.loadClass("com.MyTest");
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
                "MyTest(booleanValue=true, byteValue=1, shortValue=2, intValue=3, longValue=4, floatValue=5.0, doubleValue=6.0, charValue=a, stringValue=bc, booleanArrayValue=[true], byteArrayValue=[7], shortArrayValue=[8], intArrayValue=[9], longArrayValue=[10], floatArrayValue=[11.0], doubleArrayValue=[12.0], charArrayValue=[d, e], stringArrayValue=[fg], boolean2DArrayValue=[[true], [true]], byte2DArrayValue=[[13], [14]], short2DArrayValue=[[15], [16]], int2DArrayValue=[[17], [18]], long2DArrayValue=[[19], [20]], float2DArrayValue=[[21.0], [22.0]], double2DArrayValue=[[23.0], [24.0]], char2DArrayValue=[[h, i], [j, k]], string2DArrayValue=[[lm], [no]])",
                obj1.toString());
        assertEquals(obj1.toString(), obj2.toString());

        assertEquals(
                "MyTest(booleanValue=false, byteValue=1, shortValue=2, intValue=3, longValue=4, floatValue=5.0, doubleValue=6.0, charValue=a, stringValue=bc, booleanArrayValue=[true], byteArrayValue=[7], shortArrayValue=[8], intArrayValue=[9], longArrayValue=[10], floatArrayValue=[11.0], doubleArrayValue=[12.0], charArrayValue=[d, e], stringArrayValue=[fg], boolean2DArrayValue=[[true], [true]], byte2DArrayValue=[[13], [14]], short2DArrayValue=[[15], [16]], int2DArrayValue=[[17], [18]], long2DArrayValue=[[19], [20]], float2DArrayValue=[[21.0], [22.0]], double2DArrayValue=[[23.0], [24.0]], char2DArrayValue=[[h, i], [j, k]], string2DArrayValue=[[lm], [no]])",
                obj3.toString());
    }
}
