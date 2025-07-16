package io.quarkus.gizmo2.jandex;

import static io.quarkus.gizmo2.jandex.Jandex2Gizmo.classDescOf;
import static io.quarkus.gizmo2.jandex.Jandex2Gizmo.constructorDescOf;
import static io.quarkus.gizmo2.jandex.Jandex2Gizmo.fieldDescOf;
import static io.quarkus.gizmo2.jandex.Jandex2Gizmo.methodDescOf;
import static java.lang.constant.ConstantDescs.CD_Boolean;
import static java.lang.constant.ConstantDescs.CD_Byte;
import static java.lang.constant.ConstantDescs.CD_Character;
import static java.lang.constant.ConstantDescs.CD_Double;
import static java.lang.constant.ConstantDescs.CD_Float;
import static java.lang.constant.ConstantDescs.CD_Integer;
import static java.lang.constant.ConstantDescs.CD_List;
import static java.lang.constant.ConstantDescs.CD_Long;
import static java.lang.constant.ConstantDescs.CD_Map;
import static java.lang.constant.ConstantDescs.CD_Number;
import static java.lang.constant.ConstantDescs.CD_Object;
import static java.lang.constant.ConstantDescs.CD_Short;
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

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.PrimitiveType;
import org.jboss.jandex.TypeVariable;
import org.jboss.jandex.VoidType;
import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Reflection2Gizmo;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

public class Jandex2GizmoTest {
    @Test
    public void fromDotName() {
        assertEquals(CD_void, classDescOf(DotName.createSimple("void")));

        assertEquals(CD_boolean, classDescOf(DotName.createSimple("boolean")));
        assertEquals(CD_byte, classDescOf(DotName.createSimple("byte")));
        assertEquals(CD_short, classDescOf(DotName.createSimple("short")));
        assertEquals(CD_int, classDescOf(DotName.createSimple("int")));
        assertEquals(CD_long, classDescOf(DotName.createSimple("long")));
        assertEquals(CD_float, classDescOf(DotName.createSimple("float")));
        assertEquals(CD_double, classDescOf(DotName.createSimple("double")));
        assertEquals(CD_char, classDescOf(DotName.createSimple("char")));

        assertEquals(CD_Boolean, classDescOf(DotName.BOOLEAN_CLASS_NAME));
        assertEquals(CD_Byte, classDescOf(DotName.BYTE_CLASS_NAME));
        assertEquals(CD_Short, classDescOf(DotName.SHORT_CLASS_NAME));
        assertEquals(CD_Integer, classDescOf(DotName.INTEGER_CLASS_NAME));
        assertEquals(CD_Long, classDescOf(DotName.LONG_CLASS_NAME));
        assertEquals(CD_Float, classDescOf(DotName.FLOAT_CLASS_NAME));
        assertEquals(CD_Double, classDescOf(DotName.DOUBLE_CLASS_NAME));
        assertEquals(CD_Character, classDescOf(DotName.CHARACTER_CLASS_NAME));

        assertEquals(CD_String, classDescOf(DotName.STRING_NAME));
        assertEquals(CD_String, classDescOf(DotName.createSimple("java.lang.String")));
        assertEquals(CD_Object, classDescOf(DotName.OBJECT_NAME));
        assertEquals(CD_Object, classDescOf(DotName.createSimple("java.lang.Object")));

        // see `Type.name()` for how array types are represented
        assertEquals(CD_boolean.arrayType(), classDescOf(DotName.createSimple("[Z")));
        assertEquals(CD_byte.arrayType().arrayType(), classDescOf(DotName.createSimple("[[B")));
        assertEquals(CD_short.arrayType(3), classDescOf(DotName.createSimple("[[[S")));
        assertEquals(CD_int.arrayType(4), classDescOf(DotName.createSimple("[[[[I")));
        assertEquals(CD_long.arrayType(), classDescOf(DotName.createSimple("[J")));
        assertEquals(CD_float.arrayType().arrayType(), classDescOf(DotName.createSimple("[[F")));
        assertEquals(CD_double.arrayType(3), classDescOf(DotName.createSimple("[[[D")));
        assertEquals(CD_char.arrayType(4), classDescOf(DotName.createSimple("[[[[C")));

        assertEquals(CD_String.arrayType(), classDescOf(DotName.createSimple("[Ljava.lang.String;")));
        assertEquals(CD_Object.arrayType(2), classDescOf(DotName.createSimple("[[Ljava.lang.Object;")));
    }

