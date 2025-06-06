package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.TestClassMaker;

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
                    var foo = bc.localVar("foo", bc.optionalOf(Const.of("foo")));
                    var bar = bc.localVar("bar", bc.optionalOfNullable(Const.of("bar")));
                    var baz = bc.localVar("baz", bc.optionalOfNullable(Const.ofNull(String.class)));
                    bc.if_(bc.withOptional(foo).isEmpty(), fail -> fail.return_(1));
                    bc.ifNot(bc.withOptional(foo).isPresent(), fail -> fail.return_(2));
                    bc.ifNot(bc.objEquals(Const.of("foo"), bc.withOptional(foo).get()), fail -> fail.return_(3));
                    bc.if_(bc.withOptional(bar).isEmpty(), fail -> fail.return_(4));
                    bc.if_(bc.withOptional(baz).isPresent(), fail -> fail.return_(5));
                    var qux = Const.of("qux");
                    bc.ifNot(bc.objEquals(qux, bc.withOptional(baz).orElse(qux)), fail -> fail.return_(6));
                    bc.return_(0);
                });
            });
        });
        assertEquals(0, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

}
