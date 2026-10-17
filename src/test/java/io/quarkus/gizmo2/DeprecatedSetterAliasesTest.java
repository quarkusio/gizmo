package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.AccessLevel;
import io.quarkus.gizmo2.testing.TestClassMaker;

/**
 * Checks that the deprecated {@code set*} methods still delegate to their replacements.
 */
@SuppressWarnings("removal")
public class DeprecatedSetterAliasesTest {

    @Test
    public void testDeprecatedSetters() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.DeprecatedSetters", cc -> {
            cc.setVersion(ClassVersion.V17);
            cc.setAccess(AccessLevel.PUBLIC);
            StaticFieldVar number = cc.staticField("number", fc -> {
                fc.setType(int.class);
                fc.setInitial(42);
            });
            StaticFieldVar text = cc.staticField("text", fc -> {
                fc.setType(String.class);
                fc.setInitializer(bc -> bc.yield(Const.of("hello")));
            });
            cc.staticMethod("number", mc -> {
                mc.returning(int.class);
                mc.body(bc -> bc.return_(number));
            });
            cc.staticMethod("text", mc -> {
                mc.returning(Object.class);
                mc.body(bc -> bc.return_(text));
            });
        });
        assertEquals(42, tcm.staticMethod(desc, "number", IntSupplier.class).getAsInt());
        assertEquals("hello", tcm.staticMethod(desc, "text", Supplier.class).get());
    }
}
