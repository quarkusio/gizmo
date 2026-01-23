package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.ops.ListOps;
import io.quarkus.gizmo2.testing.TestClassMaker;

public class ListOpsTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testListGet() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.ListGet", cc -> {
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
                    var list = bc.localVar("list", bc.cast(t, List.class));
                    ListOps listOps = bc.withList(list);
                    var size = bc.localVar("size", listOps.size());
                    bc.if_(bc.gt(size, 1), gt1 -> {
                        gt1.return_(gt1.withList(list).get(gt1.sub(size, Const.of(1))));
                    });
                    bc.return_(listOps.get(0));
                });
            });
        });
        assertEquals("bar", tcm.staticMethod(desc, "test", Function.class).apply(List.of("foo", "bar")));
        assertEquals("foo", tcm.staticMethod(desc, "test", Function.class).apply(List.of("foo")));
    }

}
