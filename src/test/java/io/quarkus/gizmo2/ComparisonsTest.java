package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

public class ComparisonsTest {
    @Test
    public void cmp() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cmp", cc -> {
            cc.staticMethod("test1", mc -> {
                // static int test1() {
                //     return cmp(5, 7);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(Const.of(5), Const.of(7)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static int test2() {
                //     return cmp(7, 5);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(Const.of(7), Const.of(5)));
                });
            });
            cc.staticMethod("test3", mc -> {
                // static int test3() {
                //     return cmp(7, 7);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(Const.of(7), Const.of(7)));
                });
            });
        });
        assertEquals(-1, tcm.staticMethod("test1", IntSupplier.class).getAsInt());
        assertEquals(1, tcm.staticMethod("test2", IntSupplier.class).getAsInt());
        assertEquals(0, tcm.staticMethod("test3", IntSupplier.class).getAsInt());
    }

    @Test
    public void eq() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.EQ", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 == 7;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(5), Const.of(7)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 == 7;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(7), Const.of(7)));
                });
            });
            cc.staticMethod("test3", mc -> {
                // static boolean test3() {
                //     return 0 == 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test4", mc -> {
                // static boolean test4() {
                //     return 1 == 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(1), Const.of(0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void eqObjects() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.EQ", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     Object obj = new Object();
                //     return obj == obj;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    LocalVar obj = bc.localVar("obj", bc.new_(Object.class));
                    bc.return_(bc.eq(obj, obj));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return new Object() == new Object();
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(bc.new_(Object.class), bc.new_(Object.class)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void ne() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5L != 7L;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(5L), Const.of(7L)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7L != 7L;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(7L), Const.of(7L)));
                });
            });
            cc.staticMethod("test3", mc -> {
                // static boolean test3() {
                //     return 0 != 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test4", mc -> {
                // static boolean test4() {
                //     return 1 != 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(1), Const.of(0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void neObjects() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     Object obj = new Object();
                //     return obj != obj;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    LocalVar obj = bc.localVar("obj", bc.new_(Object.class));
                    bc.return_(bc.ne(obj, obj));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return new Object() != new Object();
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(bc.new_(Object.class), bc.new_(Object.class)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void lt() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LT", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5.0F < 7.0F;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(5.0F), Const.of(7.0F)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7.0F < 7.0F;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(7.0F), Const.of(7.0F)));
                });
            });
            cc.staticMethod("test3", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of("hello"), Const.of("world")));
                });
            });
            cc.staticMethod("test4", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of("world"), Const.of("world")));
                });
            });
            cc.staticMethod("test5", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of("world"), Const.of("hello")));
                });
            });
            cc.staticMethod("test6", mc -> {
                // static boolean test6() {
                //     return 0 < 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test7", mc -> {
                // static boolean test7() {
                //     return 1 < 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(1), Const.of(0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test5", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test6", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test7", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void le() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5.0 <= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(5.0), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7.0 <= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(7.0), Const.of(7.0)));
                });
            });
            cc.staticMethod("test3", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of("hello"), Const.of("world")));
                });
            });
            cc.staticMethod("test4", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of("world"), Const.of("world")));
                });
            });
            cc.staticMethod("test5", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of("world"), Const.of("hello")));
                });
            });
            cc.staticMethod("test6", mc -> {
                // static boolean test6() {
                //     return 0 <= 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test7", mc -> {
                // static boolean test7() {
                //     return 1 <= 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(1), Const.of(0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test5", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test6", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test7", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void gt() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GT", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 > 7;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(5), Const.of(7)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 > 7;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(7), Const.of(7)));
                });
            });
            cc.staticMethod("test3", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of("hello"), Const.of("world")));
                });
            });
            cc.staticMethod("test4", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of("world"), Const.of("world")));
                });
            });
            cc.staticMethod("test5", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of("world"), Const.of("hello")));
                });
            });
            cc.staticMethod("test6", mc -> {
                // static boolean test6() {
                //     return 0 > 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test7", mc -> {
                // static boolean test7() {
                //     return 1 > 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(1), Const.of(0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test5", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test6", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test7", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void ge() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5.0 >= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(5.0), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7.0 >= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(7.0), Const.of(7.0)));
                });
            });
            cc.staticMethod("test3", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of("hello"), Const.of("world")));
                });
            });
            cc.staticMethod("test4", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of("world"), Const.of("world")));
                });
            });
            cc.staticMethod("test5", mc -> {
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of("world"), Const.of("hello")));
                });
            });
            cc.staticMethod("test6", mc -> {
                // static boolean test6() {
                //     return 0 >= 1;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(0), Const.of(1)));
                });
            });
            cc.staticMethod("test7", mc -> {
                // static boolean test7() {
                //     return 1 >= 0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(1), Const.of(0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test4", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test5", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test6", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test7", BooleanSupplier.class).getAsBoolean());
    }
}
