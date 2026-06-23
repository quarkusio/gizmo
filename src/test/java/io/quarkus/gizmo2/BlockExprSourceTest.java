package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;
import java.lang.constant.ClassDesc;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.Util;
import io.quarkus.gizmo2.testing.TestClassMaker;

/**
 * Tests for block expression rendering in the pseudo-Java source generator.
 * Verifies that non-void block expressions render using GCC-style {@code ({ ... })}
 * syntax and that void blocks continue to render as naked {@code { ... }} blocks.
 */
public final class BlockExprSourceTest {

    /**
     * Reads the generated source for a class from the test class loader.
     */
    private static String readSource(TestClassMaker tcm, ClassDesc testClass) throws Exception {
        String sourcePath = Util.internalName(testClass) + ".java";
        InputStream is = tcm.classLoader().getResourceAsStream(sourcePath);
        assertNotNull(is, "Source file should be produced");
        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Verifies that a basic block expression used as a method return value
     * renders with GCC-style {@code ({ ... yield value; })} syntax.
     */
    @Test
    public void basicBlockExpression() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestBasicBlockExpr");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("compute", mc -> {
                mc.returning(int.class);
                mc.body(b0 -> {
                    Expr result = b0.blockExpr(int.class, b1 -> {
                        b1.yield(Const.of(42));
                    });
                    b0.return_(result);
                });
            });
        });

        // verify the method works
        IntSupplier compute = tcm.staticMethod(testClass, "compute", IntSupplier.class);
        assertEquals(42, compute.getAsInt());

        // verify source contains block expression syntax
        String source = readSource(tcm, testClass);
        assertTrue(source.contains("({"), "Source should contain block expression opening '({'");
        assertTrue(source.contains("})"), "Source should contain block expression closing '})'");
        assertTrue(source.contains("yield 42;"), "Source should contain yield statement");
    }

    /**
     * Verifies that a block expression used as an operand in a binary operation
     * renders inline at the use site, not as a separate statement.
     */
    @Test
    public void blockExpressionInBinaryOp() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestBlockExprBinOp");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("addWithBlockExpr", mc -> {
                ParamVar x = mc.parameter("x", int.class);
                mc.returning(int.class);
                mc.body(b0 -> {
                    Expr blockResult = b0.blockExpr(int.class, b1 -> {
                        b1.yield(Const.of(10));
                    });
                    b0.return_(b0.add(x, blockResult));
                });
            });
        });

        // verify the method works
        IntUnaryOperator fn = tcm.staticMethod(testClass, "addWithBlockExpr", IntUnaryOperator.class);
        assertEquals(15, fn.applyAsInt(5));
        assertEquals(10, fn.applyAsInt(0));

        // verify source: the block expr should appear inline within the add expression
        String source = readSource(tcm, testClass);
        assertTrue(source.contains("({"), "Source should contain block expression");
        assertTrue(source.contains("yield 10;"), "Source should contain yield 10");
        // the block expression should not appear as a standalone statement
        // (it should be inline in a return or add expression)
        assertFalse(source.contains("({") && source.split("\\(\\{").length > 2,
                "Block expression should appear only once (no double-rendering)");
    }

    /**
     * Verifies that a block expression passed as a method argument
     * renders inline within the method call expression.
     */
    @Test
    public void blockExpressionAsMethodArg() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestBlockExprMethodArg");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("getGreeting", mc -> {
                mc.returning(Object.class);
                mc.body(b0 -> {
                    Expr prefix = b0.blockExpr(String.class, b1 -> {
                        b1.yield(Const.of("Hello"));
                    });
                    Expr result = b0.invokeVirtual(
                            MethodDesc.of(String.class, "concat", String.class, String.class),
                            prefix,
                            List.of(Const.of(", World")));
                    b0.return_(result);
                });
            });
        });

        // verify the method works
        @SuppressWarnings("unchecked")
        Supplier<String> fn = tcm.staticMethod(testClass, "getGreeting", Supplier.class);
        assertEquals("Hello, World", fn.get());

        // verify source structure
        String source = readSource(tcm, testClass);
        assertTrue(source.contains("({"), "Source should contain block expression");
        assertTrue(source.contains("yield \"Hello\";"), "Source should contain yield with string constant");
        assertTrue(source.contains("concat"), "Source should contain concat call");
    }

    /**
     * Verifies that a void block (from {@code block()}) still renders as a
     * naked block {@code { ... }}, not as a block expression.
     */
    @Test
    public void voidBlockRendersAsNaked() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestVoidBlockNaked");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("run", mc -> {
                mc.returning(int.class);
                mc.body(b0 -> {
                    LocalVar x = b0.localVar("x", Const.of(0));
                    b0.block(b1 -> {
                        b1.set(x, Const.of(42));
                    });
                    b0.return_(x);
                });
            });
        });

        // verify the method works
        IntSupplier fn = tcm.staticMethod(testClass, "run", IntSupplier.class);
        assertEquals(42, fn.getAsInt());

        // verify source: void block should render as naked { ... }, not as ({ ... })
        String source = readSource(tcm, testClass);
        assertTrue(source.contains("{"), "Source should contain block braces");
        assertFalse(source.contains("({"), "Void block should NOT use block expression syntax");
    }

    /**
     * Verifies that a block expression that is consumed by multiple expressions
     * (or rather, used once and its result stored) does not double-render.
     * The block expression should appear exactly once in the source output.
     */
    @Test
    public void noDoubleRendering() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestBlockExprNoDouble");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("compute", mc -> {
                mc.returning(int.class);
                mc.body(b0 -> {
                    // block expression stored in a local, then used in a return
                    Expr blockResult = b0.blockExpr(int.class, b1 -> {
                        b1.yield(Const.of(7));
                    });
                    LocalVar v = b0.localVar("v", blockResult);
                    b0.return_(b0.add(v, Const.of(3)));
                });
            });
        });

        // verify the method works
        IntSupplier fn = tcm.staticMethod(testClass, "compute", IntSupplier.class);
        assertEquals(10, fn.getAsInt());

        // verify source: block expression should appear exactly once
        String source = readSource(tcm, testClass);
        int count = countOccurrences(source, "({");
        assertEquals(1, count, "Block expression should appear exactly once, not be double-rendered");
        assertTrue(source.contains("yield 7;"), "Source should contain yield statement");
    }

    /**
     * Verifies that a block expression with multiple statements renders correctly.
     */
    @Test
    public void blockExpressionWithMultipleStatements() throws Exception {
        Gizmo g = Gizmo.create().withSourceGeneration(true);
        TestClassMaker tcm = TestClassMaker.create(g);
        ClassDesc testClass = ClassDesc.of("io.quarkus.gizmo2.TestBlockExprMultiStmt");
        tcm.gizmo().class_(testClass, cc -> {
            cc.staticMethod("compute", mc -> {
                ParamVar x = mc.parameter("x", int.class);
                mc.returning(int.class);
                mc.body(b0 -> {
                    Expr result = b0.blockExpr(int.class, b1 -> {
                        LocalVar temp = b1.localVar("temp", b1.add(x, Const.of(1)));
                        b1.yield(b1.mul(temp, Const.of(2)));
                    });
                    b0.return_(result);
                });
            });
        });

        // verify the method works: (x + 1) * 2
        IntUnaryOperator fn = tcm.staticMethod(testClass, "compute", IntUnaryOperator.class);
        assertEquals(4, fn.applyAsInt(1)); // (1+1)*2 = 4
        assertEquals(10, fn.applyAsInt(4)); // (4+1)*2 = 10
        assertEquals(2, fn.applyAsInt(0)); // (0+1)*2 = 2

        // verify source structure
        String source = readSource(tcm, testClass);
        assertTrue(source.contains("({"), "Source should contain block expression");
        assertTrue(source.contains("})"), "Source should contain block expression closing");
        assertTrue(source.contains("temp"), "Source should contain local variable in block expr");
        assertTrue(source.contains("yield"), "Source should contain yield");
    }

    /**
     * Counts occurrences of a substring in a string.
     */
    private static int countOccurrences(String text, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = text.indexOf(sub, idx)) >= 0) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
