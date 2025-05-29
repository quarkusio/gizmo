package io.quarkus.gizmo2;

import static io.quarkus.gizmo2.creator.GenericType.booleanType;
import static io.quarkus.gizmo2.creator.GenericType.byteType;
import static io.quarkus.gizmo2.creator.GenericType.charType;
import static io.quarkus.gizmo2.creator.GenericType.classType;
import static io.quarkus.gizmo2.creator.GenericType.doubleType;
import static io.quarkus.gizmo2.creator.GenericType.floatType;
import static io.quarkus.gizmo2.creator.GenericType.intType;
import static io.quarkus.gizmo2.creator.GenericType.longType;
import static io.quarkus.gizmo2.creator.GenericType.parameterizedType;
import static io.quarkus.gizmo2.creator.GenericType.shortType;
import static io.quarkus.gizmo2.creator.GenericType.typeVariable;
import static io.quarkus.gizmo2.creator.GenericType.voidType;
import static io.quarkus.gizmo2.creator.GenericType.wildcardTypeUnbounded;
import static io.quarkus.gizmo2.creator.GenericType.wildcardTypeWithLowerBound;
import static io.quarkus.gizmo2.creator.GenericType.wildcardTypeWithUpperBound;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_String;
import static java.lang.constant.ConstantDescs.CD_boolean;
import static java.lang.constant.ConstantDescs.CD_byte;
import static java.lang.constant.ConstantDescs.CD_char;
import static java.lang.constant.ConstantDescs.CD_double;
import static java.lang.constant.ConstantDescs.CD_float;
import static java.lang.constant.ConstantDescs.CD_int;
import static java.lang.constant.ConstantDescs.CD_long;
import static java.lang.constant.ConstantDescs.CD_short;
import static java.lang.constant.ConstantDescs.CD_void;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.GenericType;

public class GenericTypeTest {
    private static final ClassDesc outer = ClassDesc.of("com.example.Outer");

    @Test
    public void primitiveTypes() {
        assertEquals("void", voidType().toString());
        assertEquals("boolean", booleanType().toString());
        assertEquals("byte", byteType().toString());
        assertEquals("short", shortType().toString());
        assertEquals("int", intType().toString());
        assertEquals("long", longType().toString());
        assertEquals("float", floatType().toString());
        assertEquals("double", doubleType().toString());
        assertEquals("char", charType().toString());

        assertEquals(CD_void, voidType().erasure());
        assertEquals(CD_boolean, booleanType().erasure());
        assertEquals(CD_byte, byteType().erasure());
        assertEquals(CD_short, shortType().erasure());
        assertEquals(CD_int, intType().erasure());
        assertEquals(CD_long, longType().erasure());
        assertEquals(CD_float, floatType().erasure());
        assertEquals(CD_double, doubleType().erasure());
        assertEquals(CD_char, charType().erasure());
    }

    @Test
    public void classTypes() {
        assertEquals("java.lang.Object", GenericType.ClassType.OBJECT.toString());
        assertEquals("java.lang.String", classType(String.class).toString());
        assertEquals("com.example.Outer.Inner", classType(outer)
                .innerClass("Inner").toString());
        assertEquals("com.example.Outer.Inner<T>", classType(outer)
                .innerParameterizedType("Inner", typeVariable("T")).toString());

        assertEquals(CD_Object, GenericType.ClassType.OBJECT.erasure());
        assertEquals(CD_String, classType(String.class).erasure());
        assertEquals(outer.nested("Inner"), classType(outer)
                .innerClass("Inner").erasure());
        assertEquals(outer.nested("Inner"), classType(outer)
                .innerParameterizedType("Inner", typeVariable("T")).erasure());
    }

    @Test
    public void parameterizedTypes() {
        assertEquals("java.util.List<java.lang.String>", parameterizedType(List.class,
                classType(String.class)).toString());
        assertEquals("java.util.List<? extends java.lang.String>", parameterizedType(List.class,
                wildcardTypeWithUpperBound(classType(String.class))).toString());
        assertEquals("java.util.List<? super java.lang.String>", parameterizedType(List.class,
                wildcardTypeWithLowerBound(classType(String.class))).toString());
        assertEquals("java.util.List<?>", parameterizedType(List.class, wildcardTypeUnbounded()).toString());
        assertEquals("java.util.List<T>", parameterizedType(List.class, typeVariable("T")).toString());
        assertEquals("com.example.Outer<T>.Inner", parameterizedType(classType(outer), typeVariable("T"))
                .innerClass("Inner").toString());
        assertEquals("com.example.Outer<T>.Inner<U>", parameterizedType(classType(outer), typeVariable("T"))
                .innerParameterizedType("Inner", typeVariable("U")).toString());

        assertEquals(CD_List, parameterizedType(List.class, classType(String.class)).erasure());
        assertEquals(CD_List, parameterizedType(List.class, wildcardTypeWithUpperBound(classType(String.class))).erasure());
        assertEquals(CD_List, parameterizedType(List.class, wildcardTypeWithLowerBound(classType(String.class))).erasure());
        assertEquals(CD_List, parameterizedType(List.class, wildcardTypeUnbounded()).erasure());
        assertEquals(CD_List, parameterizedType(List.class, typeVariable("T")).erasure());
        assertEquals(outer.nested("Inner"), parameterizedType(classType(outer), typeVariable("T"))
                .innerClass("Inner").erasure());
        assertEquals(outer.nested("Inner"), parameterizedType(classType(outer), typeVariable("T"))
                .innerParameterizedType("Inner", typeVariable("U")).erasure());
    }
}
