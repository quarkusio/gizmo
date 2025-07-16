package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class NewArrayTest {
    @Test
    public void noElements_ignoreObject() {
        // class NoElements {
        //     static void test() {
        //         new int[] {};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NoElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.newArray(int.class);
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod("test", Runnable.class)::run);
    }

    @Test
    public void noElements_returnArray() {
        // class NoElements {
        //     static Object test() {
        //         return new int[] {};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NoElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.newArray(int.class));
                });
            });
        });
        assertArrayEquals(new int[] {}, (int[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void oneElement_ignoreObject() {
        // class OneElement {
        //     static void test() {
        //         new int[] {1};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.OneElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.newArray(int.class, Const.of(1));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod("test", Runnable.class)::run);
    }

    @Test
    public void oneElement_returnArray() {
        // class OneElement {
        //     static Object test() {
        //         return new int[] {1};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.OneElement", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.newArray(int.class, Const.of(1)));
                });
            });
        });
        assertArrayEquals(new int[] { 1 }, (int[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void twoElements_ignoreObject() {
        // class TwoElements {
        //     static void test() {
        //         new int[] {1, 2};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TwoElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.newArray(int.class, Const.of(1), Const.of(2));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod("test", Runnable.class)::run);
    }

    @Test
    public void twoElements_returnArray() {
        // class TwoElements {
        //     static Object test() {
        //         return new int[] {1, 2};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TwoElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.newArray(int.class, Const.of(1), Const.of(2)));
                });
            });
        });
        assertArrayEquals(new int[] { 1, 2 }, (int[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void threeElements_ignoreObject() {
        // class ThreeElements {
        //     static void test() {
        //         new int[] {1, 2, 3};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ThreeElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3));
                    bc.return_();
                });
            });
        });
        assertDoesNotThrow(tcm.staticMethod("test", Runnable.class)::run);
    }

    @Test
    public void threeElements_returnArray() {
        // class ThreeElements {
        //     static Object test() {
        //         return new int[] {1, 2, 3};
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ThreeElements", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.newArray(int.class, Const.of(1), Const.of(2), Const.of(3)));
                });
            });
        });
        assertArrayEquals(new int[] { 1, 2, 3 }, (int[]) tcm.staticMethod("test", Supplier.class).get());
    }
}
