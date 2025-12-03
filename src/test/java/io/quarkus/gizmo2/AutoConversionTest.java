package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public class AutoConversionTest {
    private static final MethodDesc MD_StringBuilder_append = MethodDesc.of(StringBuilder.class,
            "append", StringBuilder.class, String.class);

    @FunctionalInterface
    public interface IntegerIntToIntFunction {
        int apply(Integer a, int b);
    }

    @FunctionalInterface
    public interface LongDoubleToDoubleFunction {
        double apply(long a, double b);
    }

    @FunctionalInterface
    public interface BooleanToIntFunction {
        int apply(boolean b);
    }

    @Test
    public void invoke_boxUnboxArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            MethodDesc method = cc.staticMethod("method", mc -> {
                // static String method(Byte a, int b, Long c, double d) {
                //     return a + "_" + b + "_" + c + "_" + d;
                // }
                ParamVar a = mc.parameter("a", Byte.class);
                ParamVar b = mc.parameter("b", int.class);
                ParamVar c = mc.parameter("c", Long.class);
                ParamVar d = mc.parameter("d", double.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(a));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(b));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(c));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(d));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return method((byte) 1, new Integer(13), 42L, new Double(5.0));
                // }
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    // box a, unbox b, box c, unbox d
                    bc.return_(bc.invokeStatic(method, Const.of((byte) 1), bc.new_(Integer.class, Const.of(13)),
                            Const.of(42L), bc.new_(Double.class, Const.of(5.0))));
                });
            });
        });
        assertEquals("1_13_42_5.0", tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void invoke_widenArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            MethodDesc method = cc.staticMethod("method", mc -> {
                // static String method(long a, double b) {
                //     return a + "_" + b;
                // }
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(a));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(b));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return method(42, 13.0F);
                // }
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    // widen a, widen b
                    bc.return_(bc.invokeStatic(method, Const.of(42), Const.of(13.0F)));
                });
            });
        });
        assertEquals("42_13.0", tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void invoke_unboxAndWidenArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            MethodDesc method = cc.staticMethod("method", mc -> {
                // static String method(long a, double b) {
                //     return a + "_" + b;
                // }
                ParamVar a = mc.parameter("a", long.class);
                ParamVar b = mc.parameter("b", double.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(a));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(b));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return method(new Integer(13), new Float(42.0F));
                // }
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    // unbox+widen a, unbox+widen b
                    bc.return_(bc.invokeStatic(method, bc.new_(Integer.class, Const.of(13)),
                            bc.new_(Float.class, Const.of(42.0F))));
                });
            });
        });
        assertEquals("13_42.0", tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void invoke_widenAndBoxArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            MethodDesc method = cc.staticMethod("method", mc -> {
                // static String method(Long a, Double b) {
                //     return a + "_" + b;
                // }
                ParamVar a = mc.parameter("a", Long.class);
                ParamVar b = mc.parameter("b", Double.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(a));
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_"));
                    bc.invokeVirtual(MD_StringBuilder_append, result, bc.exprToString(b));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return method(13, 42.0F);
                // }
                mc.returning(Object.class); // always `String`
                mc.body(bc -> {
                    // widen+box a, widen+box b
                    bc.return_(bc.invokeStatic(method, Const.of(13), Const.of(42.0F)));
                });
            });
        });
        assertEquals("13_42.0", tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void invoke_boxInstance() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return 1.longValue();
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    // box target instance
                    bc.return_(bc.invokeVirtual(MethodDesc.of(Integer.class, "longValue", long.class), Const.of(42)));
                });
            });
        });
        assertEquals(42L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void invoke_widenAndBoxInstance() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Invoke", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return 1.longValue();
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    // widen and box target instance
                    // there's no better method to use, but manual bytecode inspection shows this is compiled correctly
                    bc.return_(bc.invokeVirtual(MethodDesc.of(Long.class, "longValue", long.class), Const.of(42)));
                });
            });
        });
        assertEquals(42L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void new_boxUnboxArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.New", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("a", Byte.class);
                mc.parameter("b", int.class);
                mc.parameter("c", Long.class);
                mc.parameter("d", double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return new New((byte) 1, new Integer(13), 42L, new Double(5.0));
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    // box a, unbox b, box c, unbox d
                    bc.return_(bc.new_(ctor, Const.of((byte) 1), bc.new_(Integer.class, Const.of(13)),
                            Const.of(42L), bc.new_(Double.class, Const.of(5.0))));
                });
            });
        });
        assertNotNull(tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void new_widenArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.New", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("a", long.class);
                mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return new New(42, 13.0F);
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    // widen a, widen b
                    bc.return_(bc.new_(ctor, Const.of(42), Const.of(13.0F)));
                });
            });
        });
        assertNotNull(tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void new_unboxAndWidenArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.New", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("a", long.class);
                mc.parameter("b", double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return new New(new Integer(42), new Float(13.0F));
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    // unbox+widen a, unbox+widen b
                    bc.return_(bc.new_(ctor, bc.new_(Integer.class, Const.of(42)),
                            bc.new_(Float.class, Const.of(13.0F))));
                });
            });
        });
        assertNotNull(tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void new_widenAndBoxArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.New", cc -> {
            ConstructorDesc ctor = cc.constructor(mc -> {
                mc.parameter("a", Long.class);
                mc.parameter("b", Double.class);
                mc.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });

            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return new New(42, 13.0F);
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    // widen+box a, widen+box b
                    bc.return_(bc.new_(ctor, Const.of(42), Const.of(13.0F)));
                });
            });
        });
        assertNotNull(tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void localVarSet_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LocalVarSet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     Integer local1 = 13;
                //     int local2 = new Integer(42);
                //     return local1 + local2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar local1 = bc.localVar("local1", Integer.class, Const.ofDefault(Integer.class));
                    bc.set(local1, Const.of(13));

                    LocalVar local2 = bc.localVar("local2", int.class, Const.ofDefault(int.class));
                    bc.set(local2, bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(local1, local2));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void localVarSet_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LocalVarSet", cc -> {
            cc.staticMethod("test", mc -> {
                // static double test() {
                //     long local1 = 13;
                //     double local2 = 42.0F;
                //     return local1 + local2;
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar local1 = bc.localVar("local1", long.class, Const.ofDefault(long.class));
                    bc.set(local1, Const.of(13));

                    LocalVar local2 = bc.localVar("local2", double.class, Const.ofDefault(double.class));
                    bc.set(local2, Const.of(42.0F));

                    bc.return_(bc.add(local1, local2));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void staticFieldVarSet_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticFieldVarSet", cc -> {
            StaticFieldVar field1 = cc.staticField("field1", fc -> {
                fc.setType(Integer.class);
            });
            StaticFieldVar field2 = cc.staticField("field2", fc -> {
                fc.setType(int.class);
            });

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     field1 = 13;
                //     field2 = new Integer(42);
                //     return field1 + field2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.set(field1, Const.of(13));
                    bc.set(field2, bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(field1, field2));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void staticFieldVarSet_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticFieldVarSet", cc -> {
            StaticFieldVar field1 = cc.staticField("field1", fc -> {
                fc.setType(long.class);
            });
            StaticFieldVar field2 = cc.staticField("field2", fc -> {
                fc.setType(double.class);
            });

            cc.staticMethod("test", mc -> {
                // static double test() {
                //     field1 = 13;
                //     field2 = 42.0F;
                //     return field1 + field2;
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    bc.set(field1, Const.of(13));
                    bc.set(field2, Const.of(42.0F));

                    bc.return_(bc.add(field1, field2));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void staticFieldVarSetViaHandle_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticFieldVarSet", cc -> {
            StaticFieldVar field1 = cc.staticField("field1", fc -> {
                fc.setType(Integer.class);
            });
            StaticFieldVar field2 = cc.staticField("field2", fc -> {
                fc.setType(int.class);
            });

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     field1 = 13; // <volatile>
                //     field2 = new Integer(42); // <volatile>
                //     return field1 + field2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.set(field1, Const.of(13), MemoryOrder.Volatile);
                    bc.set(field2, bc.new_(Integer.class, Const.of(42)), MemoryOrder.Volatile);

                    bc.return_(bc.add(field1, field2));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void staticFieldVarSetViaHandle_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticFieldVarSet", cc -> {
            StaticFieldVar field1 = cc.staticField("field1", fc -> {
                fc.setType(long.class);
            });
            StaticFieldVar field2 = cc.staticField("field2", fc -> {
                fc.setType(double.class);
            });

            cc.staticMethod("test", mc -> {
                // static double test() {
                //     field1 = 13; // <volatile>
                //     field2 = 42.0F; // <volatile>
                //     return field1 + field2;
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    bc.set(field1, Const.of(13), MemoryOrder.Volatile);
                    bc.set(field2, Const.of(42.0F), MemoryOrder.Volatile);

                    bc.return_(bc.add(field1, field2));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void fieldVarSet_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FieldVarSet", cc -> {
            FieldDesc field1 = cc.field("field1", fc -> {
                fc.setType(Integer.class);
            });
            FieldDesc field2 = cc.field("field2", fc -> {
                fc.setType(int.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     FieldVarSet instance = new FieldVarSet();
                //     instance.field1 = 13;
                //     instance.field2 = new Integer(42);
                //     return instance.field1 + instance.field2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field1), Const.of(13));
                    bc.set(instance.field(field2), bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(instance.field(field1), instance.field(field2)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void fieldVarSet_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FieldVarSet", cc -> {
            FieldDesc field1 = cc.field("field1", fc -> {
                fc.setType(long.class);
            });
            FieldDesc field2 = cc.field("field2", fc -> {
                fc.setType(double.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     FieldVarSet instance = new FieldVarSet();
                //     instance.field1 = 13;
                //     instance.field2 = 42.0F;
                //     return instance.field1 + instance.field2;
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field1), Const.of(13));
                    bc.set(instance.field(field2), Const.of(42.0F));

                    bc.return_(bc.add(instance.field(field1), instance.field(field2)));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void fieldVarSetViaHandle_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FieldVarSet", cc -> {
            FieldDesc field1 = cc.field("field1", fc -> {
                fc.setType(Integer.class);
            });
            FieldDesc field2 = cc.field("field2", fc -> {
                fc.setType(int.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     FieldVarSet instance = new FieldVarSet();
                //     instance.field1 = 13; // <volatile>
                //     instance.field2 = new Integer(42); // <volatile>
                //     return instance.field1 + instance.field2;
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field1), Const.of(13), MemoryOrder.Volatile);
                    bc.set(instance.field(field2), bc.new_(Integer.class, Const.of(42)), MemoryOrder.Volatile);

                    bc.return_(bc.add(instance.field(field1), instance.field(field2)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void fieldVarSetViaHandle_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.FieldVarSet", cc -> {
            FieldDesc field1 = cc.field("field1", fc -> {
                fc.setType(long.class);
            });
            FieldDesc field2 = cc.field("field2", fc -> {
                fc.setType(double.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                // static int test() {
                //     FieldVarSet instance = new FieldVarSet();
                //     instance.field1 = 13; // <volatile>
                //     instance.field2 = 42.0F; // <volatile>
                //     return instance.field1 + instance.field2;
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field1), Const.of(13), MemoryOrder.Volatile);
                    bc.set(instance.field(field2), Const.of(42.0F), MemoryOrder.Volatile);

                    bc.return_(bc.add(instance.field(field1), instance.field(field2)));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void paramVarSet_boxUnbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ParamVarSet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(Integer param1, int param2) {
                //     param1 = 13;
                //     param2 = new Integer(42);
                //     return param1 + param2;
                // }
                ParamVar param1 = mc.parameter("param1", Integer.class);
                ParamVar param2 = mc.parameter("param2", int.class);
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.set(param1, Const.of(13));
                    bc.set(param2, bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(param1, param2));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntegerIntToIntFunction.class).apply(null, 0));
    }

    @Test
    public void paramVarSet_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ParamVarSet", cc -> {
            cc.staticMethod("test", mc -> {
                // static double test(long param1, double param2) {
                //     param1 = 13;
                //     param2 = 42.0F;
                //     return param1 + param2;
                // }
                ParamVar param1 = mc.parameter("param1", long.class);
                ParamVar param2 = mc.parameter("param2", double.class);
                mc.returning(double.class);
                mc.body(bc -> {
                    bc.set(param1, Const.of(13));
                    bc.set(param2, Const.of(42.0F));

                    bc.return_(bc.add(param1, param2));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", LongDoubleToDoubleFunction.class).apply(0L, 0.0));
    }

    @Test
    public void arraySet_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     int[] array = new int[2];
                //     array[0] = 13;
                //     array[1] = new Integer(42);
                //     return array[0] + array[1];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(int.class, 2));
                    bc.set(array.elem(0), Const.of(13));
                    bc.set(array.elem(1), bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void arraySet_box() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     Integer[] array = new Integer[2];
                //     array[0] = 13;
                //     array[1] = new Integer(42);
                //     return array[0] + array[1];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(Integer.class, 2));
                    bc.set(array.elem(0), Const.of(13));
                    bc.set(array.elem(1), bc.new_(Integer.class, Const.of(42)));

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void arraySet_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static double test() {
                //     double[] array = new double[2];
                //     array[0] = 13;
                //     array[1] = 42L;
                //     return array[0] + array[1];
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(double.class, 2));
                    bc.set(array.elem(0), Const.of(13));
                    bc.set(array.elem(1), Const.of(42L));

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void arraySetViaHandle_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     int[] array = new int[2];
                //     array[0] = 13; // <volatile>
                //     array[1] = new Integer(42); // <volatile>
                //     return array[0] + array[1];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(int.class, 2));
                    bc.set(array.elem(0), Const.of(13), MemoryOrder.Volatile);
                    bc.set(array.elem(1), bc.new_(Integer.class, Const.of(42)), MemoryOrder.Volatile);

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void arraySetViaHandle_box() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     Integer[] array = new Integer[2];
                //     array[0] = 13; // <volatile>
                //     array[1] = new Integer(42); // <volatile>
                //     return array[0] + array[1];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(Integer.class, 2));
                    bc.set(array.elem(0), Const.of(13), MemoryOrder.Volatile);
                    bc.set(array.elem(1), bc.new_(Integer.class, Const.of(42)), MemoryOrder.Volatile);

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void arraySetViaHandle_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArraySet", cc -> {
            cc.staticMethod("test", mc -> {
                // static double test() {
                //     double[] array = new double[2];
                //     array[0] = 13; // <volatile>
                //     array[1] = 42L; // <volatile>
                //     return array[0] + array[1];
                // }
                mc.returning(double.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array", bc.newEmptyArray(double.class, 2));
                    bc.set(array.elem(0), Const.of(13), MemoryOrder.Volatile);
                    bc.set(array.elem(1), Const.of(42L), MemoryOrder.Volatile);

                    bc.return_(bc.add(array.elem(0), array.elem(1)));
                });
            });
        });
        assertEquals(55.0, tcm.staticMethod("test", DoubleSupplier.class).getAsDouble());
    }

    @Test
    public void arrayIndex_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArrayIndex", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     int[] array = new int[] {13, 42};
                //     return array[new Integer(0)] + array[new Integer(1)];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array",
                            bc.newArray(int.class, Const.of(13), Const.of(42)));

                    bc.return_(bc.add(
                            array.elem(bc.new_(Integer.class, Const.of(0))),
                            array.elem(bc.new_(Integer.class, Const.of(1)))));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void arrayIndexViaHandle_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ArrayIndex", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     int[] array = new int[] {13, 42};
                //     return array[new Integer(0)] + array[new Integer(1)];
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar array = bc.localVar("array",
                            bc.newArray(int.class, Const.of(13), Const.of(42)));

                    bc.return_(bc.add(
                            bc.get(array.elem(bc.new_(Integer.class, Const.of(0))), MemoryOrder.Volatile),
                            bc.get(array.elem(bc.new_(Integer.class, Const.of(1))), MemoryOrder.Volatile)));
                });
            });
        });
        assertEquals(55, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void return_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Return", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return new Integer(3);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(Integer.class, Const.of(3)));
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void return_box() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Return", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     return 3;
                // }
                mc.returning(Object.class); // always `Integer`
                mc.body(bc -> {
                    bc.return_(Const.of(3));
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void return_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return 3;
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    bc.return_(Const.of(3));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void return_unboxAndWiden() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Return", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return new Integer(3);
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    bc.return_(bc.new_(Integer.class, Const.of(3)));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void return_widenAndBox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Return", cc -> {
            MethodDesc test = cc.staticMethod("test", mc -> {
                // static Long test() {
                //     return 3;
                // }
                mc.returning(Long.class);
                mc.body(bc -> {
                    bc.return_(Const.of(3));
                });
            });

            cc.staticMethod("testBridge", mc -> {
                // static Object testBridge() {
                //     return test();
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.invokeStatic(test));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("testBridge", Supplier.class).get());
    }

    @Test
    public void yield_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return true ? new Integer(3) : new Integer(5);
                // }
                mc.returning(int.class);
                mc.body(b0 -> {
                    b0.return_(b0.cond(int.class, Const.of(true), b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(3)));
                    }, b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(5)));
                    }));
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void yield_box() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            cc.staticMethod("test", mc -> {
                // static Integer test() {
                //     return true ? 3 : 5;
                // }
                mc.returning(Object.class); // always `Integer`
                mc.body(b0 -> {
                    b0.return_(b0.cond(Integer.class, Const.of(true), b1 -> {
                        b1.yield(Const.of(3));
                    }, b1 -> {
                        b1.yield(Const.of(5));
                    }));
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void yield_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return true ? 3 : 5;
                // }
                mc.returning(long.class);
                mc.body(b0 -> {
                    b0.return_(b0.cond(long.class, Const.of(true), b1 -> {
                        b1.yield(Const.of(3));
                    }, b1 -> {
                        b1.yield(Const.of(5));
                    }));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void yield_unboxAndWiden() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return true ? new Integer(3) : new Integer(5);
                // }
                mc.returning(long.class);
                mc.body(b0 -> {
                    b0.return_(b0.cond(long.class, Const.of(true), b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(3)));
                    }, b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(5)));
                    }));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void yield_widenAndBox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Yield", cc -> {
            MethodDesc test = cc.staticMethod("test", mc -> {
                // static Long test() {
                //     return true ? 3 : 5;
                // }
                mc.returning(long.class);
                mc.body(b0 -> {
                    b0.return_(b0.cond(long.class, Const.of(true), b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(3)));
                    }, b1 -> {
                        b1.yield(b1.new_(Integer.class, Const.of(5)));
                    }));
                });
            });

            cc.staticMethod("testBridge", mc -> {
                // static Object testBridge() {
                //     return test();
                // }
                mc.returning(Object.class);
                mc.body(bc -> {
                    bc.return_(bc.invokeStatic(test));
                });
            });
        });
        assertEquals(3L, tcm.staticMethod("testBridge", Supplier.class).get());
    }

    @Test
    public void newEmptyArray_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NewEmptyArray", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     int[] array = new int[new Integer(2)];
                //     return array;
                // }
                mc.returning(Object.class); // always `int[]`
                mc.body(bc -> {
                    Expr array = bc.newEmptyArray(int.class, bc.new_(Integer.class, Const.of(2)));
                    bc.return_(array);
                });
            });
        });
        assertArrayEquals(new int[2], (int[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void newArray_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NewArray", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     int[] array = new int[] { 13, new Integer(42), new Integer(13), 42 };
                //     return array;
                // }
                mc.returning(Object.class); // always `int[]`
                mc.body(bc -> {
                    Expr array = bc.newArray(int.class,
                            Const.of(13),
                            bc.new_(Integer.class, Const.of(42)),
                            bc.new_(Integer.class, Const.of(13)),
                            Const.of(42));
                    bc.return_(array);
                });
            });
        });
        assertArrayEquals(new int[] { 13, 42, 13, 42 }, (int[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void newArray_box() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NewArray", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     Integer[] array = new Integer[] { 13, new Integer(42), new Integer(13), 42 };
                //     return array;
                // }
                mc.returning(Object.class); // always `Integer[]`
                mc.body(bc -> {
                    Expr array = bc.newArray(Integer.class,
                            Const.of(13),
                            bc.new_(Integer.class, Const.of(42)),
                            bc.new_(Integer.class, Const.of(13)),
                            Const.of(42));
                    bc.return_(array);
                });
            });
        });
        assertArrayEquals(new Integer[] { 13, 42, 13, 42 }, (Integer[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void newArray_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NewArray", cc -> {
            cc.staticMethod("test", mc -> {
                // static Object test() {
                //     double[] array = new double[] { 13, 42L, 5.0F };
                //     return array;
                // }
                mc.returning(Object.class); // always `double[]`
                mc.body(bc -> {
                    Expr array = bc.newArray(double.class,
                            Const.of(13),
                            Const.of(42L),
                            Const.of(5.0F));
                    bc.return_(array);
                });
            });
        });
        assertArrayEquals(new double[] { 13.0, 42.0, 5.0 }, (double[]) tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void plus_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Plus", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return 3 + new Integer(5);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    Expr a = Const.of(3);
                    Expr b = bc.new_(Integer.class, Const.of(5));
                    bc.return_(bc.add(a, b));
                });
            });
        });
        assertEquals(8, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void plus_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Plus", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return 3 + 5L;
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    Expr a = Const.of(3);
                    Expr b = Const.of(5L);
                    bc.return_(bc.add(a, b));
                });
            });
        });
        assertEquals(8L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void plus_unboxAndWiden() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Plus", cc -> {
            cc.staticMethod("test", mc -> {
                // static long test() {
                //     return new Integer(3) + new Long(5L);
                // }
                mc.returning(long.class);
                mc.body(bc -> {
                    Expr a = bc.new_(Integer.class, Const.of(3));
                    Expr b = bc.new_(Long.class, Const.of(5L));
                    bc.return_(bc.add(a, b));
                });
            });
        });
        assertEquals(8L, tcm.staticMethod("test", LongSupplier.class).getAsLong());
    }

    @Test
    public void shl_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Shl", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return new Integer(5) << new Integer(2);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    Expr a = bc.new_(Integer.class, Const.of(5));
                    Expr b = bc.new_(Integer.class, Const.of(2));
                    bc.return_(bc.shl(a, b));
                });
            });
        });
        assertEquals(20, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void neg_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Neg", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return -new Integer(5);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.neg(bc.new_(Integer.class, Const.of(5))));
                });
            });
        });
        assertEquals(-5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void if_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.If", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(boolean param) {
                //     if (new Boolean(param)) {
                //         return 3;
                //     }
                //     return 5;
                // }
                mc.returning(int.class);
                ParamVar param = mc.parameter("param", boolean.class);
                mc.body(b0 -> {
                    Expr cond = b0.new_(Boolean.class, param);
                    b0.if_(cond, b1 -> {
                        b1.return_(3);
                    });
                    b0.return_(5);
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", BooleanToIntFunction.class).apply(true));
        assertEquals(5, tcm.staticMethod("test", BooleanToIntFunction.class).apply(false));
    }

    @Test
    public void ifNot_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfNot", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(boolean param) {
                //     if (!new Boolean(param)) {
                //         return 3;
                //     }
                //     return 5;
                // }
                mc.returning(int.class);
                ParamVar param = mc.parameter("param", boolean.class);
                mc.body(b0 -> {
                    Expr cond = b0.new_(Boolean.class, param);
                    b0.ifNot(cond, b1 -> {
                        b1.return_(3);
                    });
                    b0.return_(5);
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", BooleanToIntFunction.class).apply(false));
        assertEquals(5, tcm.staticMethod("test", BooleanToIntFunction.class).apply(true));
    }

    @Test
    public void ifElse_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.IfElse", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(boolean param) {
                //     if (new Boolean(param)) {
                //         return 3;
                //     } else {
                //         return 5;
                //     }
                // }
                mc.returning(int.class);
                ParamVar param = mc.parameter("param", boolean.class);
                mc.body(b0 -> {
                    Expr cond = b0.new_(Boolean.class, param);
                    b0.ifElse(cond, b1 -> {
                        b1.return_(3);
                    }, b1 -> {
                        b1.return_(5);
                    });
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", BooleanToIntFunction.class).apply(true));
        assertEquals(5, tcm.staticMethod("test", BooleanToIntFunction.class).apply(false));
    }

    @Test
    public void cond_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cond", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test(boolean param) {
                //     return new Boolean(param) ? 3 : 5;
                // }
                mc.returning(int.class);
                ParamVar param = mc.parameter("param", boolean.class);
                mc.body(b0 -> {
                    Expr cond = b0.new_(Boolean.class, param);
                    b0.return_(b0.cond(int.class, cond, b1 -> {
                        b1.yield(Const.of(3));
                    }, b1 -> {
                        b1.yield(Const.of(5));
                    }));
                });
            });
        });
        assertEquals(3, tcm.staticMethod("test", BooleanToIntFunction.class).apply(true));
        assertEquals(5, tcm.staticMethod("test", BooleanToIntFunction.class).apply(false));
    }

    @Test
    public void cmp_unbox() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cmp", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return cmp(5, new Integer(7));
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(Const.of(5), bc.new_(Integer.class, Const.of(7))));
                });
            });
        });
        assertEquals(-1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void cmp_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cmp", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return cmp(5, 7.0);
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(Const.of(5), Const.of(7.0)));
                });
            });
        });
        assertEquals(-1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void cmp_unboxAndWiden() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Cmp", cc -> {
            cc.staticMethod("test", mc -> {
                // static int test() {
                //     return cmp(new Integer(5), new Double(7.0));
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.cmp(bc.new_(Integer.class, Const.of(5)), bc.new_(Double.class, Const.of(7.0))));
                });
            });
        });
        assertEquals(-1, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void eq_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.EQ", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 == 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 == 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void ne_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 != 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 != 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void lt_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LT", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 < 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 < 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.lt(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void le_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.LE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 <= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 <= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.le(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void gt_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GT", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 > 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 > 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.gt(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void ge_widen() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return 5 >= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(5), Const.of(7.0)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return 7 >= 7.0;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ge(Const.of(7), Const.of(7.0)));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void eq_noConversion() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.EQ", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return new Integer(5) == null;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(bc.new_(Integer.class, Const.of(5)), Const.ofNull(Integer.class)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return new Integer(5) == new Integer(7);
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.eq(bc.new_(Integer.class, Const.of(5)), bc.new_(Integer.class, Const.of(7))));
                });
            });
            cc.staticMethod("test3", mc -> {
                // static boolean test3() {
                //     Integer i = new Integer(5);
                //     return i == i;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    LocalVar i = bc.localVar("i", bc.new_(Integer.class, Const.of(5)));
                    bc.return_(bc.eq(i, i));
                });
            });
        });
        assertFalse(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void ne_noConversion() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.NE", cc -> {
            cc.staticMethod("test1", mc -> {
                // static boolean test1() {
                //     return new Integer(5) != null;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(bc.new_(Integer.class, Const.of(5)), Const.ofNull(Integer.class)));
                });
            });
            cc.staticMethod("test2", mc -> {
                // static boolean test2() {
                //     return new Integer(5) != new Integer(7);
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    bc.return_(bc.ne(bc.new_(Integer.class, Const.of(5)), bc.new_(Integer.class, Const.of(7))));
                });
            });
            cc.staticMethod("test3", mc -> {
                // static boolean test3() {
                //     Integer i = new Integer(5);
                //     return i != i;
                // }
                mc.returning(boolean.class);
                mc.body(bc -> {
                    LocalVar i = bc.localVar("i", bc.new_(Integer.class, Const.of(5)));
                    bc.return_(bc.ne(i, i));
                });
            });
        });
        assertTrue(tcm.staticMethod("test1", BooleanSupplier.class).getAsBoolean());
        assertTrue(tcm.staticMethod("test2", BooleanSupplier.class).getAsBoolean());
        assertFalse(tcm.staticMethod("test3", BooleanSupplier.class).getAsBoolean());
    }
}
