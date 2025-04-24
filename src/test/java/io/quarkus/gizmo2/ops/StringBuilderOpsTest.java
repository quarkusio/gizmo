package io.quarkus.gizmo2.ops;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.Gizmo;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.TestClassMaker;
import io.quarkus.gizmo2.creator.ops.StringBuilderOps;
import io.quarkus.gizmo2.desc.MethodDesc;

public class StringBuilderOpsTest {
    @Test
    public void testStringBuilder() {
        TestClassMaker tcm = new TestClassMaker();
        Gizmo g = Gizmo.create(tcm);
        g.class_("io.quarkus.gizmo2.TestStringBuilder", cc -> {
            MethodDesc charSeq = cc.staticMethod("createCharSequence", mc -> {
                mc.returning(CharSequence.class);
                mc.body(bc -> {
                    LocalVar strBuilder = bc.define("stringBuilder", bc.new_(StringBuilder.class));
                    bc.withStringBuilder(strBuilder).append("ghi");
                    bc.return_(strBuilder);
                });
            });

            cc.staticMethod("createString", mc -> {
                mc.returning(Object.class); // in fact always `String`
                mc.body(bc -> {
                    StringBuilderOps strBuilder = bc.withNewStringBuilder();
                    strBuilder.append(Const.of(true));
                    strBuilder.append(Const.of((byte) 1));
                    strBuilder.append(Const.of((short) 2));
                    strBuilder.append(Const.of(3));
                    strBuilder.append(Const.of(4L));
                    strBuilder.append(Const.of(5.0F));
                    strBuilder.append(Const.of(6.0));
                    strBuilder.append(Const.of('a'));
                    Expr charArray = bc.newArray(char.class, Const.of('b'), Const.of('c'));
                    strBuilder.append(charArray);
                    strBuilder.append(Const.of("def"));
                    strBuilder.append(bc.invokeStatic(charSeq));
                    strBuilder.append(bc.new_(MyObject.class));
                    strBuilder.append(Const.ofNull(Object.class));
                    strBuilder.append("...");
                    strBuilder.append('!');
                    bc.return_(strBuilder.objToString());
                });
            });
        });
        assertEquals("true12345.06.0abcdefghijklmnull...!", tcm.staticMethod("createString", Supplier.class).get());
    }

    public static class MyObject {
        @Override
        public String toString() {
            return "jklm";
        }
    }
}
