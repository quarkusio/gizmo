package io.quarkus.gizmo2.impl;

import static io.smallrye.common.constraint.Assert.*;

import java.lang.constant.ClassDesc;
import java.util.List;

import io.quarkus.gizmo2.Const;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.LocalVar;
import io.quarkus.gizmo2.ParamVar;
import io.quarkus.gizmo2.creator.BlockCreator;
import io.quarkus.gizmo2.creator.ClassCreator;
import io.quarkus.gizmo2.creator.ops.StringBuilderOps;
import io.quarkus.gizmo2.desc.FieldDesc;
import io.quarkus.gizmo2.desc.MethodDesc;

public class EqualsHashCodeToStringGenerator {
    private final ClassCreator cc;
    private final List<FieldDesc> fields;

    public EqualsHashCodeToStringGenerator(ClassCreator cc, List<FieldDesc> fields) {
        this.cc = cc;
        this.fields = fields;
    }

    public void generateEquals() {
        MethodDesc floatToIntBits = MethodDesc.of(Float.class, "floatToIntBits", int.class, float.class);
        MethodDesc doubleToLongBits = MethodDesc.of(Double.class, "doubleToLongBits", long.class, double.class);

        ClassDesc thisClass = cc.type();

        cc.method("equals", mc -> {
            mc.public_();
            mc.returning(boolean.class);
            ParamVar other = mc.parameter("other", Object.class);
            mc.body(b0 -> {
                b0.if_(b0.eq(cc.this_(), other), BlockCreator::returnTrue);
                b0.ifNotInstanceOf(other, thisClass, BlockCreator::returnFalse);

                Expr otherCast = b0.define("other", b0.cast(other, thisClass));
                for (FieldDesc field : fields) {
                    if (!field.owner().equals(thisClass)) {
                        throw new IllegalArgumentException(
                                "Field does not belong to " + thisClass.displayName() + ": " + field);
                    }

                    LocalVar thisValue = b0.define("thisValue", b0.get(cc.this_().field(field)));
                    LocalVar thatValue = b0.define("thatValue", b0.get(otherCast.field(field)));
                    String fieldDesc = field.type().descriptorString();
                    switch (fieldDesc.charAt(0)) {
                        // boolean, byte, short, int, long, char
                        case 'Z', 'B', 'S', 'I', 'J', 'C' -> b0.if_(b0.ne(thisValue, thatValue), BlockCreator::returnFalse);
                        // float
                        case 'F' -> {
                            // this is consistent with Arrays.equals() and it's also what IntelliJ generates
                            Expr thisBits = b0.invokeStatic(floatToIntBits, thisValue);
                            Expr thatBits = b0.invokeStatic(floatToIntBits, thatValue);
                            b0.if_(b0.ne(thisBits, thatBits), BlockCreator::returnFalse);
                        }
                        // double
                        case 'D' -> {
                            // this is consistent with Arrays.equals() and it's also what IntelliJ generates
                            Expr thisBits = b0.invokeStatic(doubleToLongBits, thisValue);
                            Expr thatBits = b0.invokeStatic(doubleToLongBits, thatValue);
                            b0.if_(b0.ne(thisBits, thatBits), BlockCreator::returnFalse);
                        }
                        // Object
                        case 'L' -> b0.ifNot(b0.exprEquals(thisValue, thatValue), BlockCreator::returnFalse);
                        // array
                        case '[' -> b0.ifNot(b0.arrayEquals(thisValue, thatValue), BlockCreator::returnFalse);
                        default -> throw impossibleSwitchCase(field);
                    }
                }

                b0.return_(true);
            });
        });
    }

    public void generateHashCode() {
        ClassDesc thisClass = cc.type();

        cc.method("hashCode", mc -> {
            mc.public_();
            mc.returning(int.class);
            mc.body(b0 -> {
                if (fields.isEmpty()) {
                    b0.return_(0);
                    return;
                }

                LocalVar result = b0.define("result", Const.of(1));
                for (FieldDesc field : fields) {
                    if (!field.owner().equals(thisClass)) {
                        throw new IllegalArgumentException(
                                "Field does not belong to " + thisClass.displayName() + ": " + field);
                    }

                    LocalVar value = b0.define("value", b0.get(cc.this_().field(field)));
                    LocalVar hash = b0.define("hash",
                            field.type().isArray() ? b0.arrayHashCode(value) : b0.exprHashCode(value));
                    b0.set(result, b0.add(b0.mul(Const.of(31), result), hash));
                }

                b0.return_(result);
            });
        });
    }

    public void generateToString() {
        ClassDesc thisClass = cc.type();

        cc.method("toString", mc -> {
            mc.public_();
            mc.returning(String.class);
            mc.body(b0 -> {
                StringBuilderOps result = b0.withNewStringBuilder();
                result.append(thisClass.displayName() + '(');

                boolean first = true;
                for (FieldDesc field : fields) {
                    if (!field.owner().equals(thisClass)) {
                        throw new IllegalArgumentException(
                                "Field does not belong to " + thisClass.displayName() + ": " + field);
                    }

                    if (first) {
                        result.append(field.name() + '=');
                    } else {
                        result.append(", " + field.name() + '=');
                    }

                    Expr value = b0.get(cc.this_().field(field));
                    result.append(field.type().isArray() ? b0.arrayToString(value) : value);

                    first = false;
                }

                result.append(')');
                b0.return_(result.objToString());
            });
        });
    }
}
