package io.quarkus.gizmo2;

import static java.lang.constant.ConstantDescs.CD_String;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ClassMethodDesc;
import io.quarkus.gizmo2.desc.InterfaceMethodDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public class InvocationTest {
    private static final MethodDesc MD_StringBuilder_append = MethodDesc.of(StringBuilder.class,
            "append", StringBuilder.class, String.class);

    @Test
    public void invokeStaticOnClass() {
        // class StaticInvocation {
        //     static String returnString(String input) {
        //         return input + "_foobar";
        //     }
        //
        //     static Object invoke() {
        //         return returnString("input"); // <--- invokestatic
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.StaticInvocation", cc -> {
            MethodDesc returnString = cc.staticMethod("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    bc.return_(bc.invokeStatic(returnString, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeVirtualOnClass() {
        // class VirtualInvocation {
        //     String returnString(String input) {
        //         return input + "_foobar";
        //     }
        //
        //     static Object invoke() {
        //         return new VirtualInvocation().returnString("input"); // <--- invokevirtual
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.VirtualInvocation", cc -> {
            cc.defaultConstructor();

            MethodDesc returnString = cc.method("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    bc.return_(bc.invokeVirtual(returnString, instance, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeSpecialOnClass() {
        // class MySuperclass {
        //     String returnString(String input) {
        //         return input + "_foobar";
        //     }
        // }
        //
        // class SpecialInvocation extends MySuperclass {
        //     String returnStringCaller(String input) {
        //         return super.returnString(input); // <--- invokespecial
        //     }
        //
        //     static Object invoke() {
        //         return new SpecialInvocation().returnStringCaller("input");
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc superclass = g.class_("io.quarkus.gizmo2.MySuperclass", cc -> {
            cc.defaultConstructor();

            cc.method("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });
        });

        g.class_("io.quarkus.gizmo2.SpecialInvocation", cc -> {
            cc.extends_(superclass);

            cc.defaultConstructor();

            MethodDesc returnStringCaller = cc.method("returnStringCaller", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    MethodDesc desc = ClassMethodDesc.of(superclass, "returnString",
                            MethodTypeDesc.of(CD_String, CD_String));
                    bc.return_(bc.invokeSpecial(desc, cc.this_(), input));
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    bc.return_(bc.invokeVirtual(returnStringCaller, instance, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeInterfaceOnClass() {
        // interface MyInterface {
        //     String returnString(String input);
        // }
        //
        // class InterfaceInvocation implements MyInterface {
        //     public String returnString(String input) {
        //         return input + "_foobar";
        //     }
        //
        //     static Object invoke() {
        //         return ((MyInterface) new InterfaceInvocation()).returnString("input"); // <--- invokeinterface
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc myInterface = g.interface_("io.quarkus.gizmo2.MyInterface", cc -> {
            cc.method("returnString", mc -> {
                mc.parameter("input", String.class);
                mc.returning(String.class);
            });
        });

        g.class_("io.quarkus.gizmo2.InterfaceInvocation", cc -> {
            cc.implements_(myInterface);

            cc.defaultConstructor();

            cc.method("returnString", mc -> {
                mc.public_();
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    MethodDesc desc = InterfaceMethodDesc.of(myInterface, "returnString",
                            MethodTypeDesc.of(CD_String, CD_String));
                    bc.return_(bc.invokeInterface(desc, instance, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeStaticOnInterface() {
        // interface MyInterface {
        //     static String returnString(String input) {
        //         return input + "_foobar";
        //     }
        // }
        //
        // class StaticInterfaceInvocation {
        //     static Object invoke() {
        //         return MyInterface.returnString("input"); // <--- invokestatic
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc myInterface = g.interface_("io.quarkus.gizmo2.MyInterface", cc -> {
            cc.staticMethod("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });
        });

        g.class_("io.quarkus.gizmo2.StaticInvocation", cc -> {
            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    MethodDesc desc = InterfaceMethodDesc.of(myInterface, "returnString",
                            MethodTypeDesc.of(CD_String, CD_String));
                    bc.return_(bc.invokeStatic(desc, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeSpecialOnInterface() {
        // interface MyInterface {
        //     default String returnString(String input) {
        //         return input + "_foobar";
        //     }
        // }
        //
        // class SpecialInterfaceInvocation implements MyInterface {
        //     String returnStringCaller(String input) {
        //         return MyInterface.super.returnString(input); // <--- invokespecial
        //     }
        //
        //     static Object invoke() {
        //         return new SpecialInterfaceInvocation().returnStringCaller("input");
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc myInterface = g.interface_("io.quarkus.gizmo2.MyInterface", cc -> {
            cc.defaultMethod("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });
        });

        g.class_("io.quarkus.gizmo2.SpecialInvocation", cc -> {
            cc.implements_(myInterface);

            cc.defaultConstructor();

            MethodDesc returnStringCaller = cc.method("returnStringCaller", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    MethodDesc desc = InterfaceMethodDesc.of(myInterface, "returnString",
                            MethodTypeDesc.of(CD_String, CD_String));
                    bc.return_(bc.invokeSpecial(desc, cc.this_(), input));
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    bc.return_(bc.invokeVirtual(returnStringCaller, instance, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    @Test
    public void invokeInterfaceOnInterface() {
        // interface MyInterface {
        //     String returnString(String input);
        //
        //     default String returnStringCaller(String input) {
        //         return returnString(input); // <--- invokeinterface
        //     }
        // }
        //
        // class InterfaceInvocation implements MyInterface {
        //     public String returnString(String input) {
        //         return input + "_foobar";
        //     }
        //
        //     static Object invoke() {
        //         return new InterfaceInvocation().returnStringCaller("foobar");
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc myInterface = g.interface_("io.quarkus.gizmo2.MyInterface", cc -> {
            MethodDesc returnString = cc.method("returnString", mc -> {
                mc.parameter("input", String.class);
                mc.returning(String.class);
            });

            cc.defaultMethod("returnStringCaller", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_(bc.invokeInterface(returnString, cc.this_(), input));
                });
            });
        });

        g.class_("io.quarkus.gizmo2.InterfaceInvocation", cc -> {
            cc.implements_(myInterface);

            cc.defaultConstructor();

            cc.method("returnString", mc -> {
                mc.public_();
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    MethodDesc desc = InterfaceMethodDesc.of(myInterface, "returnStringCaller",
                            MethodTypeDesc.of(CD_String, CD_String));
                    bc.return_(bc.invokeInterface(desc, instance, Const.of("input")));
                });
            });
        });
        assertEquals("input_foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }

    // ---

    @Test
    public void wrongNumberOfArguments() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.WrongNumberOfArguments", cc -> {
            MethodDesc returnString = cc.staticMethod("returnString", mc -> {
                ParamVar input = mc.parameter("input", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    LocalVar result = bc.localVar("result", bc.new_(StringBuilder.class));
                    bc.invokeVirtual(MD_StringBuilder_append, result, input);
                    bc.invokeVirtual(MD_StringBuilder_append, result, Const.of("_foobar"));
                    bc.return_(bc.withObject(result).toString_());
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.body(bc -> {
                    assertThrows(IllegalArgumentException.class, () -> {
                        bc.invokeStatic(returnString);
                    });
                    assertThrows(IllegalArgumentException.class, () -> {
                        bc.invokeStatic(returnString, Const.of("input1"), Const.of("input2"));
                    });
                    bc.return_();
                });
            });
        });
    }

    @Test
    public void wrongArgumentTypes() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.WrongArgumentTypes", cc -> {
            MethodDesc returnInt = cc.staticMethod("returnInt", mc -> {
                ParamVar input = mc.parameter("input", int.class);
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.add(input, Const.of(1)));
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.body(bc -> {
                    assertThrows(IllegalArgumentException.class, () -> {
                        bc.invokeStatic(returnInt, Const.of(1L));
                    });
                    assertThrows(IllegalArgumentException.class, () -> {
                        bc.invokeStatic(returnInt, Const.of(1.0));
                    });
                    assertThrows(IllegalArgumentException.class, () -> {
                        bc.invokeStatic(returnInt, Const.of(""));
                    });

                    bc.return_();
                });
            });
        });
    }

    @Test
    public void differentButCorrectArgumentTypes() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.DifferentButCorrectArgumentTypes", cc -> {
            MethodDesc returnInt = cc.staticMethod("returnInt", mc -> {
                ParamVar input = mc.parameter("input", int.class);
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.add(input, Const.of(1)));
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(int.class);
                mc.body(bc -> {
                    bc.return_(bc.invokeStatic(returnInt, Const.of('a')));
                });
            });
        });
        assertEquals('b', tcm.staticMethod("invoke", IntSupplier.class).getAsInt());
    }

    @Test
    public void invokeNonInterfaceMethodOnInterface() {
        // interface MyInterface {
        //     String returnString();
        // }
        //
        // class InterfaceInvocation implements MyInterface {
        //     public String returnString() {
        //         return "foobar";
        //     }
        //
        //     static Object invoke() {
        //         return ((MyInterface) new InterfaceInvocation()).returnString(); // <--- invokeinterface
        //     }
        // }

        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);

        ClassDesc myInterface = g.interface_("io.quarkus.gizmo2.MyInterface", cc -> {
            cc.method("returnString", mc -> {
                mc.returning(String.class);
            });
        });

        g.class_("io.quarkus.gizmo2.InterfaceInvocation", cc -> {
            cc.implements_(myInterface);

            cc.defaultConstructor();

            cc.method("returnString", mc -> {
                mc.public_();
                mc.returning(String.class);
                mc.body(bc -> {
                    bc.return_("foobar");
                });
            });

            cc.staticMethod("invoke", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    Expr instance = bc.new_(cc.type());
                    assertThrows(IllegalArgumentException.class, () -> {
                        MethodDesc desc = ClassMethodDesc.of(myInterface, "returnString", MethodTypeDesc.of(CD_String));
                        bc.return_(bc.invokeInterface(desc, instance));
                    });
                    assertDoesNotThrow(() -> {
                        MethodDesc desc = InterfaceMethodDesc.of(myInterface, "returnString", MethodTypeDesc.of(CD_String));
                        bc.return_(bc.invokeInterface(desc, instance));
                    });
                });
            });
        });
        assertEquals("foobar", tcm.staticMethod("invoke", Supplier.class).get());
    }
}
