package io.quarkus.gizmo2;

import java.lang.constant.ClassDesc;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import io.quarkus.gizmo2.desc.MethodDesc;
import io.quarkus.gizmo2.impl.constant.ConstImpl;

public final class BasicTest {

    @Test
    public void helloWorld() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("helloWorld", mc -> {
                mc.body(b0 -> {
                    b0.printf("Hello world!%n", List.of());
                    b0.return_();
                });
            });
        });
        tcm.staticMethod("helloWorld", Runnable.class).run();
    }

    @Test
    public void cleanStack() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("helloWorld", mc -> {
                mc.body(b0 -> {
                    b0.printf("Hello world!%n", List.of());
                    b0.printf("Hello world!%n", List.of());
                    b0.printf("Hello world!%n", List.of());
                    b0.printf("Hello world!%n", List.of());
                    b0.return_();
                });
            });
        });
        tcm.staticMethod("helloWorld", Runnable.class).run();
    }

    @Test
    public void params() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("echoArg", mc -> {
                Var argument = mc.parameter("argument", String.class);
                mc.returning(String.class);
                mc.body(b0 -> {
                    b0.return_(argument);
                });
            });
        });
        Assertions.assertEquals("hello world!", tcm.staticMethod("echoArg", OneParam.class).apply("hello world!"));
    }

    @Test
    public void twoParams2() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("echoConcat", mc -> {
                ParamVar first = mc.parameter("first", String.class);
                ParamVar second = mc.parameter("second", String.class);
                mc.returning(String.class);
                mc.body(b0 -> {
                    Expr concat1 = b0.invokeVirtual(MethodDesc.of(String.class, "concat", String.class, String.class), first,
                            List.of(ConstImpl.of(" ")));
                    LocalVar spaced = b0.define("spaced", concat1);
                    Expr concat2 = b0.invokeVirtual(MethodDesc.of(String.class, "concat", String.class, String.class), spaced,
                            List.of(second));

                    b0.return_(concat2);
                });
            });
        });
        Assertions.assertEquals("hello world!", tcm.staticMethod("echoConcat", TwoParams.class).apply("hello", "world!"));
    }

    @Test
    public void twoParams() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("echoSecondArg", mc -> {
                mc.parameter("ignored", String.class);
                ParamVar echoed = mc.parameter("echoed", String.class);
                mc.returning(String.class);
                mc.body(b0 -> {
                    b0.return_(echoed);
                });
            });
        });
        Assertions.assertEquals("hello world!",
                tcm.staticMethod("echoSecondArg", TwoParams.class).apply("ignore me!", "hello world!"));
    }

    @Test
    public void twoParamsWithIf() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("selectArg", mc -> {
                ParamVar arg0 = mc.parameter("arg0", String.class);
                ParamVar arg1 = mc.parameter("arg1", String.class);
                ParamVar sel = mc.parameter("sel", int.class);
                mc.returning(String.class);
                mc.body(b0 -> {
                    b0.ifElse(b0.eq(sel, 0), b1 -> b1.return_(arg0), b1 -> b1.return_(arg1));
                });
            });
        });
        Assertions.assertEquals("argument zero!",
                tcm.staticMethod("selectArg", TwoParamsWithSelect.class).apply("argument zero!", "argument one!", 0));
        Assertions.assertEquals("argument one!",
                tcm.staticMethod("selectArg", TwoParamsWithSelect.class).apply("argument zero!", "argument one!", 1));
    }

    @Test
    public void forEach() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestBasicOutput"), cc -> {
            cc.staticMethod("testForEach", mc -> {
                ParamVar arg0 = mc.parameter("items", Iterable.class);
                mc.body(b0 -> {
                    b0.forEach(arg0, (b1, item) -> {
                        b1.printf("Item: %s%n", List.of(item));
                    });
                    b0.return_();
                });
            });
        });
        tcm.staticMethod("testForEach", OneIterable.class).apply(List.of("one", "two", "three!"));
    }

    @Test
    public void throwStuff() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestThrowingStuff"), cc -> {
            cc.staticMethod("testThrowStuff", mc -> {
                mc.body(bc -> {
                    bc.throw_(Error.class, "Hello!");
                });
            });
        });
        Assertions.assertThrows(Error.class, tcm.staticMethod("testThrowStuff", Executable.class));
    }

    public interface OneIterable {
        void apply(Iterable<?> items);
    }

    public interface OneParam {
        String apply(String p0);
    }

    public interface TwoParams {
        String apply(String p0, String p1);
    }

    public interface TwoParamsWithSelect {
        String apply(String p0, String p1, int s0);
    }
}
