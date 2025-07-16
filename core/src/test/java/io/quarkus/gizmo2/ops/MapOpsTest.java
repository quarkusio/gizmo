package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.TestClassMaker;
import io.quarkus.gizmo2.creator.ops.MapOps;

public class MapOpsTest {

    @Test
    public void testOps() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.MapOps", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //    Map map = new HashMap();
                //    map.put("foo", "bar");
                //    map.put("alpha", "bravo");
                //    if (map.size() != 2) {
                //       return 1;
                //    }
                //    if (map.isEmpty()) {
                //       return 2;
                //    }
                //    if (!map.containsKey("foo")) {
                //       return 3;
                //    }
                //    if (!map.get("foo").equals("bar")) {
                //       return 4;
                //    }
                //    if (!map.remove("alpha").equals("bravo")) {
                //       return 5;
                //    }
                //    if (map.size() != 1) {
                //       return 6;
                //    }
                //    map.clear();
                //    if (!map.isEmpty()) {
                //       return 7;
                //    }
                //    return 0;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var map = bc.localVar("map", bc.new_(HashMap.class));
                    MapOps mapOps = bc.withMap(map);
                    mapOps.put(Const.of("foo"), Const.of("bar"));
                    mapOps.put(Const.of("alpha"), Const.of("bravo"));
                    var size = mapOps.size();
                    bc.if_(bc.ne(size, 2), fail -> fail.return_(1));
                    bc.if_(mapOps.isEmpty(), fail -> fail.return_(2));
                    bc.ifNot(mapOps.containsKey(Const.of("foo")), fail -> fail.return_(3));
                    bc.ifNot(bc.objEquals(mapOps.get(Const.of("foo")), Const.of("bar")),
                            fail -> fail.return_(4));
                    bc.ifNot(bc.objEquals(mapOps.remove(Const.of("alpha")), Const.of("bravo")),
                            fail -> fail.return_(5));
                    bc.if_(bc.ne(mapOps.size(), 1), fail -> fail.return_(6));
                    mapOps.clear();
                    bc.ifNot(mapOps.isEmpty(), fail -> fail.return_(7));
                    bc.return_(0);
                });
            });
        });
        assertEquals(0, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testMapOf() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.MapOf", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //    Map map = Map.of("foo","bar",);
                //    if (!map.get("foo").equals("bar")) {
                //       return 4;
                //    }
                //    return map.size();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    assertThrows(IllegalArgumentException.class, () -> bc.mapOf(Const.of("foo")));
                    var map = bc.localVar("map", bc.mapOf(Const.of("foo"), Const.of("bar")));
                    MapOps mapOps = bc.withMap(map);
                    bc.ifNot(bc.objEquals(mapOps.get(Const.of("foo")), Const.of("bar")),
                            fail -> fail.return_(-1));
                    bc.return_(mapOps.size());
                });
            });
        });
        assertEquals(1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

}
