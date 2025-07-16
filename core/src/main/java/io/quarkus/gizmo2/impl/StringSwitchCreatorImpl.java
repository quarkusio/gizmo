package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.StringConst;

/**
 * A switch over {@code String} objects.
 */
public final class StringSwitchCreatorImpl extends HashSwitchCreatorImpl<StringConst> {
    StringSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, StringConst.class);
    }

    boolean staticEquals(final StringConst a, final StringConst b) {
        return a.desc().equals(b.desc());
    }

    void equaller(final CodeBuilder cb, final StringConst value, final Label ifEq) {
        value.writeCode(cb, enclosing);
        cb.invokevirtual(CD_String, "equals", MethodTypeDesc.of(CD_boolean, CD_Object));
        // returns 1 if equal
        cb.ifne(ifEq);
    }

    int staticHash(final StringConst val) {
        return val.desc().hashCode();
    }

    void hash(final CodeBuilder cb) {
        cb.invokevirtual(CD_String, "hashCode", MethodTypeDesc.of(CD_int));
    }
}
