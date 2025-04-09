package io.quarkus.gizmo2.impl;

import static java.lang.constant.ConstantDescs.*;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import io.github.dmlloyd.classfile.CodeBuilder;
import io.quarkus.gizmo2.Expr;
import io.quarkus.gizmo2.impl.constant.EnumConstant;

/**
 * A switch over a single {@code enum} type by ordinal value.
 * Such switches are incompatible across modules in the event that the set of {@code enum} constants
 * changes.
 */
public final class EnumOrdinalSwitchCreatorImpl extends PerfectHashSwitchCreatorImpl<EnumConstant> {
    final List<EnumConstant> constants;

    EnumOrdinalSwitchCreatorImpl(final BlockCreatorImpl enclosing, final Expr switchVal, final ClassDesc type,
            final Class<? extends Enum<?>> enumClass) {
        super(enclosing, switchVal, type, EnumConstant.class);
        constants = Stream.of(enumClass.getEnumConstants()).map(EnumConstant::of).map(EnumConstant.class::cast).toList();
    }

    int staticHash(final EnumConstant val) {
        int idx = constants.indexOf(val);
        if (idx == -1) {
            throw new NoSuchElementException("No known enum constant " + val);
        }
        return idx;
    }

    void hash(final CodeBuilder cb) {
        cb.invokevirtual(CD_Enum, "ordinal", MethodTypeDesc.of(CD_int));
    }
}