    @Test
    public void fromType() {
        assertEquals(CD_void, classDescOf(VoidType.VOID));

        assertEquals(CD_boolean, classDescOf(PrimitiveType.BOOLEAN));
        assertEquals(CD_byte, classDescOf(PrimitiveType.BYTE));
        assertEquals(CD_short, classDescOf(PrimitiveType.SHORT));
        assertEquals(CD_int, classDescOf(PrimitiveType.INT));
        assertEquals(CD_long, classDescOf(PrimitiveType.LONG));
        assertEquals(CD_float, classDescOf(PrimitiveType.FLOAT));
        assertEquals(CD_double, classDescOf(PrimitiveType.DOUBLE));
        assertEquals(CD_char, classDescOf(PrimitiveType.CHAR));

        assertEquals(CD_Boolean, classDescOf(ClassType.BOOLEAN_CLASS));
        assertEquals(CD_Byte, classDescOf(ClassType.BYTE_CLASS));
        assertEquals(CD_Short, classDescOf(ClassType.SHORT_CLASS));
        assertEquals(CD_Integer, classDescOf(ClassType.INTEGER_CLASS));
        assertEquals(CD_Long, classDescOf(ClassType.LONG_CLASS));
        assertEquals(CD_Float, classDescOf(ClassType.FLOAT_CLASS));
        assertEquals(CD_Double, classDescOf(ClassType.DOUBLE_CLASS));
        assertEquals(CD_Character, classDescOf(ClassType.CHARACTER_CLASS));

        assertEquals(CD_String, classDescOf(ClassType.STRING_TYPE));
        assertEquals(CD_Object, classDescOf(ClassType.OBJECT_TYPE));

        assertEquals(CD_boolean.arrayType(), classDescOf(ArrayType.create(PrimitiveType.BOOLEAN, 1)));
        assertEquals(CD_byte.arrayType().arrayType(), classDescOf(ArrayType.create(PrimitiveType.BYTE, 2)));
        assertEquals(CD_short.arrayType(3), classDescOf(ArrayType.create(PrimitiveType.SHORT, 3)));
        assertEquals(CD_int.arrayType(4), classDescOf(ArrayType.create(PrimitiveType.INT, 4)));
        assertEquals(CD_long.arrayType(), classDescOf(ArrayType.create(PrimitiveType.LONG, 1)));
        assertEquals(CD_float.arrayType().arrayType(), classDescOf(ArrayType.create(PrimitiveType.FLOAT, 2)));
        assertEquals(CD_double.arrayType(3), classDescOf(ArrayType.create(PrimitiveType.DOUBLE, 3)));
        assertEquals(CD_char.arrayType(4), classDescOf(ArrayType.create(PrimitiveType.CHAR, 4)));

        assertEquals(CD_String.arrayType(), classDescOf(ArrayType.create(ClassType.STRING_TYPE, 1)));
        assertEquals(CD_Object.arrayType(2), classDescOf(ArrayType.create(ClassType.OBJECT_TYPE, 2)));

        assertEquals(CD_List, classDescOf(ParameterizedType.builder(List.class).addArgument(String.class).build()));
        assertEquals(CD_List.arrayType(), classDescOf(ArrayType.create(
                ParameterizedType.builder(List.class).addArgument(String.class).build(), 1)));

        assertEquals(CD_String, classDescOf(TypeVariable.builder("T").addBound(String.class).build()));
        assertEquals(CD_Object, classDescOf(TypeVariable.create("T")));
    }

    static class A<T> {
        class B {
            T field; // unused, just to silence warnings
        }
    }

    static class FooBar {
        int f1;
        A<String>.B f2;
        Integer[] f3;

        FooBar(int p1, A<String>.B p2, Integer[] p3) {
        }

        void m1(String p1) {
        }

        A<Integer> m2(A<String>.B p1, List<String> p2) {
            return null;
        }

        <T extends Number> T m3(List<?> p1, Map<? extends T, ? super String> p2) {
            return null;
        }
    }

    static final ClassDesc A_DESC = Reflection2Gizmo.classDescOf(A.class);

    static final ClassDesc A_B_DESC = A_DESC.nested("B");

    static final ClassDesc FOO_BAR_DESC = Reflection2Gizmo.classDescOf(FooBar.class);

    @Test
    public void fromClass() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        assertEquals(FOO_BAR_DESC, classDescOf(clazz));
    }

    @Test
    public void fromField() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        FieldInfo f1 = clazz.field("f1");
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f1", CD_int), fieldDescOf(f1));
        FieldInfo f2 = clazz.field("f2");
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f2", A_B_DESC), fieldDescOf(f2));
        FieldInfo f3 = clazz.field("f3");
        assertEquals(FieldDesc.of(FOO_BAR_DESC, "f3", CD_Integer.arrayType()), fieldDescOf(f3));
    }

    @Test
    public void fromMethod() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        MethodInfo m1 = clazz.firstMethod("m1");
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m1", MethodTypeDesc.of(CD_void, CD_String)), methodDescOf(m1));
        MethodInfo m2 = clazz.firstMethod("m2");
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m2", MethodTypeDesc.of(A_DESC, A_B_DESC, CD_List)), methodDescOf(m2));
        MethodInfo m3 = clazz.firstMethod("m3");
        assertEquals(ClassMethodDesc.of(FOO_BAR_DESC, "m3", MethodTypeDesc.of(CD_Number, CD_List, CD_Map)), methodDescOf(m3));
    }

    @Test
    public void fromConstructor() throws IOException {
        ClassInfo clazz = Index.singleClass(FooBar.class);
        MethodInfo c = clazz.constructors().get(0); // there's just one
        assertEquals(ConstructorDesc.of(FOO_BAR_DESC, CD_int, A_B_DESC, CD_Integer.arrayType()), constructorDescOf(c));
    }
}
