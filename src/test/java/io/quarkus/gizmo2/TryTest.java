package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

public final class TryTest {
    @Test
    public void testJavacCrasher() {
        // This code construct crashes javac as of 2025-03-28.
        // The error is "code too large for try statement".
        // We really only pass this because of a gimmick, but it's cool anyway.
        //
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // try { foo.equals(bar); } finally {
        // return foo+bar;
        // }}}}}}}}}}}}

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc crashIt = g.class_("io.quarkus.gizmo2.CrashJavac", cc -> {
            cc.staticMethod("crashIt", smc -> {
                ParamVar foo = smc.parameter("foo", String.class);
                ParamVar bar = smc.parameter("bar", String.class);
                smc.returning(String.class);
                smc.body(b0 -> {
                    b0.try_(try0 -> {
                        try0.body(b1 -> b1.withObject(foo).equals_(bar));
                        try0.finally_(b1 -> b1.try_(try1 -> {
                            try1.body(b2 -> b2.withObject(foo).equals_(bar));
                            try1.finally_(b2 -> b2.try_(try2 -> {
                                try2.body(b3 -> b3.withObject(foo).equals_(bar));
                                try2.finally_(b3 -> b3.try_(try3 -> {
                                    try3.body(b4 -> b4.withObject(foo).equals_(bar));
                                    try3.finally_(b4 -> b4.try_(try4 -> {
                                        try4.body(b5 -> b5.withObject(foo).equals_(bar));
                                        try4.finally_(b5 -> b5.try_(try5 -> {
                                            try5.body(b6 -> b6.withObject(foo).equals_(bar));
                                            try5.finally_(b6 -> b6.try_(try6 -> {
                                                try6.body(b7 -> b7.withObject(foo).equals_(bar));
                                                try6.finally_(b7 -> b7.try_(try7 -> {
                                                    try7.body(b8 -> b8.withObject(foo).equals_(bar));
                                                    try7.finally_(b8 -> b8.try_(try8 -> {
                                                        try8.body(b9 -> b9.withObject(foo).equals_(bar));
                                                        try8.finally_(b9 -> b9.try_(try9 -> {
                                                            try9.body(b10 -> b10.withObject(foo).equals_(bar));
                                                            try9.finally_(b10 -> b10.try_(try10 -> {
                                                                try10.body(b11 -> b11.withObject(foo).equals_(bar));
                                                                try10.finally_(b11 -> b11.try_(try11 -> {
                                                                    try11.body(b12 -> b12.withObject(foo).equals_(bar));
                                                                    try11.finally_(b12 -> b12.try_(try12 -> {
                                                                        try12.body(b13 -> b13.withObject(foo).equals_(bar));
                                                                        try12.finally_(b13 -> b13.return_(
                                                                                b13.withString(foo).concat(bar)));
                                                                    }));
                                                                }));
                                                            }));
                                                        }));
                                                    }));
                                                }));
                                            }));
                                        }));
                                    }));
                                }));
                            }));
                        }));
                    });
                });
            });
        });
        assertEquals("hello" + "world", tcm.staticMethod(crashIt, "crashIt", CrashIt.class).crashIt("hello", "world"));
    }

