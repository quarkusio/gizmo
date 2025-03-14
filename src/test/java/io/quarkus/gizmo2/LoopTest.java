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

    @Test
    public void testDoWhileContinue() {
        // int i = 0;
        // do {
        //     if (i % 2 == 0) {
        //         i++;
        //         continue;
        //     }
        //     System.out.printf("Even number: ", Integer.valueOf(i));
        // } while (i <= 10);
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LoopFun", cc -> {
            cc.staticMethod("test", mc -> {
                mc.body(bc -> {
                    LocalVar i = bc.define("i", Constant.of(0));
                    bc.doWhile(bc1 -> {
                        bc1.if_(bc1.eq(bc1.rem(i, 2), Constant.of(0)), bc2 -> {
                            bc2.inc(i);
                            bc2.continue_(bc1);
                        });
                        bc1.printf("Even number: ", bc1.box(i));
                        bc1.inc(i);
                    }, bc1 -> bc1.le(i, 10));
                    bc.return_();
                });
            });
        });
    }

    public interface StringListFun {

        String apply(List<String> list);

    }

}
