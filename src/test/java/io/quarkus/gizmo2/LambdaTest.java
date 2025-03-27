package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.MethodDesc;

public class LambdaTest {

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
                            lbc.invokeInterface(MethodDesc.of(AtomicInteger.class, "set", void.class, int.class), capturedRet,
                                    suppliedValueInt);
                        });
                    }));
                    bc.invokeInterface(MethodDesc.of(Consumer.class, "accept", void.class, Object.class), consumer,
                            suppliedValue);
                    var retVal = bc.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    bc.return_(retVal);
                });
            });
        });
        assertEquals(1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

}
