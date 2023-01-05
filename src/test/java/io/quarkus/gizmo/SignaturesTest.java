package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.DotName;
import org.junit.Test;

import io.quarkus.gizmo.SignaturesTest.Nested.Inner.Inner2;
import io.quarkus.gizmo.SignaturesTest.NestedParam.InnerParam;

public class SignaturesTest {

    @Test
    public void testMethodSignatures() {
        // void test()
        assertEquals("()V",
                SignatureBuilder.forMethod().build());

        // void test(long l)
        assertEquals("(J)V",
                SignatureBuilder.forMethod().addParameter(Type.longType()).build());

        // List<String> test(List<?> list)
        assertEquals("(Ljava/util/List<*>;)Ljava/util/List<Ljava/lang/String;>;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.parameterizedType(Type.classType(List.class), Type.classType(String.class)))
                        .addParameter(Type.parameterizedType(Type.classType(List.class), Type.wildcardTypeUnbounded()))
                        .build());

        // Object test()
        assertEquals("()Ljava/lang/Object;",
                SignatureBuilder.forMethod().setReturnType(Type.classType(DotName.OBJECT_NAME)).build());

        // <T extends Comparable<T>>  String[] test(T t)
        assertEquals("<T::Ljava/lang/Comparable<TT;>;>(TT;)[Ljava/lang/String;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.arrayType(Type.classType(String.class)))
                        .addParameter(Type.typeVariable("T"))
                        .addTypeParameter(Type.typeVariable("T", null,
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T"))))
                        .build());

        // <R> List<R> test(int a, T t)
        assertEquals("(ITT;)Ljava/util/List<TR;>;",
                SignatureBuilder.forMethod()
                        .setReturnType(
                                Type.parameterizedType(Type.classType(DotName.createSimple(List.class)),
                                        Type.typeVariable("R")))
                        .addParameter(Type.intType())
                        .addParameter(Type.typeVariable("T")).build());

        // boolean test(int i)
        assertEquals("(I)Z",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.booleanType())
                        .addParameter(Type.intType()).build());

        // <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T bbb(U arg, W arg2, OuterParam<W> self)
        assertEquals(
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(TU;TW;Ltest/OuterParam<TW;>;)TT;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.typeVariable("T"))
                        .addParameter(Type.typeVariable("U"))
                        .addParameter(Type.typeVariable("W"))
                        .addParameter(Type.parameterizedType(Type.classType("test/OuterParam"),
                                Type.typeVariable("W")))
                        .addTypeParameter(Type.typeVariable("T", Type.classType(Number.class),
                                Type.parameterizedType(
                                        Type.classType(Comparable.class),
                                        Type.typeVariable("T"))))
                        .addTypeParameter(Type.typeVariable("U", null,
                                Type.parameterizedType(Type.classType(Comparable.class),
                                        Type.typeVariable("U"))))
                        .addTypeParameter(Type.typeVariable("V", Type.classType(Exception.class))).build());

        // <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T test(List<? extends U> arg, W arg2, Foo arg3) throws IllegalArgumentException, V
        assertEquals(
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;TW;Lio/quarkus/gizmo/SignaturesTest$NestedParam<TP;>.Inner;)TT;^Ljava/lang/IllegalArgumentException;^TV;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.typeVariable("T"))
                        .addTypeParameter(Type.typeVariable("T", Type.classType(Number.class),
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T"))))
                        .addTypeParameter(Type.typeVariable("U", null,
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("U"))))
                        .addTypeParameter(Type.typeVariable("V", Type.classType(Exception.class)))
                        .addParameter(Type.parameterizedType(Type.classType(List.class),
                                Type.wildcardTypeWithUpperBound(Type.typeVariable("U"))))
                        .addParameter(Type.typeVariable("W"))
                        .addParameter(Type.parameterizedType(Type.classType(NestedParam.class), Type.typeVariable("P"))
                                .nestedClassType(
                                        NestedParam.Inner.class.getSimpleName()))
                        .addException(Type.classType(IllegalArgumentException.class))
                        .addException(Type.typeVariable("V"))
                        .build());

