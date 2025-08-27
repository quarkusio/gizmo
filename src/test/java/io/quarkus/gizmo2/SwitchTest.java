package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;
import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.function.IntUnaryOperator;

import org.junit.jupiter.api.Test;

public final class SwitchTest {

    @FunctionalInterface
    public interface CharUnaryOperator {
        char apply(char operand);
    }

    @Test
    public void testCharSwitch() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestCharSwitchExpr"), zc -> {
            zc.staticMethod("frobnicate", mc -> {
                mc.returning(char.class);
                ParamVar cp = mc.parameter("cp", char.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_char, cp, sc -> {
                        sc.case_(cc -> {
                            cc.of('a');
                            cc.body(b1 -> b1.yield(Const.of('i')));
                        });
                        sc.case_(cc -> {
                            cc.of('e');
                            cc.body(b1 -> b1.yield(Const.of('o')));
                        });
                        sc.case_(cc -> {
                            cc.of('i');
                            cc.body(b1 -> b1.yield(Const.of('u')));
                        });
                        sc.case_(cc -> {
                            cc.of('o');
                            cc.body(b1 -> b1.yield(Const.of('a')));
                        });
                        sc.case_(cc -> {
                            cc.of('u');
                            cc.body(b1 -> b1.yield(Const.of('e')));
                        });
                        sc.default_(b1 -> b1.yield(cp));
                    }));
                });
            });
        });
        CharUnaryOperator frobnicate = tcm.staticMethod("frobnicate", CharUnaryOperator.class);
        assertEquals('i', frobnicate.apply('a'));
        assertEquals('q', frobnicate.apply('q'));
        assertEquals('o', frobnicate.apply('e'));
    }

    @Test
    public void testIntSwitch() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestIntSwitch"), zc -> {
            zc.staticMethod("frobnicate", mc -> {
                mc.returning(int.class);
                ParamVar cp = mc.parameter("cp", int.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_int, cp, sc -> {
                        sc.case_(cc -> {
                            cc.of('a');
                            cc.body(b1 -> b1.yield(Const.of((int) 'i')));
                        });
                        sc.case_(cc -> {
                            cc.of('e');
                            cc.body(b1 -> b1.yield(Const.of((int) 'o')));
                        });
                        sc.case_(cc -> {
                            cc.of('i');
                            cc.body(b1 -> b1.yield(Const.of((int) 'u')));
                        });
                        sc.case_(cc -> {
                            cc.of('o');
                            cc.body(b1 -> b1.yield(Const.of((int) 'a')));
                        });
                        sc.case_(cc -> {
                            cc.of('u');
                            cc.body(b1 -> b1.yield(Const.of((int) 'e')));
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
    public void testIntSwitchWithoutDefaultCase() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestIntSwitch"), zc -> {
            zc.staticMethod("frobnicate", mc -> {
                mc.returning(int.class);
                ParamVar cp = mc.parameter("cp", int.class);
                mc.body(b0 -> {
                    b0.switch_(cp, sc -> {
                        sc.caseOf('a', b1 -> b1.return_(Const.of((int) 'i')));
                        sc.caseOf('e', b1 -> b1.return_(Const.of((int) 'o')));
                        sc.caseOf('i', b1 -> b1.return_(Const.of((int) 'u')));
                        sc.caseOf('o', b1 -> b1.return_(Const.of((int) 'a')));
                        sc.caseOf('u', b1 -> b1.return_(Const.of((int) 'e')));
                    });
                    b0.return_(cp);
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
                            cc.body(b1 -> b1.yield(Const.of("It's String!")));
                        });
                        sc.case_(cc -> {
                            cc.of(Integer.class);
                            cc.of(int.class);
                            cc.body(b1 -> b1.yield(Const.of("Some kinda integer!")));
                        });
                        sc.default_(b1 -> {
                            b1.yield(Const.of("Gosh, I dunno"));
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
                        sc.caseOf("zero", b1 -> b1.yield(Const.of(0)));
                        sc.caseOf("one", b1 -> b1.yield(Const.of(1)));
                        sc.caseOf("two", b1 -> b1.yield(Const.of(2)));
                        sc.caseOf("three", b1 -> b1.yield(Const.of(3)));
                        sc.default_(b1 -> b1.yield(Const.of(-1)));
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

    @Test
    public void testStringSwitchWeird() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestStringSwitch", cc -> {
            cc.staticMethod("guess", mc -> {
                mc.returning(void.class);
                ParamVar arg = mc.parameter("arg", String.class);
                mc.body(b0 -> {
                    b0.switch_(arg, sc -> {
                        sc.caseOf("help", b1 -> b1.printf("foo"));
                        sc.default_(b1 -> b1.printf("bar"));
                    });
                    b0.return_();
                });
            });
        });
    }

    @Test
    public void testStringSwitchWithoutDefaultCase() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestStringSwitch"), zc -> {
            zc.staticMethod("nameToNumber", mc -> {
                mc.returning(int.class);
                ParamVar name = mc.parameter("name", String.class);
                mc.body(b0 -> {
                    b0.switch_(name, sc -> {
                        sc.caseOf("zero", b1 -> b1.return_(Const.of(0)));
                        sc.caseOf("one", b1 -> b1.return_(Const.of(1)));
                        sc.caseOf("two", b1 -> b1.return_(Const.of(2)));
                        sc.caseOf("three", b1 -> b1.return_(Const.of(3)));
                    });
                    b0.return_(Const.of(-1));
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

    @Test
    public void testIntSwitchWithNoCase() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestIntSwitchExpr"), zc -> {
            zc.staticMethod("frobnicate", mc -> {
                mc.returning(int.class);
                ParamVar cp = mc.parameter("cp", int.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_int, cp, sc -> {
                        sc.default_(b1 -> b1.yield(cp));
                    }));
                });
            });
        });
        IntUnaryOperator frobnicate = tcm.staticMethod("frobnicate", IntUnaryOperator.class);
        assertEquals('a', frobnicate.applyAsInt('a'));
        assertEquals('q', frobnicate.applyAsInt('q'));
        assertEquals('e', frobnicate.applyAsInt('e'));
    }

    @Test
    public void testStringSwitchWithNoCase() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestStringSwitch"), zc -> {
            zc.staticMethod("nameToNumber", mc -> {
                mc.returning(int.class);
                ParamVar name = mc.parameter("name", String.class);
                mc.body(b0 -> {
                    b0.return_(b0.switch_(CD_int, name, sc -> {
                        sc.default_(b1 -> b1.yield(Const.of(-1)));
                    }));
                });
            });
        });
        NumberParser nameToNumber = tcm.staticMethod("nameToNumber", NumberParser.class);
        assertEquals(-1, nameToNumber.get("zero"));
        assertEquals(-1, nameToNumber.get("one"));
        assertEquals(-1, nameToNumber.get("two"));
    }

    @Test
    public void testSwitchExpressionFallsThrough() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestStringSwitch"), zc -> {
            zc.staticMethod("nameToNumber", mc -> {
                mc.returning(int.class);
                ParamVar name = mc.parameter("name", String.class);
                mc.body(b0 -> {
                    // no branch in the `switch` may fall through, but the entire `switch`
                    // may, because it's an expression and is supposed to be used as such
                    b0.return_(b0.switch_(CD_int, name, sc -> {
                        sc.default_(b1 -> b1.throw_(IllegalArgumentException.class));
                    }));
                });
            });
        });
        NumberParser nameToNumber = tcm.staticMethod("nameToNumber", NumberParser.class);
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("zero"));
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("one"));
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("two"));
    }

    @Test
    public void testSwitchStatementDoesNotFallThrough() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestStringSwitch"), zc -> {
            zc.staticMethod("nameToNumber", mc -> {
                mc.returning(int.class);
                ParamVar name = mc.parameter("name", String.class);
                mc.body(b0 -> {
                    // no branch in the `switch` may fall through, so the entire `switch`
                    // may not either
                    b0.switch_(name, sc -> {
                        sc.default_(b1 -> b1.throw_(IllegalArgumentException.class));
                    });
                    // note that attempting to add the following statement would fail
                    // with "This block has already been finished"
                    //b0.return_(Const.of(0));
                });
            });
        });
        NumberParser nameToNumber = tcm.staticMethod("nameToNumber", NumberParser.class);
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("zero"));
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("one"));
        assertThrows(IllegalArgumentException.class, () -> nameToNumber.get("two"));
    }
}
