package io.quarkus.gizmo2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.constant.ClassDesc;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import io.github.dmlloyd.classfile.Signature.ClassTypeSig;
import io.github.dmlloyd.classfile.Signature.TypeArg;
import io.quarkus.gizmo2.desc.ConstructorDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public class InstanceExecutableCreatorTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testThis() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_(ClassDesc.of("io.quarkus.gizmo2.TestFun"), cc -> {
            cc.implements_(ClassTypeSig.of(Function.class.getName(), TypeArg.of(ClassTypeSig.of(String.class.getName()))));
            cc.constructor(con -> {
                con.public_();
                Var this_ = con.this_();
                con.body(bc -> {
                    bc.invokeSpecial(ConstructorDesc.of(Object.class), this_);
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
                Var this_ = mc.this_();
                mc.body(bc -> {
                    // return convert((String)t);
                    Expr strVal = bc.cast(p, String.class);
                    bc.return_(bc.invokeVirtual(convert, this_, strVal));
                });
            });
        });
        assertEquals("foo", tcm.noArgsConstructor(Function.class).apply("Foo"));
    }

}
