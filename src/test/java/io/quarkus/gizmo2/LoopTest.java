package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;

public class LoopTest {

    @Test
    public void testForEach() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LoopFun", cc -> {
            cc.implements_(StringListFun.class);
            cc.defaultConstructor();
            cc.method("apply", mc -> {
                ParamVar p = mc.parameter("list", List.class);
                mc.returning(String.class);
                mc.public_();
                mc.body(bc -> {
                    // for(String e : list) {
                    // return e;
                    // }
                    // return null;
                    bc.forEach(p, (loop, item) -> {
                        loop.return_(loop.cast(item, String.class));
                    });
                    bc.returnNull();
                });
            });
        });
        assertEquals("foo", tcm.noArgsConstructor(StringListFun.class).apply(List.of("foo", "bar", "baz")));
    }

    @Test
    public void testForEachBreak() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LoopFun", cc -> {
            cc.implements_(StringListFun.class);
            cc.defaultConstructor();
            cc.method("apply", mc -> {
                ParamVar p = mc.parameter("t", List.class);
                mc.returning(String.class);
                mc.public_();
                mc.body(bc -> {
                    // StringBuilder ret = new StringBuilder();
                    var ret = bc.define("ret", bc.new_(StringBuilder.class));
                    bc.forEach(p, (loop, item) -> {
                        // ret.append(item);
                        MethodDesc append = MethodDesc.of(StringBuilder.class, "append", StringBuilder.class, String.class);
                        loop.invokeVirtual(append, ret, loop.cast(item, String.class));
                        // if(item.equals("bar")) break;
                        loop.if_(loop.exprEquals(item, Constant.of("bar")), isEqual -> {
                            isEqual.break_(loop);
                        });
                    });
                    bc.return_(bc.withObject(ret).objToString());
                });
            });
        });
        assertEquals("foobar", tcm.noArgsConstructor(StringListFun.class).apply(List.of("foo", "bar", "baz")));
    }

    @Test
    public void testForEachContinue() {
        // StringBuilder builder = new StringBuilder();
        // for(String e : list) {
        //    if (e.equals("bar")) {
        //       continue;
        //    }
        //    builder.append(e);
        // }
        // return builder.toString();
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LoopFun", cc -> {
            cc.implements_(StringListFun.class);
            cc.defaultConstructor();
            cc.method("apply", mc -> {
                ParamVar p = mc.parameter("t", List.class);
                mc.returning(String.class);
                mc.public_();
                mc.body(bc -> {
                    var ret = bc.define("ret", bc.new_(StringBuilder.class));
                    bc.forEach(p, (loop, item) -> {
                        loop.if_(loop.exprEquals(item, Constant.of("bar")), isEqual -> {
                            isEqual.continue_(loop);
                        });
                        MethodDesc append = MethodDesc.of(StringBuilder.class, "append", StringBuilder.class, String.class);
                        loop.invokeVirtual(append, ret, loop.cast(item, String.class));
                    });
                    bc.return_(bc.withObject(ret).objToString());
                });
            });
        });
        assertEquals("foobaz", tcm.noArgsConstructor(StringListFun.class).apply(List.of("foo", "bar", "baz")));
    }

    @Test
    public void testDoWhile() {
        // int i = 0;
        // int sum = 0;
        // do {
        //     i++;
        //     sum += i;
        // } while (i < 10);
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.DoWhileSupplier", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    var i = bc.define("i", Constant.of(0));
                    var sum = bc.define("sum", Constant.of(0));
                    bc.doWhile(loop -> {
                        loop.inc(i);
                        loop.set(sum, loop.add(sum, i));
                    }, cond -> cond.yield(cond.lt(i, 10)));
                    bc.return_(bc.box(sum));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testDoWhileContinue() {
        // int i = 0;
        // int sum = 0;
        // do {
        //     i++;
        //     if (i % 2 == 0) {
        //         continue;
        //     }
        //     sum += i;
        // } while (i < 10);
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.DoWhileSupplier", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    var i = bc.define("i", Constant.of(0));
                    var sum = bc.define("sum", Constant.of(0));
                    bc.doWhile(loop -> {
                        loop.inc(i);
                        loop.if_(loop.eq(loop.rem(i, 2), Constant.of(0)), isEven -> {
                            isEven.continue_(loop);
                        });
                        loop.set(sum, loop.add(sum, i));
                    }, cond -> cond.yield(cond.lt(i, 10)));
                    bc.return_(bc.box(sum));
                });
            });
        });
        // 1 + 3 + 5 + 7 + 9
        assertEquals(25, tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testWhile() {
        // int i = 0;
        // int sum = 0;
        // while (i < 10) {
        //     i++;
        //     sum += i;
        // }
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.WhileSupplier", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    var i = bc.define("i", Constant.of(0));
                    var sum = bc.define("sum", Constant.of(0));
                    bc.while_(cond -> cond.yield(cond.lt(i, 10)), loop -> {
                        loop.inc(i);
                        loop.set(sum, loop.add(sum, i));
                    });
                    bc.return_(bc.box(sum));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testWhileContinue() {
        // int i = 0;
        // int sum = 0;
        // while (i < 10) {
        //     i++;
        //     if (i % 2 == 0) {
        //         continue;
        //     }
        //     sum += i;
        // }
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.WhileSupplier", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    var i = bc.define("i", Constant.of(0));
                    var sum = bc.define("sum", Constant.of(0));
                    bc.while_(cond -> cond.yield(cond.lt(i, 10)), loop -> {
                        loop.inc(i);
                        loop.if_(loop.eq(loop.rem(i, 2), Constant.of(0)), isEven -> {
                            isEven.continue_(loop);
                        });
                        loop.set(sum, loop.add(sum, i));
                    });
                    bc.return_(bc.box(sum));
                });
            });
        });
        // 1 + 3 + 5 + 7 + 9
        assertEquals(25, tcm.staticMethod("test", Supplier.class).get());
    }

    public interface StringListFun {

        String apply(List<String> list);
        
    }

}
