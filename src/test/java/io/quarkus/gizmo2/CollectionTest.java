package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.constant.ClassDesc;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

public class CollectionTest {
    @Test
    public void testCreateList() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.CreateList", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `List`
                mc.body(bc -> {
                    bc.return_(bc.listOf(Const.of("foo"), Const.of("bar")));
                });
            });
        });
        assertEquals(List.of("foo", "bar"), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testCreateListByMapping() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.CreateListByMapping", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `List`
                mc.body(bc -> {
                    bc.return_(bc.listOf(List.of("foo", "bar"), it -> {
                        Const value = Const.of(it);
                        return bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class), value);
                    }));
                });
            });
        });
        assertEquals(List.of("FOO", "BAR"), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testCreateSet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.CreateSet", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `Set`
                mc.body(bc -> {
                    bc.return_(
                            bc.setOf(Const.of("foo"),
                                    bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class),
                                            Const.of("bar"))));
                });
            });
        });
        assertEquals(Set.of("foo", "BAR"), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testCreateSetByMapping() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.CreateSetByMapping", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `Set`
                mc.body(bc -> {
                    bc.return_(bc.setOf(List.of("foo", "bar"), it -> {
                        Const value = Const.of(it);
                        return bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class), value);
                    }));
                });
            });
        });
        assertEquals(Set.of("FOO", "BAR"), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfEmpty() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfEmpty", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.mapOf());
                });
            });
        });
        assertEquals(Map.of(), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfSmall() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfSmall", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.mapOf(Const.of("foo"), Const.of(1), Const.of("bar"), Const.of(2)));
                });
            });
        });
        assertEquals(Map.of("foo", 1, "bar", 2), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfLarge() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Map<String, Integer> map = IntStream.rangeClosed(1, 15)
                .boxed()
                .collect(Collectors.toMap(i -> "key" + i, i -> i, (a, b) -> a));
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfLarge", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    List<Expr> items = new ArrayList<Expr>();
                    for (Map.Entry<String, Integer> e : map.entrySet()) {
                        items.add(Const.of(e.getKey()));
                        items.add(Const.of(e.getValue()));
                    }
                    // should not throw
                    bc.mapOf(items);
                    bc.returnNull();
                });
            });
        });
        assertNull(tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfEntriesEmpty() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfEntriesEmpty", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    Expr ret = bc.mapOf(List.<Map.Entry<String, Integer>> of(), Const::of, Const::of);
                    bc.return_(ret);
                });
            });
        });
        assertEquals(Map.of(), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfEntriesSmall() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfEntriesSmall", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                Map<String, Integer> map = Map.of("charlie", 42, "foo", 1, "bar", 10);
                mc.body(bc -> {
                    // should not throw
                    bc.mapOf(List.of(Map.entry(Const.of("foo"), Const.of("bar"))), Function.identity(),
                            Function.identity());
                    Expr ret = bc.mapOf(map.entrySet().stream().toList(), key -> {
                        // key -> key.toUpperCase()
                        return bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class), Const.of(key));
                    }, Const::of);
                    bc.return_(ret);
                });
            });
        });
        assertEquals(Map.of("FOO", 1, "CHARLIE", 42, "BAR", 10), tcm.staticMethod(desc, "test", Supplier.class).get());
    }

    @Test
    public void testMapOfEntriesLarge() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        // 15 entries, exceeds the Map.of() limit of 10
        Map<String, Integer> map = IntStream.rangeClosed(1, 15)
                .boxed()
                .collect(Collectors.toMap(i -> "key" + i, i -> i, (a, b) -> a));
        ClassDesc desc = g.class_("io.quarkus.gizmo2.MapOfEntriesLarge", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> {
                    // should not throw
                    Map<Const, Const> notIllegalAnymore = IntStream.rangeClosed(1, 15)
                            .boxed()
                            .collect(Collectors.toMap(i -> Const.of("foo"), i -> Const.of("bar"), (a, b) -> a));
                    bc.mapOf(notIllegalAnymore.entrySet().stream().toList(), Function.identity(),
                            Function.identity());
                    Expr ret = bc.mapOf(map.entrySet().stream().toList(), key -> {
                        // key -> key.toUpperCase()
                        return bc.invokeVirtual(MethodDesc.of(String.class, "toUpperCase", String.class), Const.of(key));
                    }, val -> {
                        // val -> (val % 2) != 0 ? val * 10 : val
                        var rem = bc.rem(Const.of(val), 2);
                        return bc.cond(int.class, bc.ne(rem, 0),
                                b1 -> b1.yield(b1.mul(Const.of(val), 10)),
                                b1 -> b1.yield(Const.of(val)));
                    });
                    bc.return_(ret);
                });
            });
        });
        var resultMap = IntStream.rangeClosed(1, 15)
                .boxed()
                .collect(Collectors.toMap(i -> ("key" + i).toUpperCase(), i -> i % 2 == 0 ? i : i * 10, (a, b) -> a));
        assertEquals(resultMap, tcm.staticMethod(desc, "test", Supplier.class).get());
    }

}
