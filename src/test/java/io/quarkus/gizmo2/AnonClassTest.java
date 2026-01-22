package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.CD_void;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.creator.MemberCreator;
import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

/**
 * Tests for anonymous classes and related behaviors.
 */
public final class AnonClassTest {

    @Test
    public void testNoArgs() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Outer", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger();
                //    Runnable runnable = new Runnable() {
                //       public void run() {
                //          ret.incrementAndGet();
                //       }
                //    }
                //    runnable.run();
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.localVar("ret", b0.new_(AtomicInteger.class));
                    Expr runnable = b0.newAnonymousClass(Runnable.class, acc -> {
                        var capturedRet = acc.capture(ret);
                        acc.method("run", imc -> {
                            imc.public_();
                            imc.body(b1 -> {
                                b1.invokeVirtual(MethodDesc.of(AtomicInteger.class, "incrementAndGet", int.class), capturedRet);
                                b1.return_();
                            });
                        });
                    });
                    b0.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), runnable);
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        }));
        assertEquals(1, tcm.staticMethod(clazz, "runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testExtendClass() {
        TestClassMaker tcm = TestClassMaker.create();
        Gizmo g = tcm.gizmo();
        ClassDesc baseDesc = g.class_("io.quarkus.gizmo2.Base", cc -> {
            cc.constructor(mc -> {
                mc.public_();
                mc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    b0.return_();
                });
            });
            cc.abstractMethod("go", MemberCreator::public_);
        });
        Class<?> clazz = tcm.loadClass(g.class_("io.quarkus.gizmo2.Test", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger();
                //    Base base = new Base() {
                //       public void go() {
                //          ret.incrementAndGet();
                //       }
                //    }
                //    base.go();
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.localVar("ret", b0.new_(AtomicInteger.class));
                    Expr base = b0.newAnonymousClass(ConstructorDesc.of(baseDesc), List.of(), acc -> {
                        var capturedRet = acc.capture(ret);
                        acc.method("go", imc -> {
                            imc.public_();
                            imc.body(b1 -> {
                                b1.printf("We did it!%n");
                                b1.invokeVirtual(MethodDesc.of(AtomicInteger.class, "incrementAndGet", int.class), capturedRet);
                                b1.return_();
                            });
                        });
                    });
                    b0.invokeVirtual(ClassMethodDesc.of(baseDesc, "go", MethodTypeDesc.of(CD_void)), base, List.of());
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        }));
        assertEquals(1, tcm.staticMethod(clazz, "runTest", IntSupplier.class).getAsInt());
    }

}
