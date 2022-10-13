package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.junit.Test;

import io.quarkus.gizmo.Gizmo.CustomInvocationGenerator;
import io.quarkus.gizmo.Gizmo.JdkList.JdkListInstance;
import io.quarkus.gizmo.Gizmo.JdkOptional;

public class GizmoUtilsTest {

    @SuppressWarnings("deprecation")
    @Test
    public void testList() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            ResultHandle sb = method.newInstance(MethodDescriptor.ofConstructor(StringBuilder.class));
            MethodDescriptor sbAppend = MethodDescriptor.ofMethod(StringBuilder.class, "append", StringBuilder.class,
                    String.class);
            Gizmo.CustomInvocationGenerator append = new CustomInvocationGenerator(method, (bc, args) -> {
                return bc.invokeVirtualMethod(sbAppend, sb, Gizmo.toString(bc, args[0]));
            });

            // List<String> list = List.of("foo","bar");
            ResultHandle list = Gizmo.listOperations(method).of(method.load("foo"), method.load("bar"));

            JdkListInstance listInstance = Gizmo.listOperations(method).on(list);
            // sb.append(list.get(1));
            append.invoke(listInstance.get(1));
            append.invoke(method.load(":"));
            // sb.append(list.size());
            append.invoke(listInstance.size());
            append.invoke(method.load(":"));
            // sb.append(list.contains("foo"));
            append.invoke(listInstance.contains(method.load("foo")));
            append.invoke(method.load(":"));

            // ArrayList empty = new ArrayList();
            ResultHandle emptyArrayList = Gizmo.newArrayList(method);
            // sb.append(empty.size);
            append.invoke(Gizmo.collectionOperations(method).on(emptyArrayList).size());
            append.invoke(method.load(":"));

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

            method.returnValue(Gizmo.toString(method, sb));
        }
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("bar:2:true:0:", myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testOptional() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
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
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals(true, myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testIterable() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
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
        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals(false, myInterface.get());
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testMap() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        TestClassLoader cl = new TestClassLoader(getClass().getClassLoader());
        try (ClassCreator creator = ClassCreator.builder().classOutput(cl).className("com.MyTest").interfaces(Supplier.class)
                .build()) {
            MethodCreator method = creator.getMethodCreator("get", Object.class);
            // HashMap map = new HashMap();
            ResultHandle map = Gizmo.newHashMap(method);
            // map.put("alpha", "A");
            Gizmo.mapOperations(method).instance(map).put(method.load("alpha"), method.load("A"));
            // map.put("bravo", "B");
            Gizmo.mapOperations(method).instance(map).put(method.load("bravo"), method.load("B"));
            // if (map.size() < 2) return false;
            method.ifIntegerLessThan(Gizmo.mapOperations(method).instance(map).size(), method.load(2)).trueBranch()
                    .returnValue(method.load(false));
            // if (map.isEmpty()) return false;
            method.ifTrue(Gizmo.mapOperations(method).instance(map).isEmpty()).trueBranch()
                    .returnValue(method.load(false));
            // return map.get("alpha").equals(map.get("alpha"));
            ResultHandle areEqual = Gizmo.equals(method, Gizmo.mapOperations(method).instance(map).get(method.load("alpha")),
                    Gizmo.mapOperations(method).instance(map).get(method.load("alpha")));
            method.returnValue(areEqual);
        }

        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals(true, myInterface.get());
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
            Gizmo.StringBuilderGenerator str = Gizmo.newStringBuilder(createCharSequence);
            str.append("ghi");
            createCharSequence.returnValue(str.getInstance());

            MethodCreator method = creator.getMethodCreator("get", Object.class);

            Gizmo.StringBuilderGenerator strBuilder = Gizmo.newStringBuilder(method);

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

        Supplier<?> myInterface = (Supplier<?>) cl.loadClass("com.MyTest").newInstance();
        assertEquals("true12345.06.0abcdefghijklmnull...!", myInterface.get());
    }

    public static class MyObject {
        @Override
        public String toString() {
            return "jklm";
        }
    }
}
