package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

public class OptionalOpsTest {

    @Test
    public void testOps() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.OptionalOps", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //    Optional foo = Optional.of("foo");
                //    Optional bar = Optional.ofNullable("bar");
                //    Optional baz = Optional.ofNullable(null);
                //    if (foo.isEmpty()) {
                //       return 1;
                //    }
                //    if (!foo.isPresent()) {
                //       return 2;
                //    }
                //    if (!foo.get().equals("foo")) {
                //       return 3;
                //    }
                //    if (bar.isEmpty()) {
                //       return 4;
                //    }
                //    if (baz.isPresent()) {
                //       return 5;
                //    }
                //    if (!baz.orElse("qux").equals("qux")) {
                //       return 5;
                //    }
                //    return 0;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var foo = bc.define("foo", bc.optionalOf(Constant.of("foo")));
                    var bar = bc.define("bar", bc.optionalOfNullable(Constant.of("bar")));
                    var baz = bc.define("baz", bc.optionalOfNullable(Constant.ofNull(String.class)));
                    bc.if_(bc.withOptional(foo).isEmpty(), fail -> fail.return_(1));
                    bc.unless(bc.withOptional(foo).isPresent(), fail -> fail.return_(2));
                    bc.unless(bc.exprEquals(Constant.of("foo"), bc.withOptional(foo).get()), fail -> fail.return_(3));
                    bc.if_(bc.withOptional(bar).isEmpty(), fail -> fail.return_(4));
                    bc.if_(bc.withOptional(baz).isPresent(), fail -> fail.return_(5));
                    var qux = Constant.of("qux");
                    bc.unless(bc.exprEquals(qux, bc.withOptional(baz).orElse(qux)), fail -> fail.return_(6));
                    bc.return_(0);
                });
            });
        });
        assertEquals(0, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

}