        // Nested.Inner.Inner2 test()
        assertEquals("()Lio/quarkus/gizmo/SignaturesTest$Nested$Inner$Inner2;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.classType(Inner2.class))
                        .build());
    }

    @Test
    public void testFieldSignatures() {
        // List<String> foo;
        assertEquals("Ljava/util/List<Ljava/lang/String;>;",
                SignatureBuilder.forField()
                        .setType(Type.parameterizedType(Type.classType(List.class), Type.classType(String.class)))
                        .build());

        // T foo;
        assertEquals("TT;",
                SignatureBuilder.forField()
                        .setType(Type.typeVariable("T"))
                        .build());

        // List<T extends Number> foo;
        assertEquals("Ljava/util/List<TT;>;",
                SignatureBuilder.forField()
                        .setType(Type.parameterizedType(Type.classType(List.class),
                                Type.typeVariable("T", Type.classType(Number.class))))
                        .build());

        // double foo;
        assertEquals("D",
                SignatureBuilder.forField()
                        .setType(Type.doubleType())
                        .build());

        // List<?> foo;
        assertEquals("Ljava/util/List<*>;",
                SignatureBuilder.forField()
                        .setType(Type.parameterizedType(Type.classType(List.class), Type.wildcardTypeUnbounded()))
                        .build());

        // Map<? extends Number,? super Number> foo;
        assertEquals("Ljava/util/Map<+Ljava/lang/Number;-Ljava/lang/Number;>;",
                SignatureBuilder.forField()
                        .setType(Type.parameterizedType(Type.classType(Map.class),
                                Type.wildcardTypeWithUpperBound(Type.classType(Number.class)),
                                Type.wildcardTypeWithLowerBound(Type.classType(Number.class))))
                        .build());

        // Signature not needed
        // Nested foo;
        assertEquals("Lio/quarkus/gizmo/SignaturesTest$Nested;",
                SignatureBuilder.forField()
                        .setType(Type.classType(Nested.class))
                        .build());

        // Signature not needed
        // NestedParam.Inner.Inner2 foo;
        assertEquals("Lio/quarkus/gizmo/SignaturesTest$NestedParam$InnerParam$Inner2;",
                SignatureBuilder.forField()
                        .setType(Type.classType(NestedParam.InnerParam.Inner2.class))
                        .build());

        // NestedParam.InnerParam<T> foo;
        assertEquals("Lio/quarkus/gizmo/SignaturesTest$NestedParam<TP;>.InnerParam<TP;>;",
                SignatureBuilder.forField()
                        .setType(Type.parameterizedType(Type.classType(NestedParam.class), Type.typeVariable("P"))
                                .nestedParameterizedType(InnerParam.class.getSimpleName(), Type.typeVariable("P")))
                        .build());
    }

    @Test
    public void testClassSignatures() {
        // class Foo<T>
        assertEquals("<T:Ljava/lang/Object;>Ljava/lang/Object;",
                SignatureBuilder.forClass()
                        .addTypeParameter(Type.typeVariable("T"))
                        .build());

        // class Foo<T> extends List<T>
        assertEquals("<T:Ljava/lang/Object;>Ljava/util/List<TT;>;",
                SignatureBuilder.forClass()
                        .addTypeParameter(Type.typeVariable("T"))
                        .setSuperClass(Type.parameterizedType(Type.classType(List.class), Type.typeVariable("T")))
                        .build());

        // class Foo<T> extends List<T> implements Serializable, Comparable<T>
        assertEquals("<T:Ljava/lang/Object;>Ljava/util/List<TT;>;Ljava/io/Serializable;Ljava/lang/Comparable<TT;>;",
                SignatureBuilder.forClass()
                        .addTypeParameter(Type.typeVariable("T"))
                        .setSuperClass(Type.parameterizedType(Type.classType(List.class), Type.typeVariable("T")))
                        .addSuperInterface(Type.classType(Serializable.class))
                        .addSuperInterface(Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T")))
                        .build());

        try {
            SignatureBuilder.forClass()
                    .setSuperClass(Type.parameterizedType(Type.classType(List.class), Type.wildcardTypeUnbounded()));
            fail();
        } catch (Exception expected) {
        }
    }

    public static class Nested {

        public class Inner {

            class Inner2 {

            }
        }

    }

    public static class NestedParam<P> {

        InnerParam<P> inner;

        public class Inner {
        }

        public class InnerParam<I> {

            class Inner2 {

            }
        }

    }

}