    @Test
    public void testCatch() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc catchTests = g.class_("io.quarkus.gizmo2.CatchTests", cc -> {
            // make sure it works *at all*
            cc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    b0.try_(try0 -> {
                        try0.body(b1 -> {
                            b1.div(123, Const.of(0));
                            b1.returnFalse();
                        });
                        try0.catch_(ArithmeticException.class, "e", (b1, e) -> {
                            b1.returnTrue();
                        });
                    });
                });
            });
            // ensure that catch ordering works correctly
            cc.staticMethod("test1", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    b0.try_(try0 -> {
                        try0.body(b1 -> {
                            b1.div(123, Const.of(0));
                            b1.returnFalse();
                        });
                        // this one should take precedence because it comes first
                        try0.catch_(RuntimeException.class, "e", (b1, e) -> {
                            b1.returnTrue();
                        });
                        try0.catch_(ArithmeticException.class, "e", (b1, e) -> {
                            b1.returnFalse();
                        });
                    });
                });
            });
            // test catch-all
            cc.staticMethod("test2", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    b0.try_(try0 -> {
                        try0.body(b1 -> {
                            b1.div(123, Const.of(0));
                            b1.returnFalse();
                        });
                        try0.catch_(Throwable.class, "e", (b1, e) -> {
                            b1.returnTrue();
                        });
                    });
                });
            });
        });
        assertTrue(tcm.staticMethod(catchTests, "test0", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod(catchTests, "test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod(catchTests, "test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void testFinallySimpleCases() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc tests = g.class_("io.quarkus.gizmo2.FinallySimpleCasesTests", cc -> {
            // test finally-overrides-return
            cc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    b0.try_(try0 -> {
                        try0.body(BlockCreator::returnFalse);
                        try0.finally_(BlockCreator::returnTrue);
                    });
                });
            });
            // make sure finally happens after the body when the body falls through
            cc.staticMethod("test1", smc -> {
                smc.returning(int.class);
                smc.body(b0 -> {
                    LocalVar gotIt = b0.localVar("gotIt", Const.of(0));
                    b0.try_(try0 -> {
                        try0.body(b1 -> b1.set(gotIt, Const.of(1)));
                        try0.finally_(b1 -> b1.set(gotIt, Const.of(2)));
                    });
                    b0.return_(gotIt);
                });
            });
        });
        assertTrue(tcm.staticMethod(tests, "test0", BooleanSupplier.class).getAsBoolean());
        assertEquals(2, tcm.staticMethod(tests, "test1", IntSupplier.class).getAsInt());
    }

    @Test
    public void testFinallyControlFlow() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc tests = g.class_("io.quarkus.gizmo2.FinallyControlFlowTests", cc -> {
            // make sure that finally is run when exiting the block via break
            cc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    LocalVar i = b0.localVar("i", Const.of(1));
                    LocalVar ran = b0.localVar("ran", Const.of(false));
                    b0.while_(cond1 -> cond1.yield(cond1.lt(i, 10)), b1 -> {
                        b1.try_(try2 -> {
                            try2.body(b3 -> {
                                b3.if_(b3.eq(b3.rem(i, 4), 0), t4 -> {
                                    t4.break_(b1);
                                });
                            });
                            try2.finally_(b3 -> {
                                b3.set(ran, Const.of(true));
                            });
                        });
                        b1.inc(i);
                    });
                    b0.return_(ran);
                });
            });
            // make sure that finally is run when exiting the block via redo
            cc.staticMethod("test1", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    LocalVar i = b0.localVar("i", Const.of(1));
                    LocalVar ran = b0.localVar("ran", Const.of(false));
                    b0.while_(cond1 -> cond1.yield(cond1.lt(i, 10)), b1 -> {
                        b1.try_(try2 -> {
                            try2.body(b3 -> {
                                b3.if_(b3.eq(b3.rem(i, 4), 0), t4 -> {
                                    t4.inc(i);
                                    t4.goto_(b1);
                                });
                            });
                            try2.finally_(b3 -> {
                                b3.set(ran, Const.of(true));
                            });
                        });
                        b1.inc(i);
                    });
                    b0.return_(ran);
                });
            });
            // make sure that finally is run when exiting the block via return
            StaticFieldVar ran = cc.staticField("ran", ifc -> {
                ifc.setType(boolean.class);
            });
            MethodDesc test2 = cc.staticMethod("test2body", smc -> {
                ParamVar i = smc.parameter("i", int.class);
                smc.body(b0 -> {
                    b0.set(ran, Const.of(false));
                    b0.while_(cond1 -> cond1.yield(cond1.lt(i, 10)), b1 -> {
                        b1.try_(try2 -> {
                            try2.body(b3 -> {
                                b3.if_(b3.eq(b3.rem(i, 4), 0), BlockCreator::return_);
                            });
                            try2.finally_(b3 -> {
                                b3.set(ran, Const.of(true));
                            });
                        });
                        b1.inc(i);
                    });
                    b0.return_();
                });
            });
            cc.staticMethod("test2", smc -> {
                smc.returning(boolean.class);
                ParamVar i = smc.parameter("i", int.class);
                smc.body(b0 -> {
                    b0.invokeStatic(test2, i);
                    b0.return_(ran);
                });
            });
            cc.staticMethod("test3", mc -> {
                mc.returning(boolean.class);
                mc.body(b0 -> {
                    b0.try_(tc -> {
                        tc.body(BlockCreator::returnFalse);
                        tc.finally_(BlockCreator::returnTrue);
                    });
                });
            });
            cc.staticMethod("test4", mc -> {
                mc.returning(int.class);
                mc.body(b0 -> {
                    LocalVar result = b0.localVar("result", Const.of(0));
                    b0.try_(tc -> {
                        tc.body(b1 -> {
                            b1.set(result, Const.of(1));
                            b1.return_(result);
                        });
                        tc.finally_(b1 -> {
                            // this is ignored, just like how javac compiles equivalent code
                            b1.set(result, Const.of(2));
                        });
                    });
                });
            });
        });
        assertTrue(tcm.staticMethod(tests, "test0", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod(tests, "test1", BooleanSupplier.class).getAsBoolean());
        for (int i = 0; i < 10; i++) {
            assertTrue(tcm.staticMethod(tests, "test2", IntToBooleanFunction.class).apply(i));
        }
        assertTrue(tcm.staticMethod(tests, "test3", BooleanSupplier.class).getAsBoolean());
        assertEquals(1, tcm.staticMethod(tests, "test4", IntSupplier.class).getAsInt());
    }

    @Test
    public void testTryCatchFinally() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc tests = g.class_("io.quarkus.gizmo2.TryCatchFinallyTests", cc -> {
            cc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    LocalVar ranCatch = b0.localVar("ranCatch", Const.of(false));
                    LocalVar ranFinally = b0.localVar("ranFinally", Const.of(false));
                    // use line numbers to make debugging stack traces more readable
                    b0.line(1);
                    b0.try_(try1 -> {
                        try1.body(b2 -> {
                            b2.line(2);
                            b2.rem(Const.of(1), 0);
                            b2.line(3);
                        });
                        try1.catch_(ArithmeticException.class, "e", (b2, e) -> {
                            b2.line(4);
                            b2.set(ranCatch, Const.of(true));
                            b2.line(5);
                            // fall out (drop exception)
                        });
                        try1.finally_(b2 -> {
                            b2.line(6);
                            b2.set(ranFinally, Const.of(true));
                            b2.line(7);
                        });
                    });
                    b0.line(8);
                    b0.return_(b0.and(ranCatch, ranFinally));
                });
            });
            // throw-from-finally
            cc.staticMethod("test1", smc -> {
                smc.returning(boolean.class);
                smc.body(b0 -> {
                    LocalVar ran = b0.localVar("ran", Const.of(false));
                    b0.try_(try1 -> {
                        try1.body(b2 -> {
                            b2.try_(try3 -> {
                                try3.body(BlockCreator::returnFalse);
                                try3.finally_(b4 -> {
                                    b4.throw_(IllegalStateException.class, "override!");
                                });
                            });
                        });
                        try1.catch_(IllegalStateException.class, "e", (b2, e) -> {
                            b2.set(ran, Const.of(true));
                            // fall thru
                        });
                    });
                    b0.return_(ran);
                });
            });
        });
        assertTrue(tcm.staticMethod(tests, "test0", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod(tests, "test1", BooleanSupplier.class).getAsBoolean());
    }

    @FunctionalInterface
    public interface CrashIt {
        String crashIt(String foo, String bar);
    }

    @FunctionalInterface
    public interface IntToBooleanFunction {
        boolean apply(int i);
    }
}
