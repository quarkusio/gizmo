package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;

public class LambdaTest {

    @Test
    public void testRunnableLambda() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.RunnableLambda", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger(1);
                //    Runnable runnable = () -> ret.incrementAndGet();
                //    runnable.run();
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.define("ret", b0.new_(AtomicInteger.class));
                    Expr runnable = b0.lambda(Runnable.class, lc -> {
                        var capturedRet = lc.capture(ret);
                        lc.body(b1 -> {
                            b1.invokeVirtual(MethodDesc.of(AtomicInteger.class, "incrementAndGet", int.class), capturedRet);
                            b1.return_();
                        });
                    });
                    b0.invokeInterface(MethodDesc.of(Runnable.class, "run", void.class), runnable);
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        });
        assertEquals(1, tcm.staticMethod("runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testConsumerLambda() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ConsumerLambda", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger(1);
                //    Consumer consumer = val -> ret.set(val);
                //    consumer.accept(10);
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.define("ret", b0.new_(AtomicInteger.class));
                    Expr consumer = b0.lambda(Consumer.class, lc -> {
                        var capturedRet = lc.capture(ret);
                        var input = lc.parameter("t", 0);
                        lc.body(b1 -> {
                            var unboxedInput = b1.unbox(b1.cast(input, Integer.class));
                            b1.invokeVirtual(MethodDesc.of(AtomicInteger.class, "set", void.class, int.class), capturedRet,
                                    unboxedInput);
                            b1.return_();
                        });
                    });
                    b0.invokeInterface(MethodDesc.of(Consumer.class, "accept", void.class, Object.class), consumer,
                            b0.box(Constant.of(10)));
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        });
        assertEquals(10, tcm.staticMethod("runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testBasicLambdas() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Lambdas", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //    AtomicInteger ret = new AtomicInteger();
                //    IntSupplier supplier = () -> 1;
                //    int suppliedValue = supplier.getAsInt();
                //    Consumer<Integer> consumer = i -> ret.set(i);
                //    consumer.accept(suppliedValue);
                //    return ret.get();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var ret = bc.define("ret", bc.new_(AtomicInteger.class));
                    var supplier = bc.declare("supplier", IntSupplier.class);
                    bc.set(supplier, bc.lambda(IntSupplier.class, lc -> {
                        lc.body(lbc -> {
                            lbc.return_(1);
                        });
                    }));
                    var suppliedValue = bc.define("suppliedValue",
                            bc.invokeInterface(MethodDesc.of(IntSupplier.class, "getAsInt", int.class), supplier));
                    var consumer = bc.declare("consumer", Consumer.class);
                    bc.set(consumer, bc.lambda(Consumer.class, lc -> {
                        var capturedRet = lc.capture(ret);
                        var input = lc.parameter("t", 0);
                        lc.body(lbc -> {
                            var suppliedValueInteger = lbc.cast(input, Integer.class);
                            var suppliedValueInt = lbc.unbox(suppliedValueInteger);
                            lbc.invokeVirtual(MethodDesc.of(AtomicInteger.class, "set", void.class, int.class), capturedRet,
                                    suppliedValueInt);
                            lbc.return_();
                        });
                    }));
                    var boxedSuppliedValue = bc.box(suppliedValue);
                    bc.invokeInterface(MethodDesc.of(Consumer.class, "accept", void.class, Object.class), consumer,
                            boxedSuppliedValue);
                    var retVal = bc.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    bc.return_(retVal);
                });
            });
        });
        assertEquals(1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

}
