package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.function.IntUnaryOperator;
import java.util.function.ToIntFunction;

import io.quarkus.gizmo2.impl.constant.IntConstant;
import org.junit.jupiter.api.Test;

public final class TestSwitch {

    @Test
    public void testIntSwitch() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestIntSwitchExpr"), zc -> {
            zc.staticMethod("frobnicate", mc -> {
                mc.returning(int.class);
                ParamVar cp = mc.parameter("cp", int.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_int, cp, sc -> {
                        sc.case_(cc -> {
                            cc.of('a');
                            cc.body(b1 -> b1.yield(IntConstant.of('i')));
                        });
                        sc.case_(cc -> {
                            cc.of('e');
                            cc.body(b1 -> b1.yield(IntConstant.of('o')));
                        });
                        sc.case_(cc -> {
                            cc.of('i');
                            cc.body(b1 -> b1.yield(IntConstant.of('u')));
                        });
                        sc.case_(cc -> {
                            cc.of('o');
                            cc.body(b1 -> b1.yield(IntConstant.of('a')));
                        });
                        sc.case_(cc -> {
                            cc.of('u');
                            cc.body(b1 -> b1.yield(IntConstant.of('e')));
                        });
                        sc.default_(b1 -> b1.yield(cp));
                    }));
                });
            });
        });
        IntUnaryOperator frobnicate = tcm.staticMethod("frobnicate", IntUnaryOperator.class);
        assertEquals('i', frobnicate.applyAsInt('a'));
        assertEquals('q', frobnicate.applyAsInt('q'));
        assertEquals('o', frobnicate.applyAsInt('e'));
    }

    @Test
    public void testClassSwitch() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestClassSwitch"), zc -> {
            zc.staticMethod("nameThatClass", mc -> {
                mc.returning(String.class);
                ParamVar clazz = mc.parameter("clazz", Class.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_String, clazz, sc -> {
                        sc.case_(cc -> {
                            cc.of(String.class);
                            cc.body(b1 -> b1.yield(Constant.of("It's String!")));
                        });
                        sc.case_(cc -> {
                            cc.of(Integer.class);
                            cc.of(int.class);
                            cc.body(b1 -> b1.yield(Constant.of("Some kinda integer!")));
                        });
                        sc.default_(b1 -> {
                            b1.yield(Constant.of("Gosh, I dunno"));
                        });
                    }));
                });
            });
        });
        ClassNamer nameThatClass = tcm.staticMethod("nameThatClass", ClassNamer.class);
        assertEquals("It's String!", nameThatClass.nameIt(String.class));
        assertEquals("Some kinda integer!", nameThatClass.nameIt(Integer.class));
        assertEquals("Some kinda integer!", nameThatClass.nameIt(int.class));
        assertEquals("Gosh, I dunno", nameThatClass.nameIt(long.class));
        assertEquals("Gosh, I dunno", nameThatClass.nameIt(void.class));
        assertEquals("Gosh, I dunno", nameThatClass.nameIt(char.class));
        assertEquals("Gosh, I dunno", nameThatClass.nameIt(Long.class));
        assertEquals("Gosh, I dunno", nameThatClass.nameIt(Class.class));
    }

    public interface ClassNamer {
        String nameIt(Class<?> clazz);
    }

    @Test
    public void testStringSwitch() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestStringSwitch"), zc -> {
            zc.staticMethod("nameToNumber", mc -> {
                mc.returning(int.class);
                ParamVar name = mc.parameter("name", String.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_int, name, sc -> {
                        sc.caseOf("zero", b1 -> b1.yield(Constant.of(0)));
                        sc.caseOf("one", b1 -> b1.yield(Constant.of(1)));
                        sc.caseOf("two", b1 -> b1.yield(Constant.of(2)));
                        sc.caseOf("three", b1 -> b1.yield(Constant.of(3)));
                        sc.default_(b1 -> b1.yield(Constant.of(-1)));
                    }));
                });
            });
        });
        NumberParser nameToNumber = tcm.staticMethod("nameToNumber", NumberParser.class);
        assertEquals(0, nameToNumber.get("zero"));
        assertEquals(1, nameToNumber.get("one"));
        assertEquals(2, nameToNumber.get("two"));
        assertEquals(3, nameToNumber.get("three"));
        assertEquals(-1, nameToNumber.get("four"));
    }

    public interface NumberParser {
        int get(String name);
    }
}
