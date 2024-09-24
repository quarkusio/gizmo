package io.quarkus.gizmo2.impl;

import java.lang.constant.ClassDesc;
import java.lang.constant.ConstantDescs;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.github.dmlloyd.classfile.extras.reflect.AccessFlag;
import io.quarkus.gizmo2.creator.ParamCreator;

public final class ParamCreatorImpl extends AnnotatableCreatorImpl implements ParamCreator {
    int flags = 0;
    ClassDesc type = ConstantDescs.CD_int;

    ParamVarImpl apply(final Consumer<ParamCreator> builder, final String name, final int index, final int slot) {
        builder.accept(this);
        return new ParamVarImpl(type, name, index, slot, flags, List.copyOf(invisible), List.copyOf(visible));
    }

    public void withFlag(final AccessFlag flag) {
        if (flag.locations().contains(AccessFlag.Location.METHOD_PARAMETER)) {
            flags |= flag.mask();
        } else {
            throw new IllegalArgumentException("Invalid flag for parameter: " + flag);
        }
    }

    public void withType(final ClassDesc type) {
        Objects.requireNonNull(type, "type");
        if (type.equals(ConstantDescs.CD_void)) {
            throw new IllegalArgumentException("Bad type for parameter: " + type);
        }
        this.type = type;
    }
}
