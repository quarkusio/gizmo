package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.github.dmlloyd.classfile.Label;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.ClassConstant;

/**
 * A switch over {@code Class} objects.
 */
public final class ClassSwitchCreatorImpl extends HashSwitchCreatorImpl<ClassConstant> {
    ClassSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type) {
        super(enclosing, switchVal, type, ClassConstant.class);
    }

    boolean staticEquals(final ClassConstant a, final ClassConstant b) {
        return a.desc().equals(b.desc());
    }

    void equaller(final CodeBuilder cb, final ClassConstant value, final Label ifEq) {
        value.writeCode(cb, enclosing);
        cb.if_acmpeq(ifEq);
    }

    int staticHash(final ClassConstant val) {
        ClassDesc desc = val.desc();
        if (desc.isArray()) {
            return desc.descriptorString().hashCode();
        } else if (desc.isPrimitive()) {
            return desc.displayName().hashCode();
        } else {
            String ds = desc.descriptorString();
            return ds.substring(1, ds.length() - 1).replace('/', '.').hashCode();
        }
    }

    void hash(final CodeBuilder cb) {
        cb.invokevirtual(CD_Class, "getName", MethodTypeDesc.of(CD_String));
        cb.invokevirtual(CD_String, "hashCode", MethodTypeDesc.of(CD_int));
    }
}
