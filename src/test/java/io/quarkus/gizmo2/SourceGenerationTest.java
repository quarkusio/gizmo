package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodType;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;
import io.quarkus.gizmo2.testing.TestClassMaker;
import io.smallrye.classfile.Attributes;
import io.smallrye.classfile.ClassFile;
import io.smallrye.classfile.ClassModel;
import io.smallrye.classfile.CodeModel;
import io.smallrye.classfile.MethodModel;
import io.smallrye.classfile.attribute.LineNumberTableAttribute;
import io.smallrye.classfile.attribute.SourceFileAttribute;

/**
 * Tests for the pseudo-Java source code generation feature.
 */
public final class SourceGenerationTest {

    /**
     * Verifies that source generation produces a source file, sets the SourceFileAttribute,
     * and includes LineNumberTable entries.
     */
    @Test
    public void basicSourceGeneration() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestSourceGenOutput");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("add", mc -> {
                ParamVar a = mc.parameter("a", int.class);
                ParamVar b = mc.parameter("b", int.class);
                mc.returning(int.class);
                mc.body(b0 -> {
                    b0.return_(b0.add(a, b));
                });
            });
        });

        // verify source file is produced
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // verify basic structure
        assertTrue(source.contains("package io.quarkus.gizmo2;"), "Source should contain package declaration");
        assertTrue(source.contains("class TestSourceGenOutput"), "Source should contain class declaration");
        assertTrue(source.contains("static"), "Source should contain static modifier");
        assertTrue(source.contains("return"), "Source should contain return statement");

        // verify bytecode has SourceFileAttribute
        ClassModel cm = tcm.readClass(testClass, bytes -> ClassFile.of().parse(bytes));
        SourceFileAttribute sfa = cm.findAttribute(Attributes.sourceFile()).orElse(null);
        assertNotNull(sfa, "SourceFileAttribute should be set");
        assertEquals("TestSourceGenOutput.java", sfa.sourceFile().stringValue());

        // verify bytecode has LineNumberTable
        MethodModel addMethod = cm.methods().stream()
                .filter(m -> m.methodName().equalsString("add"))
                .findFirst()
                .orElseThrow();
        CodeModel code = addMethod.code().orElse(null);
        assertNotNull(code, "Code should exist");
        LineNumberTableAttribute lnt = code.findAttribute(Attributes.lineNumberTable()).orElse(null);
        assertNotNull(lnt, "LineNumberTable should exist when source generation is enabled");
        assertFalse(lnt.lineNumbers().isEmpty(), "LineNumberTable should have entries");

        // verify the method works
        IntBinaryOperator add = tcm.staticMethod(testClass, "add", IntBinaryOperator.class);
        assertEquals(7, add.applyAsInt(3, 4));
    }

    /**
     * Verifies that source generation is not produced when disabled.
     */
    @Test
    public void noSourceWhenDisabled() {
        TestClassMaker tcm = TestClassMaker.create();
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestNoSourceOutput");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("noop", mc -> {
                mc.body(b0 -> {
                    b0.return_();
                });
            });
        });

        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNull(is, "Source file should not be produced when source generation is disabled");
    }

    /**
     * Verifies that a generated method with control flow produces reasonable source.
     */
    @Test
    public void controlFlowSource() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestControlFlowOutput");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("abs", mc -> {
                ParamVar x = mc.parameter("x", int.class);
                mc.returning(int.class);
                mc.body(b0 -> {
                    b0.ifElse(b0.lt(x, Const.of(0)), b1 -> {
                        b1.return_(b1.neg(x));
                    }, b1 -> {
                        b1.return_(x);
                    });
                });
            });
        });

        // verify the method works
        IntUnaryOperator abs = tcm.staticMethod(testClass, "abs", IntUnaryOperator.class);
        assertEquals(5, abs.applyAsInt(5));
        assertEquals(5, abs.applyAsInt(-5));
        assertEquals(0, abs.applyAsInt(0));

        // verify source structure
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(source.contains("if ("), "Source should contain if statement");
        assertTrue(source.contains("} else {"), "Source should contain else clause");
    }

    /**
     * Verifies source generation with method invocations.
     */
    @Test
    public void invocationSource() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestInvocationOutput");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("greet", mc -> {
                ParamVar name = mc.parameter("name", String.class);
                mc.returning(String.class);
                mc.body(b0 -> {
                    Expr concat = b0.invokeVirtual(
                            MethodDesc.of(String.class, "concat", String.class, String.class),
                            Const.of("Hello, "),
                            List.of(name));
                    b0.return_(concat);
                });
            });
        });

        // verify the method works
        @SuppressWarnings("unchecked")
        Function<String, String> greet = tcm.staticMethod(testClass, "greet",
                MethodType.methodType(String.class, String.class), Function.class);
        assertEquals("Hello, World", greet.apply("World"));

        // verify source structure
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        assertTrue(source.contains("concat"), "Source should contain method invocation");
        assertTrue(source.contains("\"Hello, \""), "Source should contain string constant");
    }

    /**
     * A test annotation with runtime retention for annotation source generation testing.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER })
    @interface TestAnno {
        /**
         * {@return the annotation value}
         */
        String value();
    }

    /**
     * Verifies that annotations on classes, methods, fields, and parameters appear in the generated source.
     */
    @Test
    public void annotationSource() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestAnnotationOutput");
        tcm.gizmo().class_(testClass, cc -> {
            // class-level annotation
            cc.addAnnotation(Deprecated.class);
            cc.addAnnotation(TestAnno.class, ann -> {
                ann.add(TestAnno::value, "classLevel");
            });
            // annotated field
            cc.staticField("count", fc -> {
                fc.setType(int.class);
                fc.addAnnotation(Deprecated.class);
            });
            // annotated method with annotated parameter
            cc.staticMethod("identity", mc -> {
                mc.addAnnotation(Deprecated.class);
                mc.returning(String.class);
                ParamVar input = mc.parameter("input", pc -> {
                    pc.setType(String.class);
                    pc.addAnnotation(TestAnno.class, ann -> {
                        ann.add(TestAnno::value, "paramLevel");
                    });
                });
                mc.body(b0 -> {
                    b0.return_(input);
                });
            });
        });

        // verify source annotations
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // class-level annotations (java.lang and same-package types are simplified)
        assertTrue(source.contains("@Deprecated"), "Source should contain @Deprecated on class");
        assertTrue(source.contains("@SourceGenerationTest$TestAnno(\"classLevel\")"),
                "Source should contain @TestAnno on class with simplified name");

        // field annotations and declaration
        assertTrue(source.contains("int count;"), "Source should contain field declaration");

        // method annotations — @Deprecated should appear before the method signature
        // parameter annotations — @TestAnno("paramLevel") should appear inline with simplified name
        assertTrue(source.contains("@SourceGenerationTest$TestAnno(\"paramLevel\")"),
                "Source should contain parameter annotation with simplified name");

        // no import statements for java.lang or same-package types
        assertFalse(source.contains("import java.lang."), "Should not import java.lang types");
        assertFalse(source.contains("import io.quarkus.gizmo2.SourceGenerationTest"),
                "Should not import same-package types");
    }

    /**
     * Verifies that import statements are generated and type names are simplified.
     */
    @Test
    public void importGeneration() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestImportOutput");
        ClassDesc CD_List = ClassDesc.of("java.util.List");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("process", mc -> {
                ParamVar list = mc.parameter("list", CD_List);
                mc.returning(String.class);
                mc.body(b0 -> {
                    b0.return_(Const.of("done"));
                });
            });
        });

        // retrieve generated source
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        String source = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        // verify import for java.util.List
        assertTrue(source.contains("import java.util.List;"),
                "Should generate import for java.util.List");

        // verify simplified names in method signature
        assertTrue(source.contains("List list"), "Should use simple name for List parameter");
        assertTrue(source.contains("String process"), "Should use simple name for String return type");

        // verify no import for java.lang types
        assertFalse(source.contains("import java.lang."), "Should not import java.lang types");

        // verify package declaration
        assertTrue(source.contains("package io.quarkus.gizmo2;"), "Should have package declaration");
    }
}
