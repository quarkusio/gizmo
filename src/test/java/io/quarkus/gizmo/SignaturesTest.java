package io.quarkus.gizmo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.DotName;
import org.junit.Test;

import io.quarkus.gizmo.SignatureBuilder.ClassSignatureBuilder;
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
                SignatureBuilder.forMethod().addParameterType(Type.longType()).build());

        // List<String> test(List<?> list)
        assertEquals("(Ljava/util/List<*>;)Ljava/util/List<Ljava/lang/String;>;",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.parameterizedType(Type.classType(List.class), Type.classType(String.class)))
                        .addParameterType(Type.parameterizedType(Type.classType(List.class), Type.wildcardTypeUnbounded()))
                        .build());

        // Object test()
        assertEquals("()Ljava/lang/Object;",
                SignatureBuilder.forMethod().setReturnType(Type.classType(DotName.OBJECT_NAME)).build());

        // <T extends Comparable<T>> String[] test(T t)
        assertEquals("<T::Ljava/lang/Comparable<TT;>;>(TT;)[Ljava/lang/String;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("T", null,
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T"))))
                        .setReturnType(Type.arrayType(Type.classType(String.class)))
                        .addParameterType(Type.typeVariable("T"))
                        .build());

        // <R> List<R> test(int a, T t)
        assertEquals("<R:Ljava/lang/Object;>(ITT;)Ljava/util/List<TR;>;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("R"))
                        .setReturnType(
                                Type.parameterizedType(Type.classType(DotName.createSimple(List.class)),
                                        Type.typeVariable("R")))
                        .addParameterType(Type.intType())
                        .addParameterType(Type.typeVariable("T"))
                        .build());

        // <R, S extends R> List<S> test(int a, T t)
        assertEquals("<R:Ljava/lang/Object;S:TR;>(ITT;)Ljava/util/List<TS;>;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("R"))
                        .addTypeParameter(Type.typeVariable("S", Type.typeVariable("R")))
                        .setReturnType(
                                Type.parameterizedType(Type.classType(DotName.createSimple(List.class)),
                                        Type.typeVariable("S")))
                        .addParameterType(Type.intType())
                        .addParameterType(Type.typeVariable("T"))
                        .build());

        // <R extends Serializable & Comparable<R>> List<R> test(int a, T t)
        assertEquals("<R::Ljava/io/Serializable;:Ljava/lang/Comparable<TR;>;>(ITT;)Ljava/util/List<TR;>;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("R", null,
                                Type.classType(Serializable.class),
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("R"))))
                        .setReturnType(
                                Type.parameterizedType(Type.classType(List.class), Type.typeVariable("R")))
                        .addParameterType(Type.intType())
                        .addParameterType(Type.typeVariable("T"))
                        .build());

        // boolean test(int i)
        assertEquals("(I)Z",
                SignatureBuilder.forMethod()
                        .setReturnType(Type.booleanType())
                        .addParameterType(Type.intType()).build());

        // <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T bbb(U arg, W arg2, OuterParam<W> self)
        assertEquals(
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(TU;TW;Ltest/OuterParam<TW;>;)TT;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("T", Type.classType(Number.class),
                                Type.parameterizedType(
                                        Type.classType(Comparable.class),
                                        Type.typeVariable("T"))))
                        .addTypeParameter(Type.typeVariable("U", null,
                                Type.parameterizedType(Type.classType(Comparable.class),
                                        Type.typeVariable("U"))))
                        .addTypeParameter(Type.typeVariable("V", Type.classType(Exception.class)))
                        .setReturnType(Type.typeVariable("T"))
                        .addParameterType(Type.typeVariable("U"))
                        .addParameterType(Type.typeVariable("W"))
                        .addParameterType(Type.parameterizedType(Type.classType("test/OuterParam"),
                                Type.typeVariable("W")))
                        .build());

        // <T extends Number & Comparable<T>, U extends Comparable<U>, V extends Exception> T test(List<? extends U> arg, W arg2, NestedParam<P>.Inner arg3) throws IllegalArgumentException, V
        assertEquals(
                "<T:Ljava/lang/Number;:Ljava/lang/Comparable<TT;>;U::Ljava/lang/Comparable<TU;>;V:Ljava/lang/Exception;>(Ljava/util/List<+TU;>;TW;Lio/quarkus/gizmo/SignaturesTest$NestedParam<TP;>.Inner;)TT;^Ljava/lang/IllegalArgumentException;^TV;",
                SignatureBuilder.forMethod()
                        .addTypeParameter(Type.typeVariable("T", Type.classType(Number.class),
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T"))))
                        .addTypeParameter(Type.typeVariable("U", null,
                                Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("U"))))
                        .addTypeParameter(Type.typeVariable("V", Type.classType(Exception.class)))
                        .setReturnType(Type.typeVariable("T"))
                        .addParameterType(Type.parameterizedType(Type.classType(List.class),
                                Type.wildcardTypeWithUpperBound(Type.typeVariable("U"))))
                        .addParameterType(Type.typeVariable("W"))
                        .addParameterType(Type.parameterizedType(Type.classType(NestedParam.class), Type.typeVariable("P"))
                                .innerClass(NestedParam.Inner.class.getSimpleName()))
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
                                .innerParameterizedType(InnerParam.class.getSimpleName(), Type.typeVariable("P")))
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
                        .addInterface(Type.classType(Serializable.class))
                        .addInterface(Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T")))
                        .build());

        // public class OuterParam<T extends Serializable> {
        //     public interface NestedParam<U> {
        //     }
        //
        //     public class InnerParam<U extends Number> {
        //         public class InnerInnerRaw {
        //             public class InnerInnerInnerParam<V> {
        //                 public class Test<X extends String, Y extends Integer>
        //                         extends OuterParam<X>.InnerParam<Y>.InnerInnerRaw.InnerInnerInnerParam<String>
        //                         implements NestedParam<V> {
        //                 }
        //             }
        //         }
        //     }
        // }
        assertEquals(
                "<X:Ljava/lang/String;Y:Ljava/lang/Integer;>Lio/quarkus/gizmo/test/OuterParam<TX;>.InnerParam<TY;>.InnerInnerRaw.InnerInnerInnerParam<Ljava/lang/String;>;Lio/quarkus/gizmo/test/OuterParam$NestedParam<TV;>;",
                SignatureBuilder.forClass()
                        .addTypeParameter(Type.typeVariable("X", Type.classType(String.class)))
                        .addTypeParameter(Type.typeVariable("Y", Type.classType(Integer.class)))
                        .setSuperClass(
                                Type.parameterizedType(Type.classType("io.quarkus.gizmo.test.OuterParam"),
                                        Type.typeVariable("X"))
                                        .innerParameterizedType("InnerParam", Type.typeVariable("Y"))
                                        .innerClass("InnerInnerRaw")
                                        .innerParameterizedType("InnerInnerInnerParam", Type.classType(String.class)))
                        .addInterface(Type.parameterizedType(Type.classType("io.quarkus.gizmo.test.OuterParam$NestedParam"),
                                Type.typeVariable("V")))
                        .build());

        try {
            SignatureBuilder.forClass()
                    .setSuperClass(Type.parameterizedType(Type.classType(List.class), Type.wildcardTypeUnbounded()));
            fail();
        } catch (Exception expected) {
        }
    }

    @Test
    public void testClassCreatorSignatureBuilder() {
        // class Foo<T> extends List<T> implements Serializable, Comparable<T>
        ClassSignatureBuilder classSignature = SignatureBuilder.forClass()
                .addTypeParameter(Type.typeVariable("T"))
                .setSuperClass(Type.parameterizedType(Type.classType(List.class), Type.typeVariable("T")))
                .addInterface(Type.classType(Serializable.class))
                .addInterface(Type.parameterizedType(Type.classType(Comparable.class), Type.typeVariable("T")));

        ClassCreator creator = ClassCreator.builder().signature(classSignature).className("org.acme.Foo").build();
        assertEquals("java/util/List", creator.getSuperClass());
        assertEquals(2, creator.getInterfaces().length);
        assertEquals("java/io/Serializable", creator.getInterfaces()[0]);
        assertEquals("java/lang/Comparable", creator.getInterfaces()[1]);
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
