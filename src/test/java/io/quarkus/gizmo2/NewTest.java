package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.constant.ClassDesc;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

public class NewTest {
    @Test
    public void noParams_ignoreObject() {
        // class NoParams {
        //     NoParams() {
        //     }
        //
        //     static void test() {
        //         new NoParams();
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.NoParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.new_(ctor);
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod(desc, "test", Runnable.class)::run);
    }

    @Test
    public void noParams_returnObject() {
        // class NoParams {
        //     NoParams() {
        //     }
        //
        //     static Object test() {
        //         return new NoParams();
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.NoParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(ctor));
                });
            });
        });
        assertNotNull(tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void oneParam_ignoreObject() {
        // class OneParam {
        //     OneParam(int param1) {
        //     }
        //
        //     static void test() {
        //         new OneParam(1);
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.OneParam", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.new_(ctor, Const.of(1));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod(desc, "test", Runnable.class)::run);
    }

    @Test
    public void oneParam_returnObject() {
        // class OneParam {
        //     OneParam(int param1) {
        //     }
        //
        //     static Object test() {
        //         return new OneParam(1);
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.OneParam", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(ctor, Const.of(1)));
                });
            });
        });
        assertNotNull(tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void twoParams_ignoreObject() {
        // class TwoParams {
        //     TwoParams(int param1, String param2) {
        //     }
        //
        //     static void test() {
        //         new TwoParams(1, "_");
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TwoParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.parameter("param2", String.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.new_(ctor, Const.of(1), Const.of("_"));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod(desc, "test", Runnable.class)::run);
    }

    @Test
    public void twoParams_returnObject() {
        // class TwoParams {
        //     TwoParams(int param1, String param2) {
        //     }
        //
        //     static Object test() {
        //         return new TwoParams(1, "_");
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.TwoParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.parameter("param2", String.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(ctor, Const.of(1), Const.of("_")));
                });
            });
        });
        assertNotNull(tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void threeParams_ignoreObject() {
        // class ThreeParams {
        //     ThreeParams(int param1, String param2, Double param3) {
        //     }
        //
        //     static void test() {
        //         new ThreeParams(1, "_", new Double(0.0));
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.ThreeParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.parameter("param2", String.class);
                mc.parameter("param3", Double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.new_(ctor, Const.of(1), Const.of("_"), bc.new_(Double.class, Const.of(0.0)));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod(desc, "test", Runnable.class)::run);
    }

    @Test
    public void threeParams_returnObject() {
        // class ThreeParams {
        //     ThreeParams(int param1, String param2, Double param3) {
        //     }
        //
        //     static Object test() {
        //         return new ThreeParams(1, "_", new Double(0.0));
        //     }
        // }

        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.ThreeParams", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("param1", int.class);
                mc.parameter("param2", String.class);
                mc.parameter("param3", Double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(ctor, Const.of(1), Const.of("_"), bc.new_(Double.class, Const.of(0.0))));
                });
            });
        });
        assertNotNull(tcm.staticMethod(desc, "test", Supplier.class).get());
    }
}
