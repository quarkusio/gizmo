package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.FieldDesc;

public class FieldAccessTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            FieldDesc bravoDesc = cc.field("bravo", fc -> {
                fc.setType(String.class);
            });
            cc.constructor(con -> {
                // this.bravo = "charlie";
                con.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    var bravo = cc.this_().field(bravoDesc);
                    bc.set(bravo, Const.of("charlie"));
                    bc.return_();
                });
            });
            cc.method("test", mc -> {
                // int test() {
                //    return bravo.length();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var b = bc.get(cc.this_().field(bravoDesc));
                    var length = bc.withString(b).length();
                    bc.return_(length);
                });
            });
        });
        assertEquals(7, tcm.instanceMethod("test", ToIntFunction.class).applyAsInt(tcm.constructor(Supplier.class).get()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            var bravo = cc.staticField("bravo", fc -> {
                fc.setType(String.class);
                fc.setInitial(Const.of("charlie"));
            });
            cc.defaultConstructor();
            cc.staticMethod("test", mc -> {
                // Object test(Object input) {
                //    if (input != null) {
                //       Alpha.bravo = Alpha.bravo.concat("s");
                //    }
                //    if (input == null) {
                //       Alpha.bravo = "NULL";
                //    }
                //    return Alpha.bravo;
                // }
                mc.returning(Object.class);
                ParamVar input = mc.parameter("input", Object.class);
                mc.body(bc -> {
                    bc.ifNotNull(input, bc1 -> {
                        var b = bc1.get(bravo);
                        var newVal = bc1.withString(b).concat(Const.of("s"));
                        bc1.set(bravo, newVal);
                    });
                    bc.ifNull(input, bc1 -> {
                        bc1.setStaticField(bravo.desc(), Const.of("NULL"));
                    });
                    bc.return_(bc.getStaticField(bravo.desc()));
                });
            });
        });
        assertEquals("charlies", tcm.staticMethod("test", Function.class).apply("foo").toString());
        assertEquals("NULL", tcm.staticMethod("test", Function.class).apply(null).toString());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testConstantField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.Alpha", cc -> {
            var bravo = cc.constantField("BRAVO", Const.of("charlie"));
            cc.defaultConstructor();
            cc.method("test", mc -> {
                // int test() {
                //    return BRAVO.length();
                // }
                mc.returning(int.class);
                mc.body(bc -> {
                    var b = bc.get(bravo);
                    var length = bc.withString(b).length();
                    bc.return_(length);
                });
            });
        });
        assertEquals(7, tcm.instanceMethod("test", ToIntFunction.class).applyAsInt(tcm.constructor(Supplier.class).get()));
    }

    @Test
    public void testInstanceConstantField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        String className = "io.quarkus.gizmo2.InstanceConstantFieldTest";
        g.class_(className, zc -> {
            // create an instance field and initialize it to a constant value
            FieldDesc field1 = zc.field("field1", ifc -> {
                ifc.setType(String.class);
                ifc.final_();
                ifc.setInitial("Hello world");
            });
            // create a constructor that does not explicitly initialize the field
            zc.constructor(cc -> {
                cc.public_();
                cc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), zc.this_());
                    b0.return_();
                });
            });
            // a test method to verify the result
            zc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.public_();
                smc.body(b0 -> {
                    Expr instance = b0.new_(ClassDesc.of(className));
                    b0.return_(b0.objEquals(instance.field(field1), Const.of("Hello world")));
                });
            });
        });
        assertTrue(tcm.staticMethod("test0", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void testInstanceInitializedField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        String className = "io.quarkus.gizmo2.InstanceInitializedFieldTest";
        g.class_(className, zc -> {
            // create an instance field and initialize it to a non-constant value
            FieldDesc field1 = zc.field("field1", ifc -> {
                ifc.setType(String.class);
                ifc.final_();
                ifc.setInitializer(b0 -> {
                    b0.yield(b0.withString(Const.of("Hello")).concat(Const.of(" world")));
                });
            });
            // create a constructor that does not explicitly initialize the field
            zc.constructor(cc -> {
                cc.public_();
                cc.body(b0 -> {
                    b0.invokeSpecial(ConstructorDesc.of(Object.class), zc.this_());
                    b0.return_();
                });
            });
            // a test method to verify the result
            zc.staticMethod("test0", smc -> {
                smc.returning(boolean.class);
                smc.public_();
                smc.body(b0 -> {
                    Expr instance = b0.new_(ClassDesc.of(className));
                    b0.return_(b0.objEquals(instance.field(field1), Const.of("Hello world")));
                });
            });
        });
        assertTrue(tcm.staticMethod("test0", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void testStaticInitializedField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticInitializedFieldTest", cc -> {
            // create a static field and initialize it to a non-constant value
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.final_();
                fc.setType(String.class);
                fc.setInitializer(bc -> {
                    bc.yield(bc.withString(Const.of("Hello")).concat(Const.of(" world")));
                });
            });
            // a test method to verify the result
            cc.staticMethod("test", mc -> {
                mc.returning(boolean.class);
                mc.public_();
                mc.body(bc -> {
                    bc.return_(bc.objEquals(fieldVar, Const.of("Hello world")));
                });
            });
        });
        assertTrue(tcm.staticMethod("test", BooleanSupplier.class).getAsBoolean());
    }

    @Test
    public void testStaticInitializedFieldToInstanceOfItsClass() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticInitializedFieldToInstanceOfItsClassTest", cc -> {
            // create a static field and initialize it to a non-constant value
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.final_();
                fc.setType(cc.type());
                fc.setInitializer(bc -> {
                    bc.yield(bc.new_(cc.type()));
                });
            });
            cc.defaultConstructor();
            // a test method to verify the result
            cc.staticMethod("test", mc -> {
                mc.returning(Object.class);
                mc.public_();
                mc.body(bc -> {
                    bc.return_(fieldVar);
                });
            });
        });
        assertNotNull(tcm.staticMethod("test", Supplier.class).get());
    }

    @Test
    public void testGetStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GetStaticFieldTest", cc -> {
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(fieldVar);
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testExplicitGetStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ExplicitGetStaticFieldTest", cc -> {
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.get(fieldVar));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testVolatileGetStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileGetStaticFieldTest", cc -> {
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.get(fieldVar, MemoryOrder.Volatile));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testSetStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.SetStaticFieldTest", cc -> {
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.setType(int.class);
            });

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    bc.set(fieldVar, param);
                    bc.return_(fieldVar);
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }

    @Test
    public void testVolatileSetStaticField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileSetStaticFieldTest", cc -> {
            StaticFieldVar fieldVar = cc.staticField("field", fc -> {
                fc.setType(int.class);
            });

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    bc.set(fieldVar, param, MemoryOrder.Volatile);
                    bc.return_(fieldVar);
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }

    @Test
    public void testGetInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.GetInstanceFieldTest", cc -> {
            FieldDesc field = cc.field("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));
                    bc.return_(instance.field(field));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testExplicitGetInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.ExplicitGetInstanceFieldTest", cc -> {
            FieldDesc field = cc.field("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));
                    bc.return_(bc.get(instance.field(field)));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testVolatileGetInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileGetInstanceFieldTest", cc -> {
            FieldDesc field = cc.field("field", fc -> {
                fc.setType(int.class);
                fc.setInitial(5);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));
                    bc.return_(bc.get(instance.field(field), MemoryOrder.Volatile));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntSupplier.class).getAsInt());
    }

    @Test
    public void testSetInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.SetInstanceFieldTest", cc -> {
            FieldDesc field = cc.field("field", fc -> {
                fc.setType(int.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field), param);

                    bc.return_(instance.field(field));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }

    @Test
    public void testVolatileSetInstanceField() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VolatileSetInstanceFieldTest", cc -> {
            FieldDesc field = cc.field("field", fc -> {
                fc.setType(int.class);
            });

            cc.defaultConstructor();

            cc.staticMethod("test", mc -> {
                mc.returning(int.class);
                ParamVar param = mc.parameter("value", int.class);
                mc.body(bc -> {
                    LocalVar instance = bc.localVar("instance", bc.new_(cc.type()));

                    bc.set(instance.field(field), param, MemoryOrder.Volatile);

                    bc.return_(instance.field(field));
                });
            });
        });
        assertEquals(5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(5));
        assertEquals(0, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(0));
        assertEquals(-5, tcm.staticMethod("test", IntUnaryOperator.class).applyAsInt(-5));
    }
}
