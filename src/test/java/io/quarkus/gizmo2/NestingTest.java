package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.constant.ClassDesc;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.testing.TestClassMaker;

// verifies that we correctly fail when calling a method on a `BlockCreator`
// which is not active, because a nested `BlockCreator` is active instead
public class NestingTest {
    @Test
    public void block() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Block"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.block(b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void blockExpr() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.BlockExpr"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.blockExpr(String.class, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(""));
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void logicalAnd() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.LogicalAnd"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.logicalAnd(Const.of(true), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(true));
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void logicalOr() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.LogicalOr"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.logicalOr(Const.of(false), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(true));
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void if_() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.If"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.if_(Const.of(true), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void ifNot() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.IfNot"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.ifNot(Const.of(false), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void ifElse() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.IfElse"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.ifElse(Const.of(true), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    }, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void cond() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Cond"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.cond(int.class, Const.of(true), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(1));
                    }, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(2));
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void switchStatement() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.SwitchStatement"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.switch_(Const.of(1), sc -> {
                        sc.caseOf(0, b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        sc.caseOf(1, b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        sc.default_(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(3);
    }

    @Test
    public void switchExpression() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.SwitchExpression"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.switch_(int.class, Const.of(1), sc -> {
                        sc.caseOf(0, b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                            b1.yield(Const.of(1));
                        });
                        sc.caseOf(1, b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                            b1.yield(Const.of(2));
                        });
                        sc.default_(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                            b1.yield(Const.of(3));
                        });
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(3);
    }

    @Test
    public void loop() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Loop"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.loop(b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.break_(b1);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void while_() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.While"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.while_(b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(true));
                    }, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void doWhile() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.DoWhile"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.doWhile(b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    }, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(true));
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void forEach() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.ForEach"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    Expr list = b0.listOf(Const.of("a"), Const.of("b"), Const.of("c"));
                    b0.forEach(list, (b1, it) -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void tryCatch() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TryCatch"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.try_(tc -> {
                        tc.body(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        tc.catch_(Exception.class, "e", (b1, e) -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void tryFinally() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TryFinally"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.try_(tc -> {
                        tc.body(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        tc.finally_(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(2);
    }

    @Test
    public void tryCatchFinally() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TryCatchFinally"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.try_(tc -> {
                        tc.body(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        tc.catch_(Exception.class, "e", (b1, e) -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                        tc.finally_(b1 -> {
                            asserter.expectedNestingFailure(b0::return_);
                        });
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(3);
    }

    @Test
    public void assert_() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Assert"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.assert_(b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                        b1.yield(Const.of(true));
                    }, "assertion");
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void synchronized_() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Synchronized"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    b0.synchronized_(cc.this_(), b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    @Test
    public void locked() {
        NestingFailureAsserter asserter = new NestingFailureAsserter();

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        g.class_(ClassDesc.of("io.quarkus.gizmo2.Locked"), cc -> {
            cc.staticMethod("hello", mc -> {
                mc.body(b0 -> {
                    LocalVar lock = b0.localVar("lock", b0.new_(ReentrantLock.class));
                    b0.locked(lock, b1 -> {
                        asserter.expectedNestingFailure(b0::return_);
                    });
                    b0.return_();
                });
            });
        });

        asserter.expectedFailures(1);
    }

    private static final class NestingFailureAsserter {
        private static final StackWalker SW = StackWalker.getInstance();

        private int counter = 0;

        void expectedNestingFailure(Runnable action) {
            // only assert when the `action` is called before writing code
            boolean isWriteCode = SW.walk(frames -> {
                return frames.anyMatch(frame -> {
                    return "io.quarkus.gizmo2.impl.BlockCreatorImpl".equals(frame.getClassName())
                            && "writeCode".equals(frame.getMethodName());
                });
            });
            if (isWriteCode) {
                return;
            }

            IllegalStateException e = assertThrowsExactly(IllegalStateException.class, action::run);
            assertTrue(e.getMessage().contains("This block is currently not active, because a nested block is being created"));
            counter++;
        }

        void expectedFailures(int count) {
            assertEquals(count, counter);
        }
    }
}
