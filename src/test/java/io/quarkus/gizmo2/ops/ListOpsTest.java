package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Constant;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.TestClassMaker;
import io.quarkus.gizmo2.creator.ops.ListOps;

public class ListOpsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testListGet() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ListGet", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test(Object t) {
                //    List list = (List)t;
                //    if (list.size() > 1) {
                //       return list.get(list.size() - 1);
                //    }
                //    return list.get(0);
                // }
                ParamVar t = mc.parameter("t", Object.class);
                mc.returning(Object.class);
                mc.body(bc -> {
                    var list = bc.define("list", bc.cast(t, List.class));
                    ListOps listOps = bc.withList(list);
                    var size = bc.define("size", listOps.size());
                    bc.if_(bc.gt(size, 1), gt1 -> {
                        gt1.return_(gt1.withList(list).get(gt1.sub(size, Constant.of(1))));
                    });
                    bc.return_(listOps.get(0));
                });
            });
        });
        assertEquals("bar", tcm.staticMethod("test", Function.class).apply(List.of("foo", "bar")));
        assertEquals("foo", tcm.staticMethod("test", Function.class).apply(List.of("foo")));
    }

}
