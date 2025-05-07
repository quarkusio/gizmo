package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.dmlloyd.classfile.Attributes;
import io.github.dmlloyd.classfile.ClassModel;
import io.github.dmlloyd.classfile.CodeModel;
import io.github.dmlloyd.classfile.FieldModel;
import io.github.dmlloyd.classfile.MethodModel;
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
                    LocalVar list = b0.declare("list", GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
                    b0.set(list, Const.ofNull(List.class));
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
                    LocalVar foo = b0.declare("foo", GenericType.of(String.class).withAnnotation(Visible.class));
                    b0.set(foo, Const.of("hello"));
                    b0.return_();
                });
            });
            zc.staticMethod("test1", mc -> {
                mc.body(b0 -> {
                    LocalVar foo = b0.declare("foo", GenericType.of(String.class).withAnnotation(Invisible.class));
                    b0.set(foo, Const.of("hello"));
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
                    pc.withType(GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
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
                    pc.withType(GenericType.of(String.class).withAnnotation(Visible.class));
                });
                mc.body(BlockCreator::return_);
            });
            zc.staticMethod("test1", mc -> {
                mc.parameter("foo", pc -> {
                    pc.withType(GenericType.of(String.class).withAnnotation(Invisible.class));
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
    public void testGenericField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TestGenericField", zc -> {
            zc.field("test0", fc -> {
                fc.withType(GenericType.of(List.class, List.of(TypeArgument.of(String.class))));
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
                fc.withType(GenericType.of(String.class).withAnnotation(Visible.class));
            });
            zc.field("test1", fc -> {
                fc.withType(GenericType.of(String.class).withAnnotation(Invisible.class));
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

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Visible {
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.CLASS)
    public @interface Invisible {
    }
}
