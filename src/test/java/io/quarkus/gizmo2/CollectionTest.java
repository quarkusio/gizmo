package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;

public class CollectionTest {
    @Test
    public void testCreateList() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CreateList", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `List`
                mc.body(bc -> {
                    bc.return_(bc.listOf(Const.of("foo"), Const.of("bar")));
                });
            });
        });
        assertEquals(List.of("foo", "bar"), tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testCreateListByMapping() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CreateListByMapping", cc -> {
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
        assertEquals(List.of("FOO", "BAR"), tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testCreateSet() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CreateSet", cc -> {
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class); // always `Set`
                mc.body(bc -> {
                    bc.return_(bc.setOf(Const.of("foo"), Const.of("bar")));
                });
            });
        });
        assertEquals(Set.of("foo", "bar"), tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testCreateSetByMapping() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.CreateSetByMapping", cc -> {
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
        assertEquals(Set.of("FOO", "BAR"), tcm.staticMethod("test", Supplier.class).get());
    }
}
