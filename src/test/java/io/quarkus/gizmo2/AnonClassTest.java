package io.quarkus.gizmo2;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.desc.MethodDesc;
import org.junit.jupiter.api.Test;

/**
 * Tests for anonymous classes and related behaviors.
 */
public final class AnonClassTest {

    @Test
    public void testNoArgs() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Outer", cc -> {
            cc.staticMethod("runTest", smc -> {
                smc.body(b0 -> {
                    b0.printf("Starting test%n");
                    Expr runnable = b0.newAnonymousClass(Runnable.class, acc -> {
                        acc.method("run", imc -> {
                            imc.withFlag(AccessFlag.PUBLIC);
                            imc.body(b1 -> {
                                b1.printf("Inside the runnable%n");
                                b1.return_();
                            });
                        });
                    });
                    b0.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), runnable);
                    b0.printf("After runnable%n");
                    b0.return_();
                });
            });
        });
        tcm.staticMethod("runTest", Runnable.class).run();
    }

    @Test
    public void testExtendClass() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Outer", cc -> {

        });
    }
}
