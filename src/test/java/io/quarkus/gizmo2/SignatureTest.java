package io.quarkus.gizmo2;

import static io.quarkus.gizmo2.creator.GenericType.classType;
import static io.quarkus.gizmo2.creator.GenericType.parameterizedType;
import static io.quarkus.gizmo2.creator.GenericType.typeVariable;
import static io.quarkus.gizmo2.creator.GenericType.wildcardTypeWithLowerBound;
import static io.quarkus.gizmo2.creator.GenericType.wildcardTypeWithUpperBound;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import java.lang.constant.ClassDesc;
import java.util.AbstractList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.dmlloyd.classfile.Attributes;
import io.github.dmlloyd.classfile.ClassModel;
import io.github.dmlloyd.classfile.FieldModel;
import io.github.dmlloyd.classfile.MethodModel;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.GenericType;

public final class SignatureTest {
    @Test
    public void testGenericParameter() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericParameter", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.parameter("list", pc -> {
                    pc.setType(List.class);
                });
                mc.signature(sc -> {
                    sc.addParameterType(parameterizedType(List.class, classType(String.class)));
                });
                mc.body(BlockCreator::return_);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        SignatureAttribute sa = test0.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("(Ljava/util/List<Ljava/lang/String;>;)V", sa.signature().stringValue());
    }

    @Test
    public void testGenericReturnType() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericReturn", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.returning(List.class);
                mc.signature(sc -> {
                    sc.setReturnType(parameterizedType(List.class, classType(String.class)));
                });
                mc.body(BlockCreator::returnNull);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        SignatureAttribute sa = test0.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("()Ljava/util/List<Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testGenericField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericField", zc -> {
            zc.field("test0", fc -> {
                fc.setType(List.class);
                fc.signature(sc -> {
                    sc.type(parameterizedType(List.class, classType(String.class)));
                });
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        FieldModel test0 = model.fields().stream().filter(fm -> fm.fieldName().equalsString("test0")).findFirst().orElseThrow();
        SignatureAttribute sa = test0.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testClassTypeParameter() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestClassTypeParameter", zc -> {
            zc.signature(sc -> {
                sc.addTypeParameter(typeVariable("T", classType(String.class), classType(Serializable.class)));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<T:Ljava/lang/String;:Ljava/io/Serializable;>Ljava/lang/Object;", sa.signature().stringValue());
    }

    @Test
    public void testClassExtendsGeneric() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestClassExtendsGeneric", zc -> {
            zc.extends_(AbstractList.class);
            zc.signature(sc -> {
                sc.extends_(parameterizedType(AbstractList.class, wildcardTypeWithLowerBound(String.class)));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("Ljava/util/AbstractList<-Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testClassImplementsGeneric() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestClassImplementsGeneric", zc -> {
            zc.implements_(List.class);
            zc.signature(sc -> {
                sc.implements_(parameterizedType(List.class, wildcardTypeWithUpperBound(String.class)));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("Ljava/lang/Object;Ljava/util/List<+Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testGenericReceiver() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        GenericType.TypeVariable typeParam = typeVariable("T", null, classType(CharSequence.class));
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericReceiver", zc -> {
            zc.signature(sc -> {
                sc.addTypeParameter(typeParam);
            });
            zc.method("test0", mc -> {
                mc.returning(zc.type());
                mc.signature(sc -> {
                    sc.setReturnType(parameterizedType(classType(zc.type()), typeParam));
                });
                mc.body(b0 -> b0.return_(zc.this_()));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<T::Ljava/lang/CharSequence;>Ljava/lang/Object;", sa.signature().stringValue());
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        assertEquals("()Lio/quarkus/gizmo2/TestGenericReceiver<TT;>;",
                test0.findAttribute(Attributes.signature()).orElseThrow().signature().stringValue());
    }

    @Test
    public void testRecursiveTypeParam() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestRecursiveTypeParam", zc -> {
            zc.signature(sc -> {
                sc.addTypeParameter(typeVariable("T", parameterizedType(classType(zc.type()), typeVariable("T"))));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<T:Lio/quarkus/gizmo2/TestRecursiveTypeParam<TT;>;>Ljava/lang/Object;", sa.signature().stringValue());
    }

    @Test
    public void testMutuallyRecursiveTypeParams() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestMutuallyRecursiveTypeParams", zc -> {
            zc.signature(sc -> {
                sc.addTypeParameter(typeVariable("S", typeVariable("T")));
                sc.addTypeParameter(typeVariable("T", typeVariable("S")));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<S:TT;T:TS;>Ljava/lang/Object;", sa.signature().stringValue());
    }
}
