package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;

import io.quarkus.gizmo2.creator.MemberCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
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
                            imc.public_();
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
        ClassDesc baseDesc = g.class_("io.quarkus.gizmo2.Base", cc -> {
            cc.constructor(mc -> {
                mc.public_();
                Var this_ = mc.this_();
                mc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), this_);
                    b0.return_();
                });
            });
            cc.abstractMethod("go", MemberCreator::public_);
        });
        g.class_("io.quarkus.gizmo2.Test", cc -> {
            cc.staticMethod("runTest", smc -> {
                smc.body(b0 -> {
                    Expr go = b0.newAnonymousClass(ConstructorDesc.of(baseDesc), List.of(), acc -> {
                        acc.method("go", imc -> {
                            imc.public_();
                            imc.body(b1 -> {
                                b1.printf("We did it!%n");
                                b1.return_();
                            });
                        });
                    });
                    b0.invokeVirtual(ClassMethodDesc.of(baseDesc, "go", MethodTypeDesc.of(CD_void)), go, List.of());
                    b0.return_();
                });
            });
        });
        tcm.staticMethod("runTest", Runnable.class).run();
    }

}
