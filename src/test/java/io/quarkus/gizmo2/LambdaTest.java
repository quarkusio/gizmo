package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Parameter;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.ValueSource;

import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.testing.TestClassMaker;

@ParameterizedClass
@ValueSource(booleans = { false, true })
public class LambdaTest {
    private static final MethodDesc MD_StringBuilder_append = MethodDesc.of(StringBuilder.class,
            "append", StringBuilder.class, String.class);

    @Parameter
    boolean lambdasAsAnonymousClasses;

    @Test
    public void testSupplierLambda() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.SupplierLambda", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static Object runTest() {
                //    Supplier supplier = () -> "foobar";
                //    return supplier.get();
                // }
                smc.returning(Object.class); // always `String`
                smc.body(b0 -> {
                    Expr supplier = b0.lambda(Supplier.class, lc -> {
                        lc.body(b1 -> {
                            b1.return_("foobar");
                        });
                    });
                    b0.return_(b0.invokeInterface(MethodDesc.of(Supplier.class, "get", Object.class), supplier));
                });
            });
        });
        assertEquals("foobar", tcm.staticMethod(desc, "runTest", Supplier.class).get());
    }

    @Test
    public void testRunnableLambda() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.RunnableLambda", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger();
                //    Runnable runnable = () -> ret.incrementAndGet();
                //    runnable.run();
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.localVar("ret", b0.new_(AtomicInteger.class));
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
        assertEquals(1, tcm.staticMethod(desc, "runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testConsumerLambda() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.ConsumerLambda", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger();
                //    Consumer consumer = val -> ret.set(val);
                //    consumer.accept(10);
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.localVar("ret", b0.new_(AtomicInteger.class));
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
                            b0.box(Const.of(10)));
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        });
        assertEquals(10, tcm.staticMethod(desc, "runTest", IntSupplier.class).getAsInt());
    }

    @Test
    public void testBasicLambdas() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.Lambdas", cc -> {
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
                    var ret = bc.localVar("ret", bc.new_(AtomicInteger.class));
                    var supplier = bc.localVar("supplier", bc.lambda(IntSupplier.class, lc -> {
                        lc.body(lbc -> {
                            lbc.return_(1);
                        });
                    }));
                    var suppliedValue = bc.localVar("suppliedValue",
                            bc.invokeInterface(MethodDesc.of(IntSupplier.class, "getAsInt", int.class), supplier));
                    var consumer = bc.localVar("consumer", bc.lambda(Consumer.class, lc -> {
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
        assertEquals(1, tcm.staticMethod(desc, "test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testLambdaCapturingThis() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.LambdaCapturingThis", cc -> {
            cc.defaultConstructor();

            MethodDesc returnString = cc.method("returnString", mc -> {
                // String returnString() {
                //     return "foobar";
                // }
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_("foobar");
                });
            });

            MethodDesc lambda = cc.method("lambda", mc -> {
                // Supplier<String> lambda() {
                //     String next = "_next";
                //     return () -> this.returnString() + next;
                // }
                mc.returning(Supplier.class);
                mc.body(b0 -> {
                    LocalVar next = b0.localVar("next", Const.of("_next"));
                    b0.return_(b0.lambda(Supplier.class, lc -> {
                        Var this_ = lc.capture("this_", cc.this_());
                        Var next_ = lc.capture(next);
                        lc.body(b1 -> {
                            Expr string = b1.invokeVirtual(returnString, this_);
                            b1.return_(b1.withString(string).concat(next_));
                        });
                    }));
                });
            });

            cc.staticMethod("runTest", smc -> {
                // static Object runTest() {
                //     LambdaCapturingThis instance = new LambdaCapturingThis();
                //     return instance.lambda().get();
                // }
                smc.returning(Object.class); // always `String`
                smc.body(b0 -> {
                    LocalVar instance = b0.localVar("instance", b0.new_(cc.type()));
                    Expr lambdaInstance = b0.invokeVirtual(lambda, instance);
                    Expr result = b0.invokeInterface(MethodDesc.of(Supplier.class, "get", Object.class), lambdaInstance);
                    b0.return_(result);
                });
            });
        });
        assertEquals("foobar_next", tcm.staticMethod(desc, "runTest", Supplier.class).get());
    }

    @Test
    public void testLambdaWithManyParametersAndCaptures() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc lambdaType = g.interface_("io.quarkus.gizmo2.LambdaType", cc -> {
            cc.addAnnotation(FunctionalInterface.class);
            cc.method("get", mc -> {
                mc.returning(String.class);
                mc.parameter("i", int.class);
                mc.parameter("l", long.class);
                mc.parameter("f", float.class);
                mc.parameter("d", double.class);
                mc.parameter("s", String.class);
            });
        });
        MethodDesc lambdaMethod = InterfaceMethodDesc.of(lambdaType, "get", String.class,
                int.class, long.class, float.class, double.class, String.class);
        ClassDesc desc = g.class_("io.quarkus.gizmo2.LambdaWithManyParametersAndCaptures", cc -> {
            cc.staticMethod("runTest", mc -> {
                // static Object runTest() {
                //     int ai = 1;
                //     long al = 2L;
                //     float af = 3.0F;
                //     double ad = 4.0;
                //     String as = "5-6-7";
                //     LambdaWithManyParameters l = (i, l, f, d, s) -> ""
                //         + ai + '_'
                //         + al + '_'
                //         + af + '_'
                //         + ad + '_'
                //         + as + '_'
                //         + i + '_'
                //         + l + '_'
                //         + f + '_'
                //         + d + '_'
                //         + s;
                //     return l.get(8, 9L, 10.0F, 11.0, "12-13-14");
                // }
                mc.returning(Object.class); // always `String`
                mc.body(b0 -> {
                    LocalVar ai = b0.localVar("ai", Const.of(1));
                    LocalVar al = b0.localVar("al", Const.of(2L));
                    LocalVar af = b0.localVar("af", Const.of(3.0F));
                    LocalVar ad = b0.localVar("ad", Const.of(4.0));
                    LocalVar as = b0.localVar("as", Const.of("5-6-7"));
                    Expr lambda = b0.lambda(lambdaMethod, lc -> {
                        Var ai_ = lc.capture(ai);
                        Var al_ = lc.capture(al);
                        Var af_ = lc.capture(af);
                        Var ad_ = lc.capture(ad);
                        Var as_ = lc.capture(as);
                        ParamVar i = lc.parameter("i", 0);
                        ParamVar l = lc.parameter("l", 1);
                        ParamVar f = lc.parameter("f", 2);
                        ParamVar d = lc.parameter("d", 3);
                        ParamVar s = lc.parameter("s", 4);
                        lc.body(b1 -> {
                            LocalVar result = b1.localVar("result", b1.new_(StringBuilder.class));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(ai_));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(al_));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(af_));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(ad_));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(as_));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(i));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(l));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(f));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(d));
                            b1.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                            b1.invokeVirtual(MD_StringBuilder_append, result, b1.exprToString(s));
                            b1.return_(b1.withObject(result).toString_());
                        });
                    });
                    b0.return_(b0.invokeInterface(lambdaMethod, lambda,
                            Const.of(8),
                            Const.of(9L),
                            Const.of(10.0F),
                            Const.of(11.0),
                            Const.of("12-13-14")));
                });
            });
        });
        assertEquals("1_2_3.0_4.0_5-6-7_8_9_10.0_11.0_12-13-14", tcm.staticMethod(desc, "runTest", Supplier.class).get());
    }

    @Test
    public void testConsumerLambdaAssigningToItsParameter() {
        TestClassMaker tcm = TestClassMaker.create(Gizmo.create().withLambdasAsAnonymousClasses(lambdasAsAnonymousClasses));
        Gizmo g = tcm.gizmo();
        ClassDesc desc = g.class_("io.quarkus.gizmo2.ConsumerLambdaAssigningToItsParameter", cc -> {
            cc.staticMethod("runTest", smc -> {
                // static int runTest() {
                //    AtomicInteger ret = new AtomicInteger();
                //    IntConsumer consumer = val -> {
                //        val += 3;
                //        ret.set(val);
                //    }
                //    consumer.accept(10);
                //    return ret.get();
                // }
                smc.returning(int.class);
                smc.body(b0 -> {
                    var ret = b0.localVar("ret", b0.new_(AtomicInteger.class));
                    Expr consumer = b0.lambda(IntConsumer.class, lc -> {
                        var capturedRet = lc.capture(ret);
                        var input = lc.parameter("t", 0);
                        lc.body(b1 -> {
                            b1.inc(input, 3);
                            b1.invokeVirtual(MethodDesc.of(AtomicInteger.class, "set", void.class, int.class),
                                    capturedRet, input);
                            b1.return_();
                        });
                    });
                    b0.invokeInterface(MethodDesc.of(IntConsumer.class, "accept", void.class, int.class),
                            consumer, Const.of(10));
                    var retVal = b0.invokeVirtual(MethodDesc.of(AtomicInteger.class, "get", int.class), ret);
                    b0.return_(retVal);
                });
            });
        });
        assertEquals(13, tcm.staticMethod(desc, "runTest", IntSupplier.class).getAsInt());
    }

}
