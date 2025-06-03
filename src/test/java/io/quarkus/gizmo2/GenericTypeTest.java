package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.util.AbstractList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.dmlloyd.classfile.Annotation;
import io.github.dmlloyd.classfile.Attributes;
import io.github.dmlloyd.classfile.ClassModel;
import io.github.dmlloyd.classfile.CodeModel;
import io.github.dmlloyd.classfile.FieldModel;
import io.github.dmlloyd.classfile.MethodModel;
import io.github.dmlloyd.classfile.TypeAnnotation;
import io.github.dmlloyd.classfile.attribute.LocalVariableTypeInfo;
import io.github.dmlloyd.classfile.attribute.LocalVariableTypeTableAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeInvisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.RuntimeVisibleTypeAnnotationsAttribute;
import io.github.dmlloyd.classfile.attribute.SignatureAttribute;
import io.quarkus.gizmo2.creator.BlockCreator;

public final class GenericTypeTest {

    @Test
    public void testGenericLocalVar() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericLocalVar", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    GenericType listOfString = GenericType.of(List.class, List.of(TypeArgument.of(String.class)));
                    LocalVar list = b0.localVar("list", listOfString, Const.ofNull(List.class));
                    b0.return_();
                });
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        CodeModel test0code = test0.code().orElseThrow();
        LocalVariableTypeTableAttribute lvtt = test0code.findAttribute(Attributes.localVariableTypeTable()).orElseThrow();
        assertEquals(1, lvtt.localVariableTypes().size());
        LocalVariableTypeInfo info = lvtt.localVariableTypes().get(0);
        assertEquals("list", info.name().stringValue());
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", info.signature().stringValue());
    }

    @Test
    public void testTypeAnnotationLocalVar() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestTypeAnnotationLocalVar", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.body(b0 -> {
                    LocalVar foo = b0.localVar("foo", GenericType.ofClass(String.class).withAnnotation(Visible.class),
                            Const.of("hello"));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    LocalVar foo = b0.localVar("foo", GenericType.ofClass(String.class).withAnnotation(Invisible.class),
                            Const.of("hello"));
                    b0.return_();
                });
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        CodeModel test0code = test0.code().orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute test0ann = test0code.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test0ann.annotations().size());
        assertEquals(Visible.class.descriptorString(), test0ann.annotations().get(0).annotation().className().stringValue());
        MethodModel test1 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test1")).findFirst()
                .orElseThrow();
        CodeModel test1code = test1.code().orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute test1ann = test1code
                .findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).orElseThrow();
        assertEquals(1, test1ann.annotations().size());
        assertEquals(Invisible.class.descriptorString(), test1ann.annotations().get(0).annotation().className().stringValue());
    }

    @Test
    public void testGenericParameter() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericParameter", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.parameter("list", pc -> {
                    pc.setType(GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
                });
                mc.body(BlockCreator::return_);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        SignatureAttribute sa = test0.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("(Ljava/util/List<Ljava/lang/String;>;)V", sa.signature().stringValue());
        CodeModel test0code = test0.code().orElseThrow();
        LocalVariableTypeTableAttribute lvtt = test0code.findAttribute(Attributes.localVariableTypeTable()).orElseThrow();
        assertEquals(1, lvtt.localVariableTypes().size());
        LocalVariableTypeInfo info = lvtt.localVariableTypes().get(0);
        assertEquals("list", info.name().stringValue());
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", info.signature().stringValue());
    }

    @Test
    public void testTypeAnnotationParameter() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestTypeAnnotationParameter", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.parameter("foo", pc -> {
                    pc.setType(GenericType.ofClass(String.class).withAnnotation(Visible.class));
                });
                mc.body(BlockCreator::return_);
            });
            zc.staticMethod("test1", mc -> {
                mc.parameter("foo", pc -> {
                    pc.setType(GenericType.ofClass(String.class).withAnnotation(Invisible.class));
                });
                mc.body(BlockCreator::return_);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute test0topAnn = test0.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test0topAnn.annotations().size());
        assertEquals(Visible.class.descriptorString(), test0topAnn.annotations().get(0).annotation().className().stringValue());
        CodeModel test0code = test0.code().orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute test0ann = test0code.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test0ann.annotations().size());
        assertEquals(Visible.class.descriptorString(), test0ann.annotations().get(0).annotation().className().stringValue());
        MethodModel test1 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test1")).findFirst()
                .orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute test1topAnn = test1.findAttribute(Attributes.runtimeInvisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test1topAnn.annotations().size());
        assertEquals(Invisible.class.descriptorString(),
                test1topAnn.annotations().get(0).annotation().className().stringValue());
        CodeModel test1code = test1.code().orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute test1ann = test1code
                .findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).orElseThrow();
        assertEquals(1, test1ann.annotations().size());
        assertEquals(Invisible.class.descriptorString(), test1ann.annotations().get(0).annotation().className().stringValue());
    }

    @Test
    public void testGenericReturn() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericReturn", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.returning(GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
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
    public void testTypeAnnotationReturn() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestTypeAnnotationReturn", zc -> {
            zc.staticMethod("test0", mc -> {
                mc.returning(GenericType.ofClass(String.class).withAnnotation(Visible.class));
                mc.body(BlockCreator::returnNull);
            });
            zc.staticMethod("test1", mc -> {
                mc.returning(GenericType.ofClass(String.class).withAnnotation(Invisible.class));
                mc.body(BlockCreator::returnNull);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        MethodModel test0 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test0")).findFirst()
                .orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute test0topAnn = test0.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test0topAnn.annotations().size());
        assertEquals(Visible.class.descriptorString(), test0topAnn.annotations().get(0).annotation().className().stringValue());
        MethodModel test1 = model.methods().stream().filter(mm -> mm.methodName().equalsString("test1")).findFirst()
                .orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute test1topAnn = test1.findAttribute(Attributes.runtimeInvisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test1topAnn.annotations().size());
        assertEquals(Invisible.class.descriptorString(),
                test1topAnn.annotations().get(0).annotation().className().stringValue());
    }

    @Test
    public void testGenericField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericField", zc -> {
            zc.field("test0", fc -> {
                fc.setType(GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        FieldModel test0 = model.fields().stream().filter(fm -> fm.fieldName().equalsString("test0")).findFirst().orElseThrow();
        SignatureAttribute sa = test0.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("Ljava/util/List<Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testTypeAnnotationField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestTypeAnnotationField", zc -> {
            zc.field("test0", fc -> {
                fc.setType(GenericType.ofClass(String.class).withAnnotation(Visible.class));
            });
            zc.field("test1", fc -> {
                fc.setType(GenericType.ofClass(String.class).withAnnotation(Invisible.class));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        FieldModel test0 = model.fields().stream().filter(fm -> fm.fieldName().equalsString("test0")).findFirst().orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute test0ann = test0.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        assertEquals(1, test0ann.annotations().size());
        assertEquals(Visible.class.descriptorString(), test0ann.annotations().get(0).annotation().className().stringValue());
        FieldModel test1 = model.fields().stream().filter(fm -> fm.fieldName().equalsString("test1")).findFirst().orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute test1ann = test1
                .findAttribute(Attributes.runtimeInvisibleTypeAnnotations()).orElseThrow();
        assertEquals(1, test1ann.annotations().size());
        assertEquals(Invisible.class.descriptorString(), test1ann.annotations().get(0).annotation().className().stringValue());
    }

    @Test
    public void testComplexCase() {
        GenericType bigGenericType = GenericType.of(Generic.class, List.of(
                TypeArgument.ofExtends(GenericType.ofClass(GenericStatic.class, List.of(
                        TypeArgument.ofSuper(GenericType.ofClass(String.class).withAnnotation(Invisible.class))
                                .withAnnotation(Visible.class),
                        TypeArgument.ofExact(GenericType.ofClass(Integer.class).withAnnotation(Visible.class))))),
                TypeArgument.ofUnbounded().withAnnotations(ac -> {
                    ac.addAnnotation(Visible.class);
                    ac.addAnnotation(Invisible.class);
                }))).withAnnotation(Visible.class);
        assertEquals(
                "io.quarkus.gizmo2.GenericTypeTest.@io.quarkus.gizmo2.GenericTypeTest$Visible Generic<? extends io.quarkus.gizmo2.GenericTypeTest$GenericStatic<@io.quarkus.gizmo2.GenericTypeTest$Visible ? super @io.quarkus.gizmo2.GenericTypeTest$Invisible java.lang.String, @io.quarkus.gizmo2.GenericTypeTest$Visible java.lang.Integer>, @io.quarkus.gizmo2.GenericTypeTest$Visible @io.quarkus.gizmo2.GenericTypeTest$Invisible ?>",
                bigGenericType.toString());
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestComplexCase", zc -> {
            zc.field("test0", fc -> {
                fc.setType(bigGenericType);
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        FieldModel test0 = model.fields().stream().filter(fm -> fm.fieldName().equalsString("test0")).findFirst().orElseThrow();
        RuntimeVisibleTypeAnnotationsAttribute visibleAttr = test0.findAttribute(Attributes.runtimeVisibleTypeAnnotations())
                .orElseThrow();
        RuntimeInvisibleTypeAnnotationsAttribute invisibleAttr = test0
                .findAttribute(Attributes.runtimeInvisibleTypeAnnotations())
                .orElseThrow();
        List<TypeAnnotation> visibleType = visibleAttr.annotations();
        List<TypeAnnotation> invisibleType = invisibleAttr.annotations();
        assertEquals(4, visibleType.size());
        assertEquals(2, invisibleType.size());
        TypeAnnotation typeAnn = visibleType.get(0);
        Annotation ann = typeAnn.annotation();
        assertEquals(Visible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(TypeAnnotation.TypePathComponent.INNER_TYPE), typeAnn.targetPath());
        typeAnn = visibleType.get(1);
        ann = typeAnn.annotation();
        assertEquals(Visible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 0),
                TypeAnnotation.TypePathComponent.WILDCARD,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 0)),
                typeAnn.targetPath());
        typeAnn = visibleType.get(2);
        ann = typeAnn.annotation();
        assertEquals(Visible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 0),
                TypeAnnotation.TypePathComponent.WILDCARD,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 1)),
                typeAnn.targetPath());
        typeAnn = visibleType.get(3);
        ann = typeAnn.annotation();
        assertEquals(Visible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 1)),
                typeAnn.targetPath());
        typeAnn = invisibleType.get(0);
        ann = typeAnn.annotation();
        assertEquals(Invisible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 0),
                TypeAnnotation.TypePathComponent.WILDCARD,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 0),
                TypeAnnotation.TypePathComponent.WILDCARD), typeAnn.targetPath());
        typeAnn = invisibleType.get(1);
        ann = typeAnn.annotation();
        assertEquals(Invisible.class.descriptorString(), ann.className().stringValue());
        assertEquals(TypeAnnotation.TargetInfo.ofField(), typeAnn.targetInfo());
        assertEquals(List.of(
                TypeAnnotation.TypePathComponent.INNER_TYPE,
                TypeAnnotation.TypePathComponent.of(TypeAnnotation.TypePathComponent.Kind.TYPE_ARGUMENT, 1)),
                typeAnn.targetPath());
    }

    @SuppressWarnings({ "InnerClassMayBeStatic", "unused" })
    public class Generic<T, S> {
    }

    @SuppressWarnings("unused")
    public static class GenericStatic<T, S> {
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Visible {
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.CLASS)
    public @interface Invisible {
    }

    @Test
    public void testClassTypeParameter() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestClassTypeParameter", zc -> {
            zc.typeParameter("T", tvc -> {
                tvc.setFirstBound(GenericType.ofClass(String.class));
                tvc.setOtherBounds(List.of(GenericType.ofClass(Serializable.class)));
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
            zc.extends_((GenericType.OfClass) GenericType.of(AbstractList.class,
                    List.of(TypeArgument.ofSuper(GenericType.ofClass(String.class)))));
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
            zc.implements_((GenericType.OfClass) GenericType.of(List.class,
                    List.of(TypeArgument.ofExtends(GenericType.ofClass(String.class)))));
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("Ljava/lang/Object;Ljava/util/List<+Ljava/lang/String;>;", sa.signature().stringValue());
    }

    @Test
    public void testGenericReceiver() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericReceiver", zc -> {
            zc.typeParameter("T", tvc -> {
                tvc.setOtherBounds(List.of(GenericType.ofClass(CharSequence.class)));
            });
            zc.method("test0", mc -> {
                mc.returning(zc.genericType());
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
    public void testRecursiveType() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestRecursiveType", zc -> {
            zc.typeParameter("S", tvc -> {
                tvc.setFirstBound(GenericType.ofTypeVariable("T", CD_List));
            });
            zc.typeParameter("T", tvc -> {
                tvc.setFirstBound(GenericType.ofTypeVariable("S", CD_List));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<S:TT;T:TS;>Ljava/lang/Object;", sa.signature().stringValue());
    }
}
