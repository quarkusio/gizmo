package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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

    public interface StringListFun {

        String apply(List<String> list);

    }

}
