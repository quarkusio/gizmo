package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.constant.ClassDesc;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public class InstanceExecutableCreatorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testThis() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestFun"), cc -> {
            cc.implements_(
                    (GenericType.OfClass) GenericType.of(Function.class,
                            List.of(TypeArgument.ofExact(String.class), TypeArgument.ofExact(String.class))));
            cc.constructor(con -> {
                con.public_();
                con.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), cc.this_());
                    bc.return_();
                });
            });
            MethodDesc convert = cc.method("convert", mc -> {
                ParamVar p = mc.parameter("value", String.class);
                mc.returning(String.class);
                mc.body(bc -> {
                    // return val.toLowerCase();
                    Expr ret = bc.invokeVirtual(MethodDesc.of(String.class, "toLowerCase", String.class), p);
                    bc.return_(ret);
                });
            });
            cc.method("apply", mc -> {
                ParamVar p = mc.parameter("t", Object.class);
                mc.returning(Object.class);
                mc.public_();
                mc.body(bc -> {
                    // return convert((String)t);
                    Expr strVal = bc.cast(p, String.class);
                    bc.return_(bc.invokeVirtual(convert, cc.this_(), strVal));
                });
            });
        });
        assertEquals("foo", tcm.noArgsConstructor(Function.class).apply("Foo"));
    }

}
