package io.quarkus.gizmo2;

import static io.quarkus.gizmo2.Reflection2Gizmo.genericTypeOf;
import static java.lang.constant.ConstantDescs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.Externalizable;
import java.io.Serializable;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.util.AbstractList;
import java.util.List;
import java.util.Map;

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
            GenericType.OfTypeVariable S = zc.typeParameter("S", tpc -> {
                // `T` doesn't exist yet, so have to specify it in full
                tpc.setFirstBound(
                        GenericType.ofClass(List.class, TypeArgument.ofExact(GenericType.ofTypeVariable("T", CD_List))));
            });
            zc.typeParameter("T", tvc -> {
                tvc.setFirstBound(GenericType.ofClass(List.class, TypeArgument.ofExact(S)));
            });
        });
        ClassModel model = tcm.forClass(desc).getModel();
        SignatureAttribute sa = model.findAttribute(Attributes.signature()).orElseThrow();
        assertEquals("<S:Ljava/util/List<TT;>;T:Ljava/util/List<TS;>;>Ljava/lang/Object;", sa.signature().stringValue());
    }

    interface TestMethods {
        void nothing();

        int primitive();

        String clazz();

        int[] arrayOfPrimitive();

        String[][] arrayOfClass();

        List<String> parameterized();

        Map<String, List<String>>[] arrayOfParameterized();

        <T> T typeVariable();

        <T> T[] arrayOfTypeVariable();

        <T> List<T> typeVariableAsTypeArgument();

        <T extends Number> T typeVariableWithClassBound();

        <T extends Number & Externalizable> T typeVariableWithClassAndInterfaceBounds();

        <T extends CharSequence & Serializable> T typeVariableWithOnlyInterfaceBounds();

        <T extends Number, U extends T> U typeVariableWithTypeVariableBound();

        <T extends Comparable<T>> T recursiveTypeVariable();

        <T extends List<U>, U extends List<T>> T mutuallyRecursiveTypeVariables();

        List<?> parameterizedWithUnboundedWildcard();

        List<? extends Number> parameterizedWithClassWildcard();

        <T> List<? super T> parameterizedWithTypeVariableWildcard();
    }

    @Test
    public void testConstruction() throws NoSuchMethodException {
        Class<TestMethods> clazz = TestMethods.class;

        assertEquals(genericTypeOf(clazz.getDeclaredMethod("nothing").getGenericReturnType()),
                GenericType.of(void.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("primitive").getGenericReturnType()),
                GenericType.of(int.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("clazz").getGenericReturnType()),
                GenericType.of(String.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("arrayOfPrimitive").getGenericReturnType()),
                GenericType.of(int.class).arrayType());
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("arrayOfClass").getGenericReturnType()),
                GenericType.of(String.class).arrayType().arrayType());
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("parameterized").getGenericReturnType()),
                GenericType.ofClass(List.class, TypeArgument.of(String.class)));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("arrayOfParameterized").getGenericReturnType()),
                GenericType.ofClass(Map.class, TypeArgument.of(String.class),
                        TypeArgument.ofExact(GenericType.ofClass(List.class, TypeArgument.of(String.class)))).arrayType());
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariable").getGenericReturnType()),
                GenericType.ofTypeVariable("T"));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("arrayOfTypeVariable").getGenericReturnType()),
                GenericType.ofTypeVariable("T").arrayType());
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariableAsTypeArgument").getGenericReturnType()),
                GenericType.ofClass(List.class, TypeArgument.ofExact(GenericType.ofTypeVariable("T"))));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariableWithClassBound").getGenericReturnType()),
                GenericType.ofTypeVariable("T", Number.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariableWithClassAndInterfaceBounds").getGenericReturnType()),
                GenericType.ofTypeVariable("T", Number.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariableWithOnlyInterfaceBounds").getGenericReturnType()),
                GenericType.ofTypeVariable("T", CharSequence.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("typeVariableWithTypeVariableBound").getGenericReturnType()),
                GenericType.ofTypeVariable("U", Number.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("recursiveTypeVariable").getGenericReturnType()),
                GenericType.ofTypeVariable("T", Comparable.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("mutuallyRecursiveTypeVariables").getGenericReturnType()),
                GenericType.ofTypeVariable("T", List.class));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("parameterizedWithUnboundedWildcard").getGenericReturnType()),
                GenericType.ofClass(List.class, TypeArgument.ofUnbounded()));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("parameterizedWithClassWildcard").getGenericReturnType()),
                GenericType.ofClass(List.class, TypeArgument.ofExtends(GenericType.ofClass(Number.class))));
        assertEquals(genericTypeOf(clazz.getDeclaredMethod("parameterizedWithTypeVariableWildcard").getGenericReturnType()),
                GenericType.ofClass(List.class, TypeArgument.ofSuper(GenericType.ofTypeVariable("T"))));
    }
}
